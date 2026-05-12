"""
ComfyUI WebSocket 客户端
负责: 提交工作流 -> 监听进度 -> 获取生成结果
"""
import asyncio
import json
import uuid
import logging
from typing import Optional, Callable
import httpx
import websockets
from config.settings import settings

logger = logging.getLogger(__name__)


class ComfyUIClient:
    """ComfyUI API 客户端, 通过 WebSocket 实时获取生成进度"""

    def __init__(self):
        self.base_url = settings.COMFYUI_BASE_URL
        self.ws_url = settings.COMFYUI_WS_URL
        self.timeout = settings.COMFYUI_TIMEOUT

    async def submit_workflow(self, workflow_json: dict) -> str:
        """提交工作流到 ComfyUI, 返回 prompt_id"""
        url = f"{self.base_url}/prompt"
        payload = {
            "prompt": workflow_json,
            "client_id": str(uuid.uuid4())
        }

        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(url, json=payload)
            response.raise_for_status()
            data = response.json()

            if "prompt_id" not in data:
                raise RuntimeError(f"ComfyUI返回异常: {data}")

            prompt_id = data["prompt_id"]
            logger.info(f"工作流已提交: prompt_id={prompt_id}")
            return prompt_id

    async def wait_for_completion(
        self,
        prompt_id: str,
        on_progress: Optional[Callable[[int, int], None]] = None
    ) -> dict:
        """
        通过 WebSocket 监听执行进度, 等待完成
        返回: {"images": [{"filename": "...", "subfolder": "...", "type": "output"}]}
        """
        client_id = str(uuid.uuid4())
        ws_url = f"{self.ws_url}?clientId={client_id}"

        try:
            async with websockets.connect(ws_url) as ws:
                async with asyncio.timeout(self.timeout):
                    while True:
                        message = await ws.recv()
                        data = json.loads(message)

                        msg_type = data.get("type", "")
                        msg_data = data.get("data", {})

                        if msg_type == "progress":
                            current = msg_data.get("value", 0)
                            max_val = msg_data.get("max", 1)
                            if on_progress:
                                on_progress(current, max_val)
                            logger.debug(f"进度: {current}/{max_val}")

                        elif msg_type == "executing":
                            node = msg_data.get("node")
                            if node is None and msg_data.get("prompt_id") == prompt_id:
                                logger.info(f"工作流执行完成: prompt_id={prompt_id}")
                                break

                        elif msg_type == "execution_error":
                            error_msg = msg_data.get("exception_message", "未知错误")
                            raise RuntimeError(f"ComfyUI执行异常: {error_msg}")

            # 获取生成结果
            return await self._get_history(prompt_id)

        except asyncio.TimeoutError:
            raise TimeoutError(f"ComfyUI执行超时({self.timeout}s): prompt_id={prompt_id}")

    async def _get_history(self, prompt_id: str) -> dict:
        """获取执行历史, 包含生成的图片信息"""
        url = f"{self.base_url}/history/{prompt_id}"

        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.get(url)
            response.raise_for_status()
            data = response.json()

            if prompt_id not in data:
                raise RuntimeError(f"未找到prompt_id对应的历史: {prompt_id}")

            history = data[prompt_id]
            outputs = history.get("outputs", {})

            images = []
            for node_id, node_output in outputs.items():
                if "images" in node_output:
                    for img in node_output["images"]:
                        images.append({
                            "filename": img["filename"],
                            "subfolder": img.get("subfolder", ""),
                            "type": img.get("type", "output")
                        })

            return {"images": images, "status": history.get("status", {})}

    async def download_image(self, filename: str, subfolder: str = "") -> bytes:
        """从 ComfyUI 下载生成的图片"""
        params = {"filename": filename, "subfolder": subfolder, "type": "output"}
        url = f"{self.base_url}/view"

        async with httpx.AsyncClient(timeout=60.0) as client:
            response = await client.get(url, params=params)
            response.raise_for_status()
            return response.content


# 全局单例
comfyui_client = ComfyUIClient()
