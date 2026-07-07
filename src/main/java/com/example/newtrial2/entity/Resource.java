package com.example.newtrial2.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource")
public class Resource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long userId;

    private String title;

    private String description;

    @TableField(exist = false)
    private String category;

    private String fileUrl;

    private String fileType;

    private Long fileSize;

    private Integer downloadCount = 0;

    /**
     * 资源类型：0=试卷, 1=习题, 2=笔记, 3=课件
     */
    private Integer resourceType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer isOffline = 0;
}
