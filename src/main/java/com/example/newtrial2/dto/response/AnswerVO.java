package com.example.newtrial2.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "回答响应")
public class AnswerVO {

    @Schema(description = "回答ID")
    private Long id;

    @Schema(description = "问题ID")
    private Long questionId;

    @Schema(description = "回答者用户ID")
    private Long userId;

    @Schema(description = "回答者昵称")
    private String userNickname;

    @Schema(description = "回答者头像")
    private String userAvatar;

    @Schema(description = "回答内容")
    private String content;

    @Schema(description = "点赞数量")
    private Integer likeCount;

    @Schema(description = "评论数量")
    private Integer commentCount;

    @Schema(description = "是否采纳")
    private Integer isAccepted;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "当前用户是否已点赞")
    private Boolean isLiked;

    @Schema(description = "图片路径列表")
    private List<String> imageUrls;

    @Schema(description = "评论列表")
    private List<CommentVO> comments;
}