package com.example.newtrial2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "个人主页响应")
public class UserProfileResponse {

    @Schema(description = "用户信息")
    private UserVO user;

    @Schema(description = "提问总数")
    private Long questionCount;

    @Schema(description = "上传资源总数")
    private Long resourceCount;

    @Schema(description = "粉丝数")
    private Long fansCount;

    @Schema(description = "关注数")
    private Long followCount;

    @Schema(description = "当前用户是否已关注")
    private Boolean isFollowed;
}