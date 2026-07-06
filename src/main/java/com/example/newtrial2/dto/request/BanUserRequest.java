package com.example.newtrial2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BanUserRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    @NotNull(message = "禁言类型不能为空")
    private Integer banType;
    @NotBlank(message = "禁言原因不能为空")
    private String banReason;
    private Integer sourceType;
    private Long sourceId;
}