package com.skynet.fulfillment.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * RocketMQ生产者 - 发送AI绘图任务到Python推理服务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "ai-gen-task-topic";
    private static final String TAG = "gen_task";

    /**
     * 发送AI绘图任务消息
     * @param taskCode 任务编号
     * @param workflowJson 工作流JSON(已完成变量替换)
     * @param productImage 商品图片URL
     * @param extraParams 额外参数
     */
    public void sendGenTask(String taskCode, String workflowJson,
                            String productImage, Map<String, Object> extraParams) {
        try {
            Map<String, Object> message = new java.util.HashMap<>();
            message.put("taskCode", taskCode);
            message.put("workflowJson", workflowJson);
            message.put("productImage", productImage);
            if (extraParams != null) {
                message.putAll(extraParams);
            }

            String payload = objectMapper.writeValueAsString(message);

            rocketMQTemplate.syncSend(TOPIC + ":" + TAG,
                    MessageBuilder.withPayload(payload).build());

            log.info("发送AI任务消息成功: taskCode={}", taskCode);
        } catch (JsonProcessingException e) {
            log.error("序列化任务消息失败: taskCode={}", taskCode, e);
            throw new RuntimeException("发送任务消息失败", e);
        }
    }
}
