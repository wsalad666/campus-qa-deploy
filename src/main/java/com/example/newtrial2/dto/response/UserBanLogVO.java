package com.example.newtrial2.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserBanLogVO {
    private Long id;
    private Long userId;
    private String userNickname;
    private Long adminId;
    private String adminNickname;
    private Integer banType;
    private String banReason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime banStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime banEndTime;
    private Integer sourceType;
    private Long sourceId;
    private Integer isActive;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}