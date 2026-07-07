package com.example.newtrial2.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`comment`")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long answerId;

    private Long userId;

    private Long parentId;

    private Long replyToId;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Integer isOffline = 0;
}