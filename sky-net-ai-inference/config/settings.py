"""智绘云 AI推理服务 - 配置管理"""
from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    # 服务配置
    APP_NAME: str = "ai-inference-service"
    APP_VERSION: str = "1.0.0"
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    DEBUG: bool = False

    # ComfyUI 配置
    COMFYUI_BASE_URL: str = "http://127.0.0.1:8188"
    COMFYUI_WS_URL: str = "ws://127.0.0.1:8188/ws"
    COMFYUI_TIMEOUT: int = 300  # 生成超时(秒)

    # RocketMQ 配置
    ROCKETMQ_NAME_SERVER: str = "127.0.0.1:9876"
    ROCKETMQ_CONSUMER_GROUP: str = "ai-inference-consumer-group"
    ROCKETMQ_TOPIC: str = "ai-gen-task-topic"
    ROCKETMQ_TAG: str = "gen_task"

    # MinIO 配置
    MINIO_ENDPOINT: str = "127.0.0.1:9000"
    MINIO_ACCESS_KEY: str = "minioadmin"
    MINIO_SECRET_KEY: str = "minioadmin123"
    MINIO_BUCKET: str = "ai-results"
    MINIO_SECURE: bool = False

    # 回调配置
    CALLBACK_TOPIC: str = "ai-gen-result-topic"
    CALLBACK_TAG: str = "gen_result"

    # 日志
    LOG_LEVEL: str = "INFO"
    LOG_FORMAT: str = "json"

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


settings = Settings()
