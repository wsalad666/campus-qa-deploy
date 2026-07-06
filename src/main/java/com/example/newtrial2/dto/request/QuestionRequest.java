package com.example.newtrial2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发布提问请求")
public class QuestionRequest {

    @NotNull(message = "课程分类ID不能为空")
    @Schema(description = "课程分类ID", example = "1")
    private Long courseId;

    @NotBlank(message = "问题标题不能为空")
    @Schema(description = "问题标题", example = "Java中HashMap的底层原理是什么？")
    private String title;

    @NotBlank(message = "问题内容不能为空")
    @Schema(description = "问题内容(富文本)", example = "我想了解HashMap的put方法...")
    private String content;

    @Schema(description = "图片访问路径列表", example = "[\"/uploads/xxx.png\", \"/uploads/yyy.png\"]")
    private List<String> imageUrls;
}