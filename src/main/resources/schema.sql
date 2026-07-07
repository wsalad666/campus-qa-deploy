 -- ============================================
-- 校园问答数据库建表脚本（已合并所有迁移）
-- 用法：
--   1. CREATE DATABASE campus_qa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
--   2. mysql -u root -p"密码" campus_qa < schema.sql
--   3. mysql -u root -p"密码" campus_qa < data.sql
-- ============================================

CREATE DATABASE IF NOT EXISTS campus_qa DEFAULT CHARSET utf8mb4;

USE campus_qa;

-- 学生用户表
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    student_no VARCHAR(32) NOT NULL COMMENT '学号',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password VARCHAR(256) NOT NULL COMMENT '密码(加密存储)',
    nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    signature VARCHAR(256) DEFAULT NULL COMMENT '个性签名',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0禁用 1正常)',
    ban_end_time DATETIME DEFAULT NULL COMMENT '禁言截止时间',
    ban_reason VARCHAR(512) DEFAULT NULL COMMENT '当前禁言原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_offline TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑下架(0正常 1违规隐藏)',
    UNIQUE KEY uk_student_no (student_no),
    UNIQUE KEY uk_username (username),
    KEY idx_phone (phone),
    KEY idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生用户表';

-- 管理员表
CREATE TABLE admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '管理员ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password VARCHAR(256) NOT NULL COMMENT '密码(加密存储)',
    nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    role TINYINT NOT NULL DEFAULT 1 COMMENT '角色(1普通管理员 2超级管理员)',
    user_id BIGINT DEFAULT NULL COMMENT '关联的user表ID，用于前台个人主页',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_offline TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑下架(0正常 1违规隐藏)',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 课程分类表
CREATE TABLE course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '课程分类ID',
    name VARCHAR(64) NOT NULL COMMENT '课程名称',
    description VARCHAR(512) DEFAULT NULL COMMENT '课程描述',
    icon VARCHAR(512) DEFAULT NULL COMMENT '图标URL',
    parent_id BIGINT DEFAULT 0 COMMENT '父级分类ID(0表示顶级分类)',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_offline TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑下架(0正常 1违规隐藏)',
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程分类表';

-- 用户关注关系表
CREATE TABLE follow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关注记录ID',
    follower_id BIGINT NOT NULL COMMENT '关注者用户ID',
    followed_id BIGINT NOT NULL COMMENT '被关注者用户ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_offline TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑下架(0正常 1违规隐藏)',
    UNIQUE KEY uk_follower_followed (follower_id, followed_id),
    KEY idx_followed_id (followed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';

-- 收藏记录表
CREATE TABLE favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_id BIGINT NOT NULL COMMENT '收藏目标ID',
    type TINYINT NOT NULL COMMENT '收藏类型(1问题 2资源)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_offline TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑下架(0正常 1违规隐藏)',
    UNIQUE KEY uk_user_target_type (user_id, target_id, type),
    KEY idx_target_id (target_id),
    KEY idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏记录表';

-- 提问表
CREATE TABLE question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '提问ID',
    user_id BIGINT NOT NULL COMMENT '提问者用户ID',
    course_id BIGINT NOT NULL COMMENT '所属课程分类ID',
    title VARCHAR(256) NOT NULL COMMENT '问题标题',
    content TEXT NOT NULL COMMENT '问题内容',
    image_urls JSON DEFAULT NULL COMMENT '图片路径列表(JSON数组)',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0未解决 1已解决)',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    answer_count INT DEFAULT 0 COMMENT '回答数量',
    adopt_answer_id BIGINT DEFAULT NULL COMMENT '已采纳的回答ID',
    like_count INT DEFAULT 0 COMMENT '点赞数量',
    favorite_count INT DEFAULT 0 COMMENT '收藏数量',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_offline TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑下架(0正常 1违规隐藏)',
    KEY idx_user_id (user_id),
    KEY idx_course_id (course_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提问表';

-- 回答表
CREATE TABLE answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '回答ID',
    question_id BIGINT NOT NULL COMMENT '所属提问ID',
    user_id BIGINT NOT NULL COMMENT '回答者用户ID',
    content TEXT NOT NULL COMMENT '回答内容',
    image_urls JSON DEFAULT NULL COMMENT '图片路径列表(JSON数组)',
    like_count INT DEFAULT 0 COMMENT '点赞数量',
    comment_count INT DEFAULT 0 COMMENT '评论数量',
    is_accepted TINYINT NOT NULL DEFAULT 0 COMMENT '是否采纳(0未采纳 1已采纳)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_offline TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑下架(0正常 1违规隐藏)',
    KEY idx_question_id (question_id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回答表';

-- 评论/层级回复表
CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    answer_id BIGINT NOT NULL COMMENT '所属回答ID',
    user_id BIGINT NOT NULL COMMENT '评论者用户ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父级评论ID(0表示一级评论)',
    reply_to_id BIGINT DEFAULT 0 COMMENT '回复目标用户ID(0表示一级评论)',
    content VARCHAR(1024) NOT NULL COMMENT '评论内容',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_offline TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑下架(0正常 1违规隐藏)',
    KEY idx_answer_id (answer_id),
    KEY idx_user_id (user_id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论/层级回复表';

-- 回答点赞表
CREATE TABLE question_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞记录ID',
    user_id BIGINT NOT NULL COMMENT '点赞用户ID',
    answer_id BIGINT DEFAULT NULL COMMENT '被点赞回答ID',
    question_id BIGINT DEFAULT NULL COMMENT '被点赞问题ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_offline TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑下架(0正常 1违规隐藏)',
    UNIQUE KEY uk_user_answer (user_id, answer_id),
    UNIQUE KEY uk_user_question (user_id, question_id),
    KEY idx_answer_id (answer_id),
    KEY idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表(回答/问题)';

-- 课程课件资源表
CREATE TABLE resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '资源ID',
    course_id BIGINT NOT NULL COMMENT '所属课程分类ID',
    user_id BIGINT NOT NULL COMMENT '上传者用户ID',
    title VARCHAR(256) NOT NULL COMMENT '资源标题',
    description VARCHAR(1024) DEFAULT NULL COMMENT '资源描述',
    file_url VARCHAR(512) NOT NULL COMMENT '文件路径',
    file_type VARCHAR(32) DEFAULT NULL COMMENT '文件类型(pdf/ppt/video等)',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    resource_type INT DEFAULT NULL COMMENT '资源类型(0=试卷, 1=习题, 2=笔记, 3=课件)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_offline TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑下架(0正常 1违规隐藏)',
    KEY idx_course_id (course_id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程课件资源表';

-- 举报工单表
CREATE TABLE report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '举报ID',
    reporter_id BIGINT NOT NULL COMMENT '举报人用户ID',
    target_type TINYINT NOT NULL COMMENT '举报目标类型(1提问 2回答)',
    target_id BIGINT NOT NULL COMMENT '举报目标ID',
    reason VARCHAR(512) NOT NULL COMMENT '举报原因',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态(0待处理 1已处理-下架 2已驳回)',
    handler_id BIGINT DEFAULT NULL COMMENT '处理人管理员ID',
    handle_note VARCHAR(512) DEFAULT NULL COMMENT '处理备注',
    handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_reporter_id (reporter_id),
    KEY idx_target (target_type, target_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报工单表';

-- 用户处罚日志表
CREATE TABLE user_ban_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '被处罚用户ID',
    admin_id BIGINT NOT NULL COMMENT '操作管理员ID',
    ban_type TINYINT NOT NULL COMMENT '禁言类型(1轻度3天 2中度7天 3重度永久)',
    ban_reason VARCHAR(512) NOT NULL COMMENT '禁言原因',
    ban_start_time DATETIME NOT NULL COMMENT '禁言开始时间',
    ban_end_time DATETIME DEFAULT NULL COMMENT '禁言结束时间(NULL表示永久)',
    source_type TINYINT DEFAULT NULL COMMENT '违规来源类型(1举报 2管理员巡查)',
    source_id BIGINT DEFAULT NULL COMMENT '来源ID(举报ID或问题/回答ID)',
    is_active TINYINT NOT NULL DEFAULT 1 COMMENT '是否生效(1生效 0已解除)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_admin_id (admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户处罚日志表';

-- 站内通知表
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(256) NOT NULL COMMENT '通知标题',
    content VARCHAR(1024) NOT NULL COMMENT '通知内容',
    type TINYINT NOT NULL DEFAULT 0 COMMENT '通知类型(0点赞提问 1点赞回答 2评论回复 3回答被采纳 4被关注 5内容下架 6禁言通知 7举报反馈)',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读(0未读 1已读)',
    link_type TINYINT DEFAULT NULL COMMENT '跳转类型(1问题详情 2回答 3用户主页)',
    link_id BIGINT DEFAULT NULL COMMENT '跳转目标ID',
    sender_id BIGINT DEFAULT NULL COMMENT '触发用户ID',
    sender_nickname VARCHAR(64) DEFAULT NULL COMMENT '触发用户昵称',
    is_deletable TINYINT NOT NULL DEFAULT 1 COMMENT '是否可删除(0不可删除 1可删除)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_user_read (user_id, is_read),
    KEY idx_user_type (user_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知表';

-- 用户常用课程表
CREATE TABLE user_course_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_course (user_id, course_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户常用课程表';

-- 用户收藏文件夹表
CREATE TABLE user_collect_folder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文件夹ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    folder_name VARCHAR(50) NOT NULL COMMENT '文件夹名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏文件夹表';

-- 用户收藏关联表
CREATE TABLE user_collect_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    folder_id BIGINT NOT NULL COMMENT '文件夹ID',
    target_type TINYINT NOT NULL COMMENT '收藏类型(1=提问, 2=资源)',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_folder_target (user_id, folder_id, target_type, target_id),
    INDEX idx_folder_id (folder_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏关联表';