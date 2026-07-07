package com.example.newtrial2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.newtrial2.dto.response.FavoriteVO;
import com.example.newtrial2.dto.response.PageResult;
import com.example.newtrial2.entity.Favorite;
import com.example.newtrial2.entity.Question;
import com.example.newtrial2.entity.Resource;
import com.example.newtrial2.exception.BusinessException;
import com.example.newtrial2.mapper.FavoriteMapper;
import com.example.newtrial2.mapper.QuestionMapper;
import com.example.newtrial2.mapper.ResourceMapper;
import com.example.newtrial2.service.FavoriteService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteServiceImpl.class);

    private final FavoriteMapper favoriteMapper;
    private final QuestionMapper questionMapper;
    private final ResourceMapper resourceMapper;

    public FavoriteServiceImpl(FavoriteMapper favoriteMapper, QuestionMapper questionMapper,
                               ResourceMapper resourceMapper) {
        this.favoriteMapper = favoriteMapper;
        this.questionMapper = questionMapper;
        this.resourceMapper = resourceMapper;
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long targetId, Integer type) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetId, targetId)
                .eq(Favorite::getType, type);

        // 尝试删除，如果删除成功说明之前是收藏状态，现在取消收藏
        int deletedRows = favoriteMapper.delete(wrapper);
        if (deletedRows > 0) {
            // 更新收藏计数
            updateFavoriteCount(targetId, type);
            return false; // 已取消收藏
        } else {
            // 如果删除失败，说明之前没有收藏，现在尝试收藏
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setTargetId(targetId);
            favorite.setType(type);
            try {
                favoriteMapper.insert(favorite);
                // 更新收藏计数
                updateFavoriteCount(targetId, type);
                return true; // 收藏成功
            } catch (DuplicateKeyException e) {
                logger.warn("用户 {} 尝试重复收藏 targetId {} (type {})，但可能存在并发或状态不同步问题。", userId, targetId, type);
                return true; // 视为已收藏成功，避免前端报错
            } catch (DataIntegrityViolationException e) {
                logger.error("收藏操作数据库完整性校验失败: {}", e.getMessage());
                throw new BusinessException("收藏失败，请检查数据完整性或稍后再试");
            }
        }
    }

    /**
     * 更新收藏目标（问题/资源）的收藏计数
     */
    private void updateFavoriteCount(Long targetId, Integer type) {
        LambdaQueryWrapper<Favorite> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Favorite::getTargetId, targetId)
                .eq(Favorite::getType, type);
        long count = favoriteMapper.selectCount(countWrapper);

        if (type == 1) {
            // 问题收藏
            Question question = questionMapper.selectById(targetId);
            if (question != null) {
                question.setFavoriteCount((int) count);
                questionMapper.updateById(question);
            }
        }
        // type == 2: 资源收藏，暂不需要更新计数
    }

    @Override
    public PageResult<FavoriteVO> getMyFavorites(Long userId, Integer pageNum, Integer pageSize) {
        Page<Favorite> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime);
        IPage<Favorite> result = favoriteMapper.selectPage(page, wrapper);

        List<FavoriteVO> records = new ArrayList<>();
        for (Favorite f : result.getRecords()) {
            FavoriteVO vo = new FavoriteVO();
            vo.setId(f.getId());
            vo.setTargetId(f.getTargetId());
            vo.setType(f.getType());
            vo.setCreateTime(f.getCreateTime());
            if (f.getType() == 1) {
                Question q = questionMapper.selectById(f.getTargetId());
                if (q != null) {
                    vo.setTitle(q.getTitle());
                    vo.setDescription(q.getContent());
                }
            } else if (f.getType() == 2) {
                Resource r = resourceMapper.selectById(f.getTargetId());
                if (r != null) {
                    vo.setTitle(r.getTitle());
                    vo.setDescription(r.getDescription());
                }
            }
            records.add(vo);
        }
        return new PageResult<>(result.getTotal(), pageNum, pageSize, records);
    }

    @Override
    public boolean isFavorited(Long userId, Long targetId, Integer type) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetId, targetId)
                .eq(Favorite::getType, type);
        return favoriteMapper.selectCount(wrapper) > 0;
    }
}