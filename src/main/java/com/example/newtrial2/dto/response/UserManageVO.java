package com.example.newtrial2.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserManageVO {
    private Long id;
    private String studentNo;
    private String username;
    private String nickname;
    private String avatar;
    private Integer status;
    private Integer isOffline;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime banEndTime;
    private String banReason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}