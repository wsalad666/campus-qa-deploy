package com.example.newtrial2.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_collect_relation")
public class UserCollectRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long folderId;
    private Integer targetType; // 1=提问, 2=资源
    private Long targetId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}