package com.skynet.common.core.result;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILURE(500, "操作失败"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(400, "参数校验失败"),
    TOKEN_EXPIRED(40101, "Token已过期"),
    TOKEN_INVALID(40102, "Token无效"),
    USER_NOT_EXIST(10001, "用户不存在"),
    PASSWORD_ERROR(10002, "密码错误"),
    USER_DISABLED(10003, "账号已被禁用"),
    PRODUCT_NOT_EXIST(20001, "商品不存在"),
    WORKFLOW_NOT_EXIST(30001, "工作流模板不存在"),
    TASK_NOT_EXIST(40001, "任务不存在"),
    TASK_STATUS_ERROR(40002, "任务状态不允许此操作"),
    AI_GENERATION_FAILED(50001, "AI生成失败"),
    FILE_UPLOAD_FAILED(50002, "文件上传失败");

    private final int code;
    private final String message;
}
