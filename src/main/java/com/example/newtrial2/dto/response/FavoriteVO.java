package com.example.newtrial2.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "收藏列表项")
public class FavoriteVO {

    @Schema(description = "收藏记录ID")
    private Long id;

    @Schema(description = "收藏目标ID")
    private Long targetId;

    @Schema(description = "收藏类型(1问题 2资源)")
    private Integer type;

    @Schema(description = "收藏目标标题")
    private String title;

    @Schema(description = "收藏目标描述")
    private String description;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}