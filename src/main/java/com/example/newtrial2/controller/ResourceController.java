package com.example.newtrial2.controller;

import com.example.newtrial2.common.Result;
import com.example.newtrial2.dto.response.PageResult;
import com.example.newtrial2.dto.response.ResourceVO;
import com.example.newtrial2.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "课程资源模块", description = "课件上传、检索、下载")
@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Operation(summary = "上传课件/习题资源")
    @PostMapping("/upload")
    public Result<Void> uploadResource(
            HttpServletRequest httpRequest,
            @Parameter(description = "课程分类ID") @RequestParam Long courseId,
            @Parameter(description = "资源标题") @RequestParam(required = false) String title,
            @Parameter(description = "资源描述") @RequestParam(required = false) String description,
            @Parameter(description = "资源类型(0=试卷, 1=习题, 2=笔记, 3=课件)") @RequestParam(required = false) Integer resourceType,
            @Parameter(description = "文件") @RequestParam MultipartFile file) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        resourceService.uploadResource(userId, courseId, title, description, file, resourceType);
        return Result.success();
    }

    @Operation(summary = "资源分页检索")
    @GetMapping("/list")
    public Result<PageResult<ResourceVO>> pageResources(
            @Parameter(description = "课程分类ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "资源类型(0=试卷, 1=习题, 2=笔记, 3=课件)") @RequestParam(required = false) Integer resourceType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "用户ID过滤") @RequestParam(required = false) Long userId) {
        return Result.success(resourceService.pageResources(courseId, pageNum, pageSize, keyword, resourceType, userId));
    }

    @Operation(summary = "资源下载")
    @GetMapping("/download/{resourceId}")
    public ResponseEntity<byte[]> downloadResource(
            HttpServletRequest httpRequest,
            @Parameter(description = "资源ID") @PathVariable Long resourceId) {
        String[] fileName = new String[1];
        byte[] data = resourceService.downloadResource(resourceId, fileName);
        String encodedFileName = URLEncoder.encode(fileName[0], StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @Operation(summary = "查询我上传的所有资源")
    @GetMapping("/my")
    public Result<PageResult<ResourceVO>> getMyResources(
            HttpServletRequest httpRequest,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(resourceService.getMyResources(userId, pageNum, pageSize));
    }

    @Operation(summary = "删除资源（学生自主删除）")
    @DeleteMapping("/{resourceId}")
    public Result<Void> deleteResource(
            HttpServletRequest httpRequest,
            @Parameter(description = "资源ID") @PathVariable Long resourceId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        resourceService.deleteResource(userId, resourceId);
        return Result.success();
    }
}