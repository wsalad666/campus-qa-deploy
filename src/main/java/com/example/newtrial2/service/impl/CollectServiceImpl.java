package com.example.newtrial2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.newtrial2.dto.response.CollectFolderVO;
import com.example.newtrial2.dto.response.CollectItemVO;
import com.example.newtrial2.entity.*;
import com.example.newtrial2.exception.BusinessException;
import com.example.newtrial2.mapper.*;
import com.example.newtrial2.service.CollectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CollectServiceImpl implements CollectService {

    private final UserCollectFolderMapper folderMapper;
    private final UserCollectRelationMapper relationMapper;
    private final QuestionMapper questionMapper;
    private final ResourceMapper resourceMapper;
    private final UserMapper userMapper;
    private final CourseMapper courseMapper;

    public CollectServiceImpl(UserCollectFolderMapper folderMapper,
                              UserCollectRelationMapper relationMapper,
                              QuestionMapper questionMapper,
                              ResourceMapper resourceMapper,
                              UserMapper userMapper,
                              CourseMapper courseMapper) {
        this.folderMapper = folderMapper;
        this.relationMapper = relationMapper;
        this.questionMapper = questionMapper;
        this.resourceMapper = resourceMapper;
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
    }

    @Override
    public List<CollectFolderVO> getFolders(Long userId) {
        // 确保有默认文件夹
        ensureDefaultFolder(userId);
        List<UserCollectFolder> folders = folderMapper.selectList(
            new LambdaQueryWrapper<UserCollectFolder>().eq(UserCollectFolder::getUserId, userId)
                .orderByAsc(UserCollectFolder::getCreateTime));
        List<CollectFolderVO> vos = new ArrayList<>();
        for (UserCollectFolder f : folders) {
            CollectFolderVO vo = new CollectFolderVO();
            vo.setId(f.getId());
            vo.setFolderName(f.getFolderName());
            vo.setCreateTime(f.getCreateTime());
            Long count = relationMapper.selectCount(
                new LambdaQueryWrapper<UserCollectRelation>().eq(UserCollectRelation::getFolderId, f.getId()));
            vo.setCount(count.intValue());
            vos.add(vo);
        }
        return vos;
    }

    private void ensureDefaultFolder(Long userId) {
        Long count = folderMapper.selectCount(
            new LambdaQueryWrapper<UserCollectFolder>()
                .eq(UserCollectFolder::getUserId, userId)
                .eq(UserCollectFolder::getFolderName, "默认收藏"));
        if (count == 0) {
            UserCollectFolder folder = new UserCollectFolder();
            folder.setUserId(userId);
            folder.setFolderName("默认收藏");
            folderMapper.insert(folder);
        }
    }

    @Override
    public CollectFolderVO createFolder(Long userId, String folderName) {
        if (folderName == null || folderName.trim().isEmpty()) {
            throw new BusinessException("文件夹名称不能为空");
        }
        Long count = folderMapper.selectCount(
            new LambdaQueryWrapper<UserCollectFolder>()
                .eq(UserCollectFolder::getUserId, userId)
                .eq(UserCollectFolder::getFolderName, folderName.trim()));
        if (count > 0) {
            throw new BusinessException("文件夹名称已存在");
        }
        UserCollectFolder folder = new UserCollectFolder();
        folder.setUserId(userId);
        folder.setFolderName(folderName.trim());
        folderMapper.insert(folder);
        CollectFolderVO vo = new CollectFolderVO();
        vo.setId(folder.getId());
        vo.setFolderName(folder.getFolderName());
        vo.setCount(0);
        vo.setCreateTime(folder.getCreateTime());
        return vo;
    }

    @Override
    public void renameFolder(Long userId, Long folderId, String newName) {
        UserCollectFolder folder = folderMapper.selectById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            throw new BusinessException("文件夹不存在");
        }
        folder.setFolderName(newName.trim());
        folderMapper.updateById(folder);
    }

    @Override
    @Transactional
    public void deleteFolder(Long userId, Long folderId) {
        UserCollectFolder folder = folderMapper.selectById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            throw new BusinessException("文件夹不存在");
        }
        if ("默认收藏".equals(folder.getFolderName())) {
            throw new BusinessException("默认收藏文件夹不可删除");
        }
        // 删除文件夹内的所有收藏关联
        relationMapper.delete(
            new LambdaQueryWrapper<UserCollectRelation>().eq(UserCollectRelation::getFolderId, folderId));
        folderMapper.deleteById(folderId);
    }

    @Override
    @Transactional
    public boolean toggleCollect(Long userId, Long folderId, Integer targetType, Long targetId) {
        LambdaQueryWrapper<UserCollectRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollectRelation::getUserId, userId)
                .eq(UserCollectRelation::getFolderId, folderId)
                .eq(UserCollectRelation::getTargetType, targetType)
                .eq(UserCollectRelation::getTargetId, targetId);
        List<UserCollectRelation> existing = relationMapper.selectList(wrapper);
        if (existing.size() > 1) {
            throw new BusinessException("数据异常：重复收藏");
        }
        if (existing.size() == 1) {
            relationMapper.deleteById(existing.get(0));
            return false;
        }
        UserCollectRelation rel = new UserCollectRelation();
        rel.setUserId(userId);
        rel.setFolderId(folderId);
        rel.setTargetType(targetType);
        rel.setTargetId(targetId);
        relationMapper.insert(rel);
        return true;
    }

    @Override
    @Transactional
    public void collectToFolders(Long userId, List<Long> folderIds, Integer targetType, Long targetId) {
        if (folderIds == null || folderIds.isEmpty()) {
            // 默认收藏到默认文件夹
            ensureDefaultFolder(userId);
            UserCollectFolder defaultFolder = folderMapper.selectOne(
                new LambdaQueryWrapper<UserCollectFolder>()
                    .eq(UserCollectFolder::getUserId, userId)
                    .eq(UserCollectFolder::getFolderName, "默认收藏"));
            toggleCollect(userId, defaultFolder.getId(), targetType, targetId);
            return;
        }
        for (Long folderId : folderIds) {
            toggleCollect(userId, folderId, targetType, targetId);
        }
    }

    @Override
    public List<CollectItemVO> getFolderCollects(Long userId, Long folderId, Integer targetType) {
        LambdaQueryWrapper<UserCollectRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollectRelation::getFolderId, folderId);
        if (targetType != null) {
            wrapper.eq(UserCollectRelation::getTargetType, targetType);
        }
        List<UserCollectRelation> relations = relationMapper.selectList(wrapper);
        List<CollectItemVO> vos = new ArrayList<>();
        for (UserCollectRelation rel : relations) {
            CollectItemVO vo = new CollectItemVO();
            vo.setRelationId(rel.getId());
            vo.setTargetId(rel.getTargetId());
            vo.setTargetType(rel.getTargetType());
            vo.setCreateTime(rel.getCreateTime());
            if (rel.getTargetType() == 1) {
                Question q = questionMapper.selectById(rel.getTargetId());
                if (q != null) {
                    vo.setTitle(q.getTitle());
                    vo.setDescription(q.getContent());
                    User u = userMapper.selectById(q.getUserId());
                    if (u != null) vo.setUserNickname(u.getNickname());
                    Course c = courseMapper.selectById(q.getCourseId());
                    if (c != null) vo.setCourseName(c.getName());
                }
            } else {
                Resource r = resourceMapper.selectById(rel.getTargetId());
                if (r != null) {
                    vo.setTitle(r.getTitle());
                    vo.setDescription(r.getDescription());
                    User u = userMapper.selectById(r.getUserId());
                    if (u != null) vo.setUserNickname(u.getNickname());
                    Course c = courseMapper.selectById(r.getCourseId());
                    if (c != null) vo.setCourseName(c.getName());
                }
            }
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public boolean isCollected(Long userId, Long folderId, Integer targetType, Long targetId) {
        Long count = relationMapper.selectCount(
            new LambdaQueryWrapper<UserCollectRelation>()
                .eq(UserCollectRelation::getUserId, userId)
                .eq(UserCollectRelation::getFolderId, folderId)
                .eq(UserCollectRelation::getTargetType, targetType)
                .eq(UserCollectRelation::getTargetId, targetId));
        return count > 0;
    }

    @Override
    public boolean isCollectedAny(Long userId, Integer targetType, Long targetId) {
        Long count = relationMapper.selectCount(
            new LambdaQueryWrapper<UserCollectRelation>()
                .eq(UserCollectRelation::getUserId, userId)
                .eq(UserCollectRelation::getTargetType, targetType)
                .eq(UserCollectRelation::getTargetId, targetId));
        return count > 0;
    }

    @Override
    @Transactional
    public void moveCollect(Long userId, Long relationId, Long targetFolderId) {
        UserCollectRelation rel = relationMapper.selectById(relationId);
        if (rel == null || !rel.getUserId().equals(userId)) {
            throw new BusinessException("收藏记录不存在");
        }
        // 检查目标文件夹是否已有该收藏
        Long count = relationMapper.selectCount(
            new LambdaQueryWrapper<UserCollectRelation>()
                .eq(UserCollectRelation::getUserId, userId)
                .eq(UserCollectRelation::getFolderId, targetFolderId)
                .eq(UserCollectRelation::getTargetType, rel.getTargetType())
                .eq(UserCollectRelation::getTargetId, rel.getTargetId()));
        if (count > 0) {
            // 已存在，删除当前
            relationMapper.deleteById(relationId);
        } else {
            rel.setFolderId(targetFolderId);
            relationMapper.updateById(rel);
        }
    }

    @Override
    public void removeCollect(Long userId, Long relationId) {
        UserCollectRelation rel = relationMapper.selectById(relationId);
        if (rel == null || !rel.getUserId().equals(userId)) {
            throw new BusinessException("收藏记录不存在");
        }
        relationMapper.deleteById(relationId);
    }
}