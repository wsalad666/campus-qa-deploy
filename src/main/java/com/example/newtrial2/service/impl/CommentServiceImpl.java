package com.example.newtrial2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.newtrial2.dto.request.CommentRequest;
import com.example.newtrial2.dto.response.CommentVO;
import com.example.newtrial2.entity.Comment;
import com.example.newtrial2.entity.Notification;
import com.example.newtrial2.entity.User;
import com.example.newtrial2.exception.BusinessException;
import com.example.newtrial2.mapper.AnswerMapper;
import com.example.newtrial2.mapper.CommentMapper;
import com.example.newtrial2.mapper.NotificationMapper;
import com.example.newtrial2.mapper.UserMapper;
import com.example.newtrial2.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final AnswerMapper answerMapper;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;

    public CommentServiceImpl(CommentMapper commentMapper, AnswerMapper answerMapper,
                              UserMapper userMapper, NotificationMapper notificationMapper) {
        this.commentMapper = commentMapper;
        this.answerMapper = answerMapper;
        this.userMapper = userMapper;
        this.notificationMapper = notificationMapper;
    }

    @Override
    @Transactional
    public void addComment(Long userId, CommentRequest request) {
        checkBanStatus(userId);
        if (answerMapper.selectById(request.getAnswerId()) == null) {
            throw new BusinessException("回答不存在");
        }
        Comment comment = new Comment();
        comment.setAnswerId(request.getAnswerId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        comment.setReplyToId(request.getReplyToId() != null ? request.getReplyToId() : 0L);
        commentMapper.insert(comment);

        // 更新回答的评论数
        com.example.newtrial2.entity.Answer answer = answerMapper.selectById(request.getAnswerId());
        if (answer != null) {
            answer.setCommentCount((answer.getCommentCount() == null ? 0 : answer.getCommentCount()) + 1);
            answerMapper.updateById(answer);
        }

        // 发送通知
        User commenter = userMapper.selectById(userId);
        String commenterNickname = commenter != null ? commenter.getNickname() : "用户";

        if (request.getParentId() == null || request.getParentId() == 0) {
            // 一级评论：通知回答者
            if (answer != null && !answer.getUserId().equals(userId)) {
                sendInteractionNotification(answer.getUserId(),
                    "有人评论了你的回答",
                    "「" + commenterNickname + "」评论了你的回答",
                    2, 1, answer.getQuestionId(), userId, commenterNickname);
            }
        } else {
            // 子评论/回复：通知被回复的评论者
            if (request.getReplyToId() != null && request.getReplyToId() > 0
                    && !request.getReplyToId().equals(userId)) {
                User replyToUser = userMapper.selectById(request.getReplyToId());
                String replyToNickname = replyToUser != null ? replyToUser.getNickname() : "用户";
                sendInteractionNotification(request.getReplyToId(),
                    "有人回复了你的评论",
                    "「" + commenterNickname + "」回复了你的评论",
                    2, 1, answer != null ? answer.getQuestionId() : null, userId, commenterNickname);
            }
        }
    }

    @Override
    public List<CommentVO> getCommentsByAnswerId(Long answerId, Long currentUserId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getAnswerId, answerId)
                .eq(Comment::getIsOffline, 0)
                .orderByAsc(Comment::getCreateTime);
        List<Comment> comments = commentMapper.selectList(wrapper);

        if (comments.isEmpty()) {
            return new ArrayList<>();
        }

        // 收集所有用户ID
        List<Long> userIds = comments.stream().map(Comment::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userIds.isEmpty() ? new java.util.HashMap<>()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<CommentVO> allVos = comments.stream().map(c -> {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setAnswerId(c.getAnswerId());
            vo.setUserId(c.getUserId());
            vo.setParentId(c.getParentId());
            vo.setReplyToId(c.getReplyToId());
            vo.setContent(c.getContent());
            vo.setCreateTime(c.getCreateTime());
            User u = userMap.get(c.getUserId());
            if (u != null) {
                vo.setUserNickname(u.getNickname());
                vo.setUserAvatar(u.getAvatar());
            }
            if (c.getReplyToId() != null && c.getReplyToId() > 0) {
                User replyTo = userMap.get(c.getReplyToId());
                if (replyTo != null) {
                    vo.setReplyToNickname(replyTo.getNickname());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        // 构建树形结构
        Map<Long, List<CommentVO>> parentMap = allVos.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                .collect(Collectors.groupingBy(CommentVO::getParentId));

        List<CommentVO> rootComments = new ArrayList<>();
        for (CommentVO vo : allVos) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                vo.setChildren(parentMap.getOrDefault(vo.getId(), new ArrayList<>()));
                rootComments.add(vo);
            }
        }
        return rootComments;
    }

    private void checkBanStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            boolean isPermanentMuted = user.getBanEndTime() == null && user.getBanReason() != null;
            boolean isTimeMuted = user.getBanEndTime() != null && user.getBanEndTime().isAfter(LocalDateTime.now());
            if (isPermanentMuted || isTimeMuted) {
                String desc = isPermanentMuted ? "永久禁言" : "限时禁言至" + user.getBanEndTime();
                throw new BusinessException(403, "你已被禁言（" + desc + "），无法发布评论。原因：" + user.getBanReason());
            }
        }
    }

    private void sendInteractionNotification(Long userId, String title, String content, Integer type,
            Integer linkType, Long linkId, Long senderId, String senderNickname) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        notification.setLinkType(linkType);
        notification.setLinkId(linkId);
        notification.setSenderId(senderId);
        notification.setSenderNickname(senderNickname);
        notification.setIsDeletable(1);
        notificationMapper.insert(notification);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人评论");
        }
        // 物理删除评论及其子回复
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, commentId);
        commentMapper.delete(wrapper); // 删除子回复
        commentMapper.deleteById(commentId); // 删除该评论
    }
}