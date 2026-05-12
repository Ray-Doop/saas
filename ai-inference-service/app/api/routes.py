"""AI推理服务 REST API"""
import json
import logging
from fastapi import APIRouter, HTTPException, BackgroundTasks
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any

from app.core.engine import process_task
from app.core.comfyui_client import comfyui_client

logger = logging.getLogger(__name__)
router = APIRouter()


class TaskRequest(BaseModel):
    task_code: str = Field(..., alias="taskCode", description="任务编号")
    workflow_json: str = Field(..., alias="workflowJson", description="ComfyUI工作流JSON")
    product_image: Optional[str] = Field(None, alias="productImage", description="商品图片URL")
    extra_params: Optional[Dict[str, Any]] = Field(None, alias="extraParams", description="额外参数")

    class Config:
        populate_by_name = True


@router.post("/generate")
async def generate_image(request: TaskRequest, background_tasks: BackgroundTasks):
    """
    直接调用AI绘图 (非MQ方式, 用于测试)

    提交工作流到 ComfyUI, 等待生成完成, 返回图片结果
    """
    message = {
        "taskCode": request.task_code,
        "workflowJson": request.workflow_json,
        "productImage": request.product_image,
    }
    if request.extra_params:
        message.update(request.extra_params)

    # 后台异步处理
    background_tasks.add_task(process_task, message)

    return {
        "code": 200,
        "message": "任务已提交",
        "data": {"taskCode": request.task_code}
    }


@router.get("/comfyui/status")
async def comfyui_status():
    """检查 ComfyUI 服务状态"""
    import httpx
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            response = await client.get(f"{comfyui_client.base_url}/system_stats")
            if response.status_code == 200:
                return {"status": "connected", "stats": response.json()}
            return {"status": "error", "message": f"HTTP {response.status_code}"}
    except Exception as e:
        return {"status": "disconnected", "message": str(e)}


@router.post("/workflow/validate")
async def validate_workflow(request: dict):
    """验证 ComfyUI 工作流JSON格式"""
    try:
        # 检查基本结构
        if not isinstance(request, dict):
            return {"valid": False, "message": "工作流必须是JSON对象"}

        nodes = request.get("nodes", [])
        if not nodes:
            return {"valid": False, "message": "工作流中没有节点"}

        node_count = len(nodes)
        return {
            "valid": True,
            "message": f"格式有效, 包含 {node_count} 个节点",
            "nodeCount": node_count
        }
    except Exception as e:
        return {"valid": False, "message": str(e)}
