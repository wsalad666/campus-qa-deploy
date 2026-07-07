package com.example.newtrial2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {
    @NotNull(message = "举报目标类型不能为空")
    private Integer targetType;
    @NotNull(message = "举报目标ID不能为空")
    private Long targetId;
    @NotBlank(message = "举报原因不能为空")
    private String reason;
}