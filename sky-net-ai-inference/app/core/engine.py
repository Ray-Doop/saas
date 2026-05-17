"""
AI任务引擎 - 核心处理流程
1. 解析任务消息
2. 提交工作流到 ComfyUI
3. 监听生成进度
4. 下载结果图片上传到 MinIO
5. 回调 Java 服务更新任务状态
"""
import json
import logging
from typing import Dict, Any
from datetime import datetime

from app.core.comfyui_client import comfyui_client
from app.utils.minio_util import upload_to_minio, get_image_info
from app.utils.callback import send_result_callback

logger = logging.getLogger(__name__)


async def process_task(message: Dict[str, Any]):
    """
    处理单个AI绘图任务

    message 结构:
    {
        "taskCode": "TASK_xxx",
        "workflowJson": "{...}",     # ComfyUI工作流JSON字符串
        "productImage": "http://...", # 商品图URL(可选)
        ...extraParams
    }
    """
    task_code = message.get("taskCode", "unknown")
    workflow_str = message.get("workflowJson", "{}")

    try:
        # 1. 解析工作流JSON
        try:
            workflow = json.loads(workflow_str) if isinstance(workflow_str, str) else workflow_str
        except json.JSONDecodeError as e:
            logger.error(f"工作流JSON解析失败: {task_code}, error={e}")
            await send_result_callback(task_code, "FAILED", error_message=f"工作流JSON无效: {e}")
            return

        # 2. 提交到 ComfyUI
        logger.info(f"提交工作流到ComfyUI: taskCode={task_code}")
        try:
            prompt_id = await comfyui_client.submit_workflow(workflow)
        except Exception as e:
            logger.error(f"提交ComfyUI失败: taskCode={task_code}, error={e}")
            await send_result_callback(task_code, "FAILED", error_message=f"提交ComfyUI失败: {e}")
            return

        # 3. 等待生成完成 (WebSocket 监听进度)
        logger.info(f"等待生成完成: taskCode={task_code}, prompt_id={prompt_id}")
        try:
            result = await comfyui_client.wait_for_completion(
                prompt_id,
                on_progress=lambda cur, max: logger.debug(f"进度 {task_code}: {cur}/{max}")
            )
        except TimeoutError:
            logger.error(f"ComfyUI生成超时: taskCode={task_code}")
            await send_result_callback(task_code, "FAILED", error_message="生成超时")
            return
        except Exception as e:
            logger.error(f"ComfyUI执行失败: taskCode={task_code}, error={e}")
            await send_result_callback(task_code, "FAILED", error_message=str(e))
            return

        # 4. 下载图片并上传到 MinIO
        images = result.get("images", [])
        if not images:
            logger.warning(f"未生成图片: taskCode={task_code}")
            await send_result_callback(task_code, "FAILED", error_message="未生成任何图片")
            return

        uploaded_images = []
        for img_info in images:
            filename = img_info["filename"]
            subfolder = img_info.get("subfolder", "")

            try:
                image_bytes = await comfyui_client.download_image(filename, subfolder)
                minio_url = await upload_to_minio(task_code, filename, image_bytes)
                img_w, img_h, img_size = get_image_info(image_bytes)

                uploaded_images.append({
                    "url": minio_url,
                    "filename": filename,
                    "fileSize": img_size,
                    "width": img_w,
                    "height": img_h
                })

                logger.info(f"图片已上传: {task_code} -> {minio_url}")
            except Exception as e:
                logger.error(f"上传图片失败: filename={filename}, error={e}")

        if not uploaded_images:
            await send_result_callback(task_code, "FAILED", error_message="图片上传失败")
            return

        # 5. 回调 Java 服务 - 更新任务状态为成功
        await send_result_callback(task_code, "SUCCESS", images=uploaded_images)
        logger.info(f"任务处理完成: taskCode={task_code}, images={len(uploaded_images)}")

    except Exception as e:
        logger.exception(f"任务处理异常: taskCode={task_code}")
        await send_result_callback(task_code, "FAILED", error_message=str(e))
