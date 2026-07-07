package com.example.newtrial2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "answer", autoResultMap = true)
public class Answer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private Long userId;

    private String content;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> imageUrls;

    private Integer likeCount = 0;

    private Integer commentCount = 0;

    private Integer isAccepted = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer isOffline = 0;
}