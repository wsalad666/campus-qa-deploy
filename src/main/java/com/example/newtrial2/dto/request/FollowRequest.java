package com.example.newtrial2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "关注请求")
public class FollowRequest {

    @NotNull(message = "被关注用户ID不能为空")
    @Schema(description = "被关注用户ID", example = "2")
    private Long followedId;
}