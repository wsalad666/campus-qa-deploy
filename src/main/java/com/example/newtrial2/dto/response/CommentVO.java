package com.example.newtrial2.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "评论响应")
public class CommentVO {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "回答ID")
    private Long answerId;

    @Schema(description = "评论者用户ID")
    private Long userId;

    @Schema(description = "评论者昵称")
    private String userNickname;

    @Schema(description = "评论者头像")
    private String userAvatar;

    @Schema(description = "父级评论ID")
    private Long parentId;

    @Schema(description = "回复目标用户ID")
    private Long replyToId;

    @Schema(description = "回复目标用户昵称")
    private String replyToNickname;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "子评论列表")
    private List<CommentVO> children;
}