package com.example.newtrial2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "收藏请求")
public class FavoriteRequest {

    @NotNull(message = "收藏目标ID不能为空")
    @Schema(description = "收藏目标ID", example = "1")
    private Long targetId;

    @NotNull(message = "收藏类型不能为空")
    @Schema(description = "收藏类型(1问题 2资源)", example = "1")
    private Integer type;
}