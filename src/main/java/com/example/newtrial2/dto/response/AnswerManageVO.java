package com.example.newtrial2.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnswerManageVO {
    private Long id;
    private Long questionId;
    private String questionTitle;
    private Long userId;
    private String userNickname;
    private String content;
    private Integer likeCount;
    private Integer commentCount;
    private Integer isAccepted;
    private Integer isOffline;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}