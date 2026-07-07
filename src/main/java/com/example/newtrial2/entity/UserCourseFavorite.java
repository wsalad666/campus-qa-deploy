package com.example.newtrial2.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_course_favorite")
public class UserCourseFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long courseId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}