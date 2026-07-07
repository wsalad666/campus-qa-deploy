-- resource表新增resource_type字段迁移
ALTER TABLE resource
    ADD COLUMN IF NOT EXISTS resource_type INT DEFAULT NULL COMMENT '资源类型(0=试卷, 1=习题, 2=笔记, 3=课件)';