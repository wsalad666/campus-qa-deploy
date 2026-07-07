package com.example.newtrial2.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("question_like")
public class QuestionLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long answerId;

    private Long questionId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}