package com.example.newtrial2.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HandleReportRequest {
    @NotNull(message = "处理结果不能为空")
    private Integer status;
    private String handleNote;
    private Integer banType; // 1轻度3天 2中度7天 3重度永久, null表示不禁言
    private String banReason;
}