package com.example.newtrial2.service;

import com.example.newtrial2.dto.response.PageResult;
import com.example.newtrial2.dto.response.ResourceVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ResourceService {

    void uploadResource(Long userId, Long courseId, String title, String description, MultipartFile file, Integer resourceType);

    PageResult<ResourceVO> pageResources(List<Long> courseIds, Integer pageNum, Integer pageSize, String keyword, Integer resourceType, Long userId, Long currentUserId);

    PageResult<ResourceVO> getMyResources(Long userId, Integer pageNum, Integer pageSize);

    byte[] downloadResource(Long resourceId, String[] fileName);

    void deleteResource(Long userId, Long resourceId);
    ResourceVO getResourceDetail(Long resourceId, Long currentUserId);

}
