-- 通知表新增字段迁移脚本
-- 如果通知表已存在，执行以下ALTER TABLE语句

ALTER TABLE notification
    ADD COLUMN IF NOT EXISTS link_type TINYINT DEFAULT NULL COMMENT '跳转类型(1问题详情 2回答 3用户主页)',
    ADD COLUMN IF NOT EXISTS link_id BIGINT DEFAULT NULL COMMENT '跳转目标ID',
    ADD COLUMN IF NOT EXISTS sender_id BIGINT DEFAULT NULL COMMENT '触发用户ID',
    ADD COLUMN IF NOT EXISTS sender_nickname VARCHAR(64) DEFAULT NULL COMMENT '触发用户昵称',
    ADD COLUMN IF NOT EXISTS is_deletable TINYINT NOT NULL DEFAULT 1 COMMENT '是否可删除(0不可删除 1可删除)';

-- 添加索引
ALTER TABLE notification
    ADD INDEX IF NOT EXISTS idx_user_type (user_id, type);

-- 修改type字段默认值
ALTER TABLE notification
    MODIFY COLUMN type TINYINT NOT NULL DEFAULT 0 COMMENT '通知类型(0点赞提问 1点赞回答 2评论回复 3回答被采纳 4被关注 5内容下架 6禁言通知 7举报反馈)';