"""SkyNet AI Gateway - AI统一接入网关 (Python FastAPI)

任务路由与负载均衡、统一鉴权与流式响应(WebSocket/SSE支持)
"""
from fastapi import FastAPI

app = FastAPI(title="SkyNet AI Gateway", version="1.0.0")


@app.get("/health")
async def health():
    return {"status": "ok", "service": "sky-net-ai-gateway"}
