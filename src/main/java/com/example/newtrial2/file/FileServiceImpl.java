package com.example.newtrial2.file;

import com.example.newtrial2.exception.BusinessException;
import com.example.newtrial2.file.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Value("${file.upload.path}")
    private String uploadDir;

    @Override
    public String uploadFile(MultipartFile file, String[] allowedTypes, long maxSize) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 文件大小校验
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小超出限制，最大允许 " + (maxSize / (1024 * 1024)) + "MB");
        }

        // 文件类型校验
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(allowedTypes).contains(contentType)) {
            throw new BusinessException("文件类型不支持，只允许 " + String.join(", ", allowedTypes) + " 格式");
        }

        // 获取文件后缀名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + fileExtension;
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = uploadPath.resolve(fileName);

        // 创建上传目录（如果不存在）
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 保存文件
        Files.copy(file.getInputStream(), filePath);

        // 返回相对路径，以便前端或数据库存储
        return "/uploads/" + fileName; // 注意：这里返回的路径是相对于应用的，不是物理路径
    }

    @Override
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty() || !filePath.startsWith("/uploads/")) {
            return false; // 非法路径或不是本地上传的文件
        }
        Path fileToDelete = Paths.get(uploadDir, filePath.substring("/uploads/".length())).toAbsolutePath().normalize();
        try {
            Files.deleteIfExists(fileToDelete);
            logger.info("文件 {} 已删除", fileToDelete);
            return true;
        } catch (IOException e) {
            logger.error("删除文件 {} 失败", fileToDelete, e);
            return false;
        }
    }
}

