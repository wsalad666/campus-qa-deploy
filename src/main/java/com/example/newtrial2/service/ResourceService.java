package com.example.newtrial2.service;

import com.example.newtrial2.dto.response.PageResult;
import com.example.newtrial2.dto.response.ResourceVO;
import org.springframework.web.multipart.MultipartFile;

public interface ResourceService {

    void uploadResource(Long userId, Long courseId, String title, String description, MultipartFile file, Integer resourceType);

    PageResult<ResourceVO> pageResources(Long courseId, Integer pageNum, Integer pageSize, String keyword, Integer resourceType, Long userId);

    PageResult<ResourceVO> getMyResources(Long userId, Integer pageNum, Integer pageSize);

    byte[] downloadResource(Long resourceId, String[] fileName);

    void deleteResource(Long userId, Long resourceId);
}