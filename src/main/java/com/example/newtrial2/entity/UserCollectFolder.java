package com.example.newtrial2.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_collect_folder")
public class UserCollectFolder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String folderName;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}