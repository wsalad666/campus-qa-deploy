package com.example.newtrial2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "回答问题请求")
public class AnswerRequest {

    @NotNull(message = "问题ID不能为空")
    @Schema(description = "问题ID", example = "1")
    private Long questionId;

    @NotBlank(message = "回答内容不能为空")
    @Schema(description = "回答内容(富文本)", example = "HashMap底层采用数组+链表+红黑树实现...")
    private String content;

    @Schema(description = "图片访问路径列表", example = "[\"/uploads/aaa.png\"]")
    private List<String> imageUrls;
}