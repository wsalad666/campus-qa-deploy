package com.example.newtrial2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "课程简要信息")
public class CourseVO {

    @Schema(description = "课程ID")
    private Long id;

    @Schema(description = "课程名称")
    private String name;

    @Schema(description = "课程描述")
    private String description;

    @Schema(description = "图标URL")
    private String icon;

    @Schema(description = "排序序号")
    private Integer sortOrder;
}