package com.example.newtrial2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.newtrial2.dto.response.PageResult;
import com.example.newtrial2.dto.response.ResourceVO;
import com.example.newtrial2.entity.Course;
import com.example.newtrial2.entity.Resource;
import com.example.newtrial2.entity.User;
import com.example.newtrial2.exception.BusinessException;
import com.example.newtrial2.exception.ResourceNotFoundException;
import com.example.newtrial2.mapper.CourseMapper;
import com.example.newtrial2.mapper.ResourceMapper;
import com.example.newtrial2.mapper.UserMapper;
import com.example.newtrial2.service.ResourceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ResourceServiceImpl implements ResourceService {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("pdf", "doc", "docx", "jpg", "png"));

    private final ResourceMapper resourceMapper;
    private final UserMapper userMapper;
    private final CourseMapper courseMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

    public ResourceServiceImpl(ResourceMapper resourceMapper, UserMapper userMapper, CourseMapper courseMapper) {
        this.resourceMapper = resourceMapper;
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
    }

    @Override
    @Transactional
    public void uploadResource(Long userId, Long courseId, String title, String description, MultipartFile file, Integer resourceType) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException("文件名不能为空");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException("不支持的文件格式，仅允许: pdf, doc, docx, jpg, png");
        }

        // 保存文件
        String savedFilename = UUID.randomUUID().toString() + "." + ext;
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            Path filePath = uploadDir.resolve(savedFilename);
            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败: " + e.getClass().getSimpleName() + " - " + e.getMessage() + " (uploadDir=" + uploadDir + ")");
        }

        Resource resource = new Resource();
        resource.setUserId(userId);
        resource.setCourseId(courseId);
        resource.setTitle(title != null ? title : originalFilename);
        resource.setDescription(description);
        resource.setFileUrl("/uploads/" + savedFilename);
        resource.setFileType(ext);
        resource.setFileSize(file.getSize());
        resource.setDownloadCount(0);
        resource.setResourceType(resourceType);
        resourceMapper.insert(resource);
    }

    @Override
    public PageResult<ResourceVO> pageResources(Long courseId, Integer pageNum, Integer pageSize, String keyword, Integer resourceType, Long userId) {
        Page<Resource> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null && courseId > 0) {
            wrapper.eq(Resource::getCourseId, courseId);
        }
        if (userId != null) {
            wrapper.eq(Resource::getUserId, userId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Resource::getTitle, keyword.trim());
        }
        if (resourceType != null) {
            wrapper.eq(Resource::getResourceType, resourceType);
        }
        wrapper.eq(Resource::getIsOffline, 0);
        wrapper.orderByDesc(Resource::getCreateTime);
        IPage<Resource> result = resourceMapper.selectPage(page, wrapper);

        List<ResourceVO> records = new ArrayList<>();
        for (Resource r : result.getRecords()) {
            ResourceVO vo = convertToVO(r);
            records.add(vo);
        }
        return new PageResult<>(result.getTotal(), pageNum, pageSize, records);
    }

    @Override
    public PageResult<ResourceVO> getMyResources(Long userId, Integer pageNum, Integer pageSize) {
        Page<Resource> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getUserId, userId)
                .eq(Resource::getIsOffline, 0)
                .orderByDesc(Resource::getCreateTime);
        IPage<Resource> result = resourceMapper.selectPage(page, wrapper);

        List<ResourceVO> records = new ArrayList<>();
        for (Resource r : result.getRecords()) {
            ResourceVO vo = convertToVO(r);
            records.add(vo);
        }
        return new PageResult<>(result.getTotal(), pageNum, pageSize, records);
    }

    @Override
    @Transactional
    public byte[] downloadResource(Long resourceId, String[] fileName) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new ResourceNotFoundException("资源不存在");
        }
        // 累加下载次数
        resource.setDownloadCount((resource.getDownloadCount() == null ? 0 : resource.getDownloadCount()) + 1);
        resourceMapper.updateById(resource);

        try {
            Path filePath = Paths.get(uploadPath, resource.getFileUrl().replace("/uploads/", ""));
            if (fileName != null && fileName.length > 0) {
                fileName[0] = resource.getTitle() + "." + resource.getFileType();
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new BusinessException("文件读取失败");
        }
    }

    private ResourceVO convertToVO(Resource r) {
        ResourceVO vo = new ResourceVO();
        vo.setId(r.getId());
        vo.setCourseId(r.getCourseId());
        vo.setUserId(r.getUserId());
        vo.setTitle(r.getTitle());
        vo.setDescription(r.getDescription());
        vo.setFileType(r.getFileType());
        vo.setResourceType(r.getResourceType());
        vo.setFileSize(r.getFileSize());
        vo.setDownloadCount(r.getDownloadCount());
        vo.setIsOffline(r.getIsOffline());
        vo.setCreateTime(r.getCreateTime());
        vo.setFileUrl(r.getFileUrl());

        User u = userMapper.selectById(r.getUserId());
        if (u != null) {
            vo.setUserNickname(u.getNickname());
        }
        Course c = courseMapper.selectById(r.getCourseId());
        if (c != null) {
            vo.setCourseName(c.getName());
        }
        return vo;
    }

    @Override
    @Transactional
    public void deleteResource(Long userId, Long resourceId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        if (!resource.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人资源");
        }
        // 删除服务器文件
        try {
            Path filePath = Paths.get(uploadPath, resource.getFileUrl().replace("/uploads/", ""));
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 文件删除失败不影响数据库记录删除
        }
        // 删除数据库记录
        resourceMapper.deleteById(resourceId);
    }
}