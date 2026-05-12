"""
RocketMQ 消费者 - 监听AI生成任务消息
"""
import asyncio
import json
import logging
import threading
from typing import Optional
from rocketmq.client import PushConsumer, ConsumeStatus

from config.settings import settings
from app.core.engine import process_task

logger = logging.getLogger(__name__)


class TaskMQConsumer:
    """RocketMQ 消费者, 监听绘图任务并异步处理"""

    def __init__(self):
        self.consumer: Optional[PushConsumer] = None
        self._running = False
        self._thread: Optional[threading.Thread] = None

    def start(self):
        """启动消费者(在独立线程中运行)"""
        self._running = True
        self._thread = threading.Thread(target=self._run_consumer, daemon=True)
        self._thread.start()
        logger.info("RocketMQ消费者已启动")

    def stop(self):
        """停止消费者"""
        self._running = False
        if self.consumer:
            self.consumer.shutdown()
        logger.info("RocketMQ消费者已停止")

    def _run_consumer(self):
        """消费者主循环"""
        consumer = PushConsumer(settings.ROCKETMQ_CONSUMER_GROUP)
        consumer.set_name_server_address(settings.ROCKETMQ_NAME_SERVER)
        consumer.subscribe(
            settings.ROCKETMQ_TOPIC,
            self._on_message
        )
        self.consumer = consumer

        try:
            consumer.start()
            logger.info(f"开始监听: topic={settings.ROCKETMQ_TOPIC}, tag={settings.ROCKETMQ_TAG}")

            while self._running:
                import time
                time.sleep(1)

        except Exception as e:
            logger.error(f"RocketMQ消费者异常: {e}")
        finally:
            consumer.shutdown()

    def _on_message(self, msg) -> ConsumeStatus:
        """消息处理回调"""
        try:
            body = json.loads(msg.body.decode("utf-8"))
            logger.info(f"收到任务消息: taskCode={body.get('taskCode')}")

            # 异步处理任务
            asyncio.run(process_task(body))

            return ConsumeStatus.CONSUME_SUCCESS
        except Exception as e:
            logger.error(f"处理消息失败: {e}")
            return ConsumeStatus.RECONSUME_LATER


# 全局消费者实例
mq_consumer = TaskMQConsumer()
