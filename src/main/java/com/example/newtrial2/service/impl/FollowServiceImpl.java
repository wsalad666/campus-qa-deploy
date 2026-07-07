package com.example.newtrial2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.newtrial2.dto.response.UserVO;
import com.example.newtrial2.entity.Follow;
import com.example.newtrial2.entity.Notification;
import com.example.newtrial2.entity.User;
import com.example.newtrial2.exception.BusinessException;
import com.example.newtrial2.mapper.FollowMapper;
import com.example.newtrial2.mapper.NotificationMapper;
import com.example.newtrial2.mapper.UserMapper;
import com.example.newtrial2.service.FollowService;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FollowServiceImpl implements FollowService {

    private static final Logger logger = LoggerFactory.getLogger(FollowServiceImpl.class);

    private final FollowMapper followMapper;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;

    public FollowServiceImpl(FollowMapper followMapper, UserMapper userMapper,
                             NotificationMapper notificationMapper) {
        this.followMapper = followMapper;
        this.userMapper = userMapper;
        this.notificationMapper = notificationMapper;
    }

    @Override
    @Transactional
    public boolean toggleFollow(Long userId, Long followedId) {
        if (userId.equals(followedId)) {
            throw new BusinessException("不能关注自己");
        }
        if (userMapper.selectById(followedId) == null) {
            throw new BusinessException("被关注用户不存在");
        }
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, userId)
                .eq(Follow::getFollowedId, followedId);
        
        // 尝试删除，如果删除成功说明之前是关注状态，现在取消关注
        int deletedRows = followMapper.delete(wrapper);
        if (deletedRows > 0) {
            return false; // 已取消关注
        } else {
            // 如果删除失败，说明之前没有关注，现在尝试关注
            Follow follow = new Follow();
            follow.setFollowerId(userId);
            follow.setFollowedId(followedId);
            try {
                followMapper.insert(follow);

                // 通知被关注者
                User follower = userMapper.selectById(userId);
                String nickname = follower != null ? follower.getNickname() : "用户";
                Notification notification = new Notification();
                notification.setUserId(followedId);
                notification.setTitle("有人关注了你");
                notification.setContent("「" + nickname + "」关注了你");
                notification.setType(4);
                notification.setIsRead(0);
                notification.setLinkType(3); // 用户主页
                notification.setLinkId(userId);
                notification.setSenderId(userId);
                notification.setSenderNickname(nickname);
                notification.setIsDeletable(1);
                notificationMapper.insert(notification);

                return true; // 关注成功
            } catch (DuplicateKeyException e) {
                // 如果插入时出现重复键异常，说明在删除操作后，有并发请求导致重复插入
                // 或者在删除操作前，selectList判断失误
                // 此时应该返回已关注状态
                logger.warn("用户 {} 尝试重复关注用户 {}，但可能存在并发或状态不同步问题。", userId, followedId);
                return true; // 视为已关注成功，避免前端报错
            } catch (DataIntegrityViolationException e) {
                logger.error("关注操作数据库完整性校验失败: {}", e.getMessage());
                throw new BusinessException("关注失败，请检查数据完整性或稍后再试");
            }
        }
    }

    @Override
    public List<UserVO> getFollowList(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, userId);
        List<Follow> follows = followMapper.selectList(wrapper);
        return follows.stream().map(f -> {
            User user = userMapper.selectById(f.getFollowedId());
            UserVO vo = new UserVO();
            if (user != null) {
                BeanUtils.copyProperties(user, vo);
            }
            vo.setIsFollowed(true); // 关注列表中的用户一定已关注
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserVO> getFansList(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowedId, userId);
        List<Follow> follows = followMapper.selectList(wrapper);
        return follows.stream().map(f -> {
            User user = userMapper.selectById(f.getFollowerId());
            UserVO vo = new UserVO();
            if (user != null) {
                BeanUtils.copyProperties(user, vo);
            }
            // 检查当前用户是否也关注了该粉丝
            vo.setIsFollowed(isFollowed(userId, user != null ? user.getId() : null));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean isFollowed(Long userId, Long targetUserId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, userId)
                .eq(Follow::getFollowedId, targetUserId);
        return followMapper.selectCount(wrapper) > 0;
    }
}