package com.example.newtrial2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "课程分类请求")
public class CourseRequest {

    @NotBlank(message = "课程名称不能为空")
    @Schema(description = "课程名称", example = "Java程序设计")
    private String name;

    @Schema(description = "课程描述", example = "Java基础与进阶")
    private String description;

    @Schema(description = "图标URL", example = "https://example.com/icon.png")
    private String icon;

    @Schema(description = "父级分类ID(0表示顶级)", example = "0")
    private Long parentId;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;
}