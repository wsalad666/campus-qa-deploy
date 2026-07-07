package com.example.newtrial2.file;

import com.example.newtrial2.common.Result;
import com.example.newtrial2.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "文件上传", description = "文件上传相关接口")
@RestController
@RequestMapping("/api/upload")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "通用图片上传", description = "上传图片文件，返回图片访问路径")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 允许的图片类型
            String[] allowedTypes = {"image/jpeg", "image/png"};
            // 最大文件大小 5MB （这里可以根据实际需求调整）
            long maxSize = 5 * 1024 * 1024;
            String filePath = fileService.uploadFile(file, allowedTypes, maxSize);
            return Result.success("图片上传成功", filePath);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
