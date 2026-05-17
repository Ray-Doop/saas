"""SkyNet AI Training - AI模型训练平台

LoRA自动化训练任务调度、训练数据集管理与版本控制
"""
from fastapi import FastAPI

app = FastAPI(title="SkyNet AI Training", version="1.0.0")


@app.get("/health")
async def health():
    return {"status": "ok", "service": "sky-net-ai-training"}
