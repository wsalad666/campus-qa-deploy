package com.example.newtrial2.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_ban_log")
public class UserBanLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long adminId;
    private Integer banType;
    private String banReason;
    private LocalDateTime banStartTime;
    private LocalDateTime banEndTime;
    private Integer sourceType;
    private Long sourceId;
    private Integer isActive;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}