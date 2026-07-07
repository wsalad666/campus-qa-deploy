package com.example.newtrial2.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CollectItemVO {
    private Long relationId;
    private Long targetId;
    private Integer targetType;
    private String title;
    private String description;
    private String courseName;
    private String userNickname;
    private LocalDateTime createTime;
}