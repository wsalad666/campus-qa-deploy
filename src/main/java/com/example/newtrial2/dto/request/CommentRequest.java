package com.example.newtrial2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "评论请求")
public class CommentRequest {

    @NotNull(message = "回答ID不能为空")
    @Schema(description = "回答ID", example = "1")
    private Long answerId;

    @NotBlank(message = "评论内容不能为空")
    @Schema(description = "评论内容", example = "这个回答很详细，谢谢！")
    private String content;

    @Schema(description = "父级评论ID(0表示一级评论)", example = "0")
    private Long parentId;

    @Schema(description = "回复目标用户ID(0表示一级评论)", example = "0")
    private Long replyToId;
}