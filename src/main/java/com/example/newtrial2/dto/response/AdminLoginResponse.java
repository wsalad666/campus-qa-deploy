package com.example.newtrial2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "管理员登录响应")
public class AdminLoginResponse {

    @Schema(description = "管理员Token")
    private String token;

    @Schema(description = "管理员ID")
    private Long adminId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "关联的用户ID（用于前台个人主页）")
    private Long userId;
}