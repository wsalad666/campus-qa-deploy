package com.example.newtrial2.dto.response;

import lombok.Data;

@Data
public class SimilarQuestionVO {
    private Long id;
    private String title;
    private Integer viewCount;
    private Integer answerCount;
    private Double similarity;  // 相似度百分比 0-100
}