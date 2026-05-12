package com.smartcloud.task.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcloud.task.entity.GenResultPO;
import com.smartcloud.task.entity.GenTaskPO;
import com.smartcloud.task.repository.GenResultRepository;
import com.smartcloud.task.repository.GenTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * RocketMQ消费者 - 接收Python AI推理服务的完成回调
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "ai-gen-result-topic",
        consumerGroup = "task-result-consumer-group",
        selectorExpression = "gen_result"
)
public class TaskResultConsumer implements RocketMQListener<String> {

    private final GenTaskRepository taskRepository;
    private final GenResultRepository resultRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void onMessage(String message) {
        log.info("收到AI任务结果回调: {}", message);
        try {
            JsonNode root = objectMapper.readTree(message);
            String taskCode = root.get("taskCode").asText();
            String status = root.get("status").asText();

            Optional<GenTaskPO> optTask = taskRepository.findByTaskCode(taskCode);
            if (optTask.isEmpty()) {
                log.error("任务不存在: taskCode={}", taskCode);
                return;
            }

            GenTaskPO task = optTask.get();

            if ("SUCCESS".equals(status)) {
                task.setStatus("SUCCESS");
                task.setProgress(100);
                task.setFinishTime(LocalDateTime.now());

                JsonNode images = root.get("images");
                if (images != null && images.isArray()) {
                    for (int i = 0; i < images.size(); i++) {
                        JsonNode img = images.get(i);
                        GenResultPO result = new GenResultPO();
                        result.setTaskId(task.getId());
                        result.setImageUrl(img.get("url").asText());
                        result.setImageIndex(i);
                        if (img.has("fileSize")) result.setFileSize(img.get("fileSize").asLong());
                        if (img.has("width")) result.setWidth(img.get("width").asInt());
                        if (img.has("height")) result.setHeight(img.get("height").asInt());
                        resultRepository.save(result);
                    }
                }
            } else {
                task.setStatus("FAILED");
                if (root.has("errorMessage")) {
                    task.setErrorMessage(root.get("errorMessage").asText());
                }
                task.setFinishTime(LocalDateTime.now());
            }

            taskRepository.save(task);
            log.info("任务结果更新完成: taskCode={}, status={}", taskCode, task.getStatus());

        } catch (Exception e) {
            log.error("处理AI任务结果回调异常", e);
        }
    }
}
