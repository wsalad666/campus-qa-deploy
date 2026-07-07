package com.example.newtrial2.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportVO {
    private Long id;
    private Long reporterId;
    private String reporterNickname;
    private Integer targetType;
    private Long targetId;
    private String targetTitle;
    private String targetContent;
    private String reason;
    private Integer status;
    private Long handlerId;
    private String handlerNickname;
    private String handleNote;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // ====== 完整问答上下文 ======
    private Long questionId;
    private String questionTitle;
    private String questionContent;
    private String questionUserNickname;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime questionCreateTime;
    private List<AnswerContextVO> answers;
    private Long reportedAnswerId; // targetType=2时，被举报的具体回答ID

    @Data
    public static class AnswerContextVO {
        private Long id;
        private Long userId;
        private String userNickname;
        private String userAvatar;
        private String content;
        private Integer likeCount;
        private Integer commentCount;
        private Integer isAccepted;
        private Integer isOffline;
        private Boolean isReported; // 是否被举报的这条
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }
}