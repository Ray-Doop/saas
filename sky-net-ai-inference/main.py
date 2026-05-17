"""
智绘云 AI推理服务 - 主入口
FastAPI + RocketMQ Consumer + ComfyUI WebSocket
"""
import logging
import sys
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from config.settings import settings
from app.api.routes import router
from app.core.mq_consumer import mq_consumer

# 日志配置
logging.basicConfig(
    level=getattr(logging, settings.LOG_LEVEL),
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s" if settings.LOG_FORMAT != "json"
    else '{"time":"%(asctime)s","level":"%(levelname)s","logger":"%(name)s","message":"%(message)s"}',
    stream=sys.stdout
)

logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    # 启动时: 启动 RocketMQ 消费者
    logger.info(f"启动 {settings.APP_NAME} v{settings.APP_VERSION}")
    mq_consumer.start()

    yield

    # 关闭时: 停止消费者
    logger.info("正在关闭服务...")
    mq_consumer.stop()


app = FastAPI(
    title="智绘云 AI推理服务",
    description="基于 ComfyUI 的 AIGC 电商营销素材生成引擎",
    version=settings.APP_VERSION,
    lifespan=lifespan
)

# CORS 配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(router, prefix="/api/v1/ai")


@app.get("/")
async def root():
    return {
        "service": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "status": "running"
    }


@app.get("/health")
async def health():
    """健康检查端点"""
    return {
        "status": "healthy",
        "comfyui": settings.COMFYUI_BASE_URL,
        "rocketmq": settings.ROCKETMQ_NAME_SERVER
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=settings.DEBUG
    )
