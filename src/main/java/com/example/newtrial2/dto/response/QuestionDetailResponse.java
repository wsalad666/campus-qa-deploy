package com.example.newtrial2.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "问题详情响应")
public class QuestionDetailResponse {

    @Schema(description = "问题ID")
    private Long id;

    @Schema(description = "提问者用户ID")
    private Long userId;

    @Schema(description = "提问者昵称")
    private String userNickname;

    @Schema(description = "提问者头像")
    private String userAvatar;

    @Schema(description = "课程分类ID")
    private Long courseId;

    @Schema(description = "课程分类名称")
    private String courseName;

    @Schema(description = "问题标题")
    private String title;

    @Schema(description = "问题内容")
    private String content;

    @Schema(description = "状态(0未解决 1已解决)")
    private Integer status;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "回答数量")
    private Integer answerCount;

    @Schema(description = "点赞数量")
    private Integer likeCount;

    @Schema(description = "收藏数量")
    private Integer favoriteCount;

    @Schema(description = "当前用户是否已点赞")
    private Boolean isLiked;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "当前用户是否已收藏")
    private Boolean isFavorited;

    @Schema(description = "已采纳的回答ID")
    private Long adoptAnswerId;

    @Schema(description = "图片路径列表")
    private List<String> imageUrls;

    @Schema(description = "回答列表")
    private List<AnswerVO> answers;
}