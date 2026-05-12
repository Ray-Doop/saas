-- ============================================================
-- 智绘云 - 数据库初始化脚本 (MVP Phase 1)
-- ============================================================

CREATE DATABASE IF NOT EXISTS `smart_cloud` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `nacos_config` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `smart_cloud`;

-- ==================== 用户表 ====================
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(256) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否, 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 默认管理员账号 admin / admin123
INSERT INTO `t_user` (`username`, `password`, `nickname`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '超级管理员', 1);

-- ==================== 商品SPU表 ====================
DROP TABLE IF EXISTS `t_product_spu`;
CREATE TABLE `t_product_spu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'SPU ID',
    `spu_code` VARCHAR(64) NOT NULL COMMENT 'SPU编码',
    `spu_name` VARCHAR(256) NOT NULL COMMENT '商品名称',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `brand` VARCHAR(128) DEFAULT NULL COMMENT '品牌',
    `description` TEXT COMMENT '商品描述',
    `main_image` VARCHAR(512) DEFAULT NULL COMMENT '主图URL',
    `image_list` JSON DEFAULT NULL COMMENT '图片列表JSON',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spu_code` (`spu_code`),
    KEY `idx_category` (`category_id`),
    KEY `idx_spu_name` (`spu_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SPU表';

-- ==================== 商品SKU表 ====================
DROP TABLE IF EXISTS `t_product_sku`;
CREATE TABLE `t_product_sku` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
    `spu_id` BIGINT NOT NULL COMMENT '所属SPU ID',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(256) NOT NULL COMMENT 'SKU名称',
    `specs` JSON DEFAULT NULL COMMENT '规格属性JSON(如: {"颜色":"红色","尺码":"XL"})',
    `price` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '销售价格(分)',
    `cost_price` DECIMAL(12,2) DEFAULT NULL COMMENT '成本价格',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    `image` VARCHAR(512) DEFAULT NULL COMMENT 'SKU图片',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_code` (`sku_code`),
    KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';

-- ==================== 工作流模板表 ====================
DROP TABLE IF EXISTS `t_workflow`;
CREATE TABLE `t_workflow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '工作流ID',
    `workflow_name` VARCHAR(256) NOT NULL COMMENT '工作流名称',
    `description` VARCHAR(1024) DEFAULT NULL COMMENT '描述',
    `category` VARCHAR(64) DEFAULT NULL COMMENT '分类: poster-海报, model-模特图, scene-场景图',
    `workflow_json` LONGTEXT NOT NULL COMMENT 'ComfyUI工作流JSON(含变量占位符如{{product_image}})',
    `preview_image` VARCHAR(512) DEFAULT NULL COMMENT '预览图URL',
    `node_count` INT DEFAULT 0 COMMENT '节点数量',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_name` (`workflow_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流模板表';

-- ==================== AI生成任务表 ====================
DROP TABLE IF EXISTS `t_gen_task`;
CREATE TABLE `t_gen_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `task_code` VARCHAR(64) NOT NULL COMMENT '任务编号',
    `workflow_id` BIGINT NOT NULL COMMENT '关联工作流ID',
    `spu_id` BIGINT DEFAULT NULL COMMENT '关联商品SPU ID',
    `sku_id` BIGINT DEFAULT NULL COMMENT '关联商品SKU ID',
    `product_image` VARCHAR(512) DEFAULT NULL COMMENT '商品原图URL',
    `workflow_snapshot` LONGTEXT NOT NULL COMMENT '工作流快照(已替换变量后的最终JSON)',
    `input_params` JSON DEFAULT NULL COMMENT '输入参数JSON(变量映射)',
    `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态: PENDING-待处理, PROCESSING-生成中, SUCCESS-成功, FAILED-失败',
    `result_images` JSON DEFAULT NULL COMMENT '结果图片列表JSON',
    `progress` INT DEFAULT 0 COMMENT '进度 0-100',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `cost_points` INT DEFAULT 0 COMMENT '消耗积分',
    `comfyui_prompt_id` VARCHAR(128) DEFAULT NULL COMMENT 'ComfyUI返回的prompt_id',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始处理时间',
    `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_code` (`task_code`),
    KEY `idx_workflow_id` (`workflow_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成任务表';

-- ==================== 生成结果表 ====================
DROP TABLE IF EXISTS `t_gen_result`;
CREATE TABLE `t_gen_result` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '结果ID',
    `task_id` BIGINT NOT NULL COMMENT '关联任务ID',
    `image_url` VARCHAR(512) NOT NULL COMMENT '生成图片URL',
    `image_index` INT DEFAULT 0 COMMENT '图片序号(一次任务可能生成多张)',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
    `width` INT DEFAULT NULL COMMENT '图片宽度',
    `height` INT DEFAULT NULL COMMENT '图片高度',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生成结果表';
