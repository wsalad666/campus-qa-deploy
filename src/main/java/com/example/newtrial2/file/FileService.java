package com.example.newtrial2.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    /**
     * 通用文件上传方法
     * @param file 上传的文件
     * @param allowedTypes 允许的文件类型，例如 "image/jpeg", "image/png"
     * @param maxSize 允许的最大文件大小（字节）
     * @return 文件在服务器上的相对路径
     * @throws IOException 文件操作异常
     */
    String uploadFile(MultipartFile file, String[] allowedTypes, long maxSize) throws IOException;

    /**
     * 删除文件
     * @param filePath 文件的相对路径
     * @return 是否成功删除
     */
    boolean deleteFile(String filePath);
}
