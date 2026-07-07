package com.example.newtrial2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理后台统计数据")
public class StatisticsResponse {

    @Schema(description = "总注册学生数")
    private Long totalUsers;

    @Schema(description = "今日新增用户数")
    private Long todayNewUsers;

    @Schema(description = "总提问数量")
    private Long totalQuestions;

    @Schema(description = "今日提问数")
    private Long todayQuestions;

    @Schema(description = "总上传资源数量")
    private Long totalResources;

    @Schema(description = "总下载次数")
    private Long totalDownloads;
}