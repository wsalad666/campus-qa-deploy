package com.example.newtrial2.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer type;
    private Integer isRead;
    private Integer linkType;
    private Long linkId;
    private Long senderId;
    private String senderNickname;
    private Integer isDeletable;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}