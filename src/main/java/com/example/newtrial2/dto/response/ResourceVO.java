package com.example.newtrial2.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "资源列表项")
public class ResourceVO {

    @Schema(description = "资源ID")
    private Long id;

    @Schema(description = "课程分类ID")
    private Long courseId;

    @Schema(description = "课程分类名称")
    private String courseName;

    @Schema(description = "上传者用户ID")
    private Long userId;

    @Schema(description = "上传者昵称")
    private String userNickname;

    @Schema(description = "资源标题")
    private String title;

    @Schema(description = "资源描述")
    private String description;

    @Schema(description = "文件类型(pdf/doc等)")
    private String fileType;

    @Schema(description = "资源类型(0=试卷, 1=习题, 2=笔记, 3=课件)")
    private Integer resourceType;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "下载次数")
    private Integer downloadCount;

    @Schema(description = "是否下架(0正常 1已下架)")
    private Integer isOffline;

    @Schema(description = "当前用户是否已收藏")
    private Boolean isFavorited;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "文件预览/下载地址")
    private String fileUrl;
}