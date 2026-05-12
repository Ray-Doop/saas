"""回调Java服务 - 通过RocketMQ发送任务结果"""
import json
import logging
from typing import Optional, List, Dict, Any
from rocketmq.client import Producer, Message
from config.settings import settings

logger = logging.getLogger(__name__)


async def send_result_callback(
    task_code: str,
    status: str,
    images: Optional[List[Dict[str, Any]]] = None,
    error_message: Optional[str] = None
):
    """发送任务结果回调到 Java 服务"""
    producer = Producer("ai-result-producer-group")
    producer.set_name_server_address(settings.ROCKETMQ_NAME_SERVER)

    try:
        producer.start()

        payload = {
            "taskCode": task_code,
            "status": status,
            "completedAt": __import__("datetime").datetime.now().isoformat()
        }

        if images:
            payload["images"] = images
        if error_message:
            payload["errorMessage"] = error_message

        msg = Message(settings.CALLBACK_TOPIC)
        msg.set_keys(task_code)
        msg.set_tags(settings.CALLBACK_TAG)
        msg.set_body(json.dumps(payload, ensure_ascii=False).encode("utf-8"))

        result = producer.send_sync(msg)
        logger.info(
            f"回调发送成功: taskCode={task_code}, status={status}, msgId={result.msg_id}"
        )

    except Exception as e:
        logger.error(f"回调发送失败: taskCode={task_code}, error={e}")
    finally:
        producer.shutdown()
