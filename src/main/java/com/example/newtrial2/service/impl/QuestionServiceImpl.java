package com.example.newtrial2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.newtrial2.dto.request.AnswerRequest;
import com.example.newtrial2.dto.request.QuestionRequest;
import com.example.newtrial2.dto.response.*;
import com.example.newtrial2.entity.*;
import com.example.newtrial2.exception.BusinessException;
import com.example.newtrial2.exception.ResourceNotFoundException;
import com.example.newtrial2.mapper.*;
import com.example.newtrial2.service.CollectService;
import com.example.newtrial2.service.CommentService;
import com.example.newtrial2.service.FavoriteService;
import com.example.newtrial2.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionServiceImpl.class);

    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final QuestionLikeMapper questionLikeMapper;
    private final FavoriteService favoriteService;
    private final CollectService collectService;
    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final NotificationMapper notificationMapper;

    public QuestionServiceImpl(QuestionMapper questionMapper, CommentMapper commentMapper, AnswerMapper answerMapper,
                               UserMapper userMapper, CourseMapper courseMapper,
                               QuestionLikeMapper questionLikeMapper,
                               FavoriteService favoriteService,
                               CollectService collectService,
                               CommentService commentService,
                               NotificationMapper notificationMapper) {
        this.questionMapper = questionMapper;
        this.answerMapper = answerMapper;
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
        this.questionLikeMapper = questionLikeMapper;
        this.favoriteService = favoriteService;
        this.collectService = collectService;
        this.commentService = commentService;
        this.commentMapper = commentMapper;
        this.notificationMapper = notificationMapper;
    }

    @Override
    @Transactional
    public void publishQuestion(Long userId, QuestionRequest request) {
        checkBanStatus(userId);
        Question question = new Question();
        question.setUserId(userId);
        question.setCourseId(request.getCourseId());
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setImageUrls(request.getImageUrls()); // 保存图片路径列表
        question.setStatus(0);
        question.setViewCount(0);
        question.setAnswerCount(0);
        questionMapper.insert(question);
    }

    @Override
    public PageResult<QuestionVO> pageQuestions(List<Long> courseIds, String keyword, String sort,
                                                 Integer pageNum, Integer pageSize, Long currentUserId, Long userId) {
        Page<Question> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();

        if (courseIds != null && !courseIds.isEmpty()) {
            wrapper.in(Question::getCourseId, courseIds);
        }
        wrapper.eq(Question::getIsOffline, 0);
        if (userId != null) {
            wrapper.eq(Question::getUserId, userId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 同时搜索发布者（昵称/学号）
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(User::getNickname, keyword)
                    .or().like(User::getStudentNo, keyword);
            List<User> matchedUsers = userMapper.selectList(userWrapper);
            List<Long> matchedUserIds = matchedUsers.stream().map(User::getId).collect(Collectors.toList());
            
            wrapper.and(w -> {
                w.like(Question::getTitle, keyword)
                 .or().like(Question::getContent, keyword);
                if (!matchedUserIds.isEmpty()) {
                    w.or().in(Question::getUserId, matchedUserIds);
                }
            });
        }        if ("hot".equals(sort)) {
            wrapper.orderByDesc(Question::getViewCount, Question::getAnswerCount);
        } else {
            wrapper.orderByDesc(Question::getCreateTime);
        }

        IPage<Question> result = questionMapper.selectPage(page, wrapper);

        List<QuestionVO> records = new ArrayList<>();
        for (Question q : result.getRecords()) {
            QuestionVO vo = new QuestionVO();
            vo.setId(q.getId());
            vo.setUserId(q.getUserId());
            vo.setCourseId(q.getCourseId());
            vo.setTitle(q.getTitle());
            vo.setContent(q.getContent());
            vo.setStatus(q.getStatus());
            vo.setViewCount(q.getViewCount());
            vo.setAnswerCount(q.getAnswerCount());
            vo.setLikeCount(q.getLikeCount());
            vo.setFavoriteCount(q.getFavoriteCount());
            vo.setCreateTime(q.getCreateTime());
            vo.setImageUrls(q.getImageUrls());

            User u = userMapper.selectById(q.getUserId());
            if (u != null) {
                vo.setUserNickname(u.getNickname());
                vo.setUserAvatar(u.getAvatar());
            }
            Course c = courseMapper.selectById(q.getCourseId());
            if (c != null) {
                vo.setCourseName(c.getName());
            }

            // 查询当前用户的点赞和收藏状态
            if (currentUserId != null) {
                LambdaQueryWrapper<QuestionLike> likeWrapper = new LambdaQueryWrapper<>();
                likeWrapper.eq(QuestionLike::getUserId, currentUserId)
                        .eq(QuestionLike::getQuestionId, q.getId());
                vo.setIsLiked(questionLikeMapper.selectCount(likeWrapper) > 0);
                vo.setIsFavorited(favoriteService.isFavorited(currentUserId, q.getId(), 1)
                        || collectService.isCollectedAny(currentUserId, 1, q.getId()));
            } else {
                vo.setIsLiked(false);
                vo.setIsFavorited(false);
            }

            records.add(vo);
        }
        return new PageResult<>(result.getTotal(), pageNum, pageSize, records);
    }

    @Override
    @Transactional
    public QuestionDetailResponse getQuestionDetail(Long questionId, Long currentUserId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null || question.getIsOffline() == 1) {
            throw new ResourceNotFoundException("问题不存在");
        }
        // 增加浏览量
        question.setViewCount((question.getViewCount() == null ? 0 : question.getViewCount()) + 1);
        questionMapper.updateById(question);

        QuestionDetailResponse response = new QuestionDetailResponse();
        response.setId(question.getId());
        response.setUserId(question.getUserId());
        response.setCourseId(question.getCourseId());
        response.setTitle(question.getTitle());
        response.setContent(question.getContent());
        response.setStatus(question.getStatus());
        response.setViewCount(question.getViewCount());
        response.setAnswerCount(question.getAnswerCount());
        response.setLikeCount(question.getLikeCount());
        response.setFavoriteCount(question.getFavoriteCount());
        response.setCreateTime(question.getCreateTime());
        response.setImageUrls(question.getImageUrls());
        response.setAdoptAnswerId(question.getAdoptAnswerId());

        User questionUser = userMapper.selectById(question.getUserId());
        if (questionUser != null) {
            response.setUserNickname(questionUser.getNickname());
            response.setUserAvatar(questionUser.getAvatar());
        }
        Course course = courseMapper.selectById(question.getCourseId());
        if (course != null) {
            response.setCourseName(course.getName());
        }

        // 当前用户收藏状态
        if (currentUserId != null) {
            response.setIsFavorited(favoriteService.isFavorited(currentUserId, questionId, 1)
                    || collectService.isCollectedAny(currentUserId, 1, questionId));
            // 当前用户是否点赞了该问题
            LambdaQueryWrapper<QuestionLike> questionLikeWrapper = new LambdaQueryWrapper<>();
            questionLikeWrapper.eq(QuestionLike::getUserId, currentUserId)
                    .eq(QuestionLike::getQuestionId, questionId);
            response.setIsLiked(questionLikeMapper.selectCount(questionLikeWrapper) > 0);
        } else {
            response.setIsFavorited(false);
            response.setIsLiked(false);
        }

        // 查询回答列表：采纳置顶，其余按点赞数降序
        LambdaQueryWrapper<Answer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(Answer::getQuestionId, questionId)
                .eq(Answer::getIsOffline, 0)
                .orderByDesc(Answer::getIsAccepted)
                .orderByDesc(Answer::getLikeCount);
        List<Answer> answers = answerMapper.selectList(answerWrapper);

        List<Long> answerUserIds = answers.stream().map(Answer::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = answerUserIds.isEmpty() ? new java.util.HashMap<>()
                : userMapper.selectBatchIds(answerUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<AnswerVO> answerVOs = new ArrayList<>();
        for (Answer answer : answers) {
            AnswerVO avo = new AnswerVO();
            avo.setId(answer.getId());
            avo.setQuestionId(answer.getQuestionId());
            avo.setUserId(answer.getUserId());
            avo.setContent(answer.getContent());
            avo.setLikeCount(answer.getLikeCount());
            avo.setCommentCount(answer.getCommentCount());
            avo.setIsAccepted(answer.getIsAccepted());
            avo.setCreateTime(answer.getCreateTime());
            avo.setImageUrls(answer.getImageUrls());

            User au = userMap.get(answer.getUserId());
            if (au != null) {
                avo.setUserNickname(au.getNickname());
                avo.setUserAvatar(au.getAvatar());
            }

            // 当前用户是否点赞
            if (currentUserId != null) {
                LambdaQueryWrapper<QuestionLike> likeWrapper = new LambdaQueryWrapper<>();
                likeWrapper.eq(QuestionLike::getUserId, currentUserId)
                        .eq(QuestionLike::getAnswerId, answer.getId());
                avo.setIsLiked(questionLikeMapper.selectCount(likeWrapper) > 0);
            }

            // 查询评论
            List<CommentVO> comments = commentService.getCommentsByAnswerId(answer.getId(), currentUserId);
            avo.setComments(comments);
            answerVOs.add(avo);
        }
        response.setAnswers(answerVOs);
        return response;
    }

    @Override
    @Transactional
    public void answerQuestion(Long userId, AnswerRequest request) {
        checkBanStatus(userId);
        Question question = questionMapper.selectById(request.getQuestionId());
        if (question == null) {
            throw new ResourceNotFoundException("问题不存在");
        }
        Answer answer = new Answer();
        answer.setQuestionId(request.getQuestionId());
        answer.setUserId(userId);
        answer.setContent(request.getContent());
        answer.setImageUrls(request.getImageUrls()); // 保存图片路径列表
        answer.setLikeCount(0);
        answer.setCommentCount(0);
        answer.setIsAccepted(0);
        answerMapper.insert(answer);

        question.setAnswerCount((question.getAnswerCount() == null ? 0 : question.getAnswerCount()) + 1);
        questionMapper.updateById(question);

        // 通知提问者：有人回答了你的问题
        if (!question.getUserId().equals(userId)) {
            User answerUser = userMapper.selectById(userId);
            String nickname = answerUser != null ? answerUser.getNickname() : "用户";
            sendInteractionNotification(question.getUserId(),
                "有人回答了你的提问",
                "「" + nickname + "」回答了你的提问「" + question.getTitle() + "」",
                2, 1, question.getId(), userId, nickname);
        }
    }

    @Override
    @Transactional
    public boolean likeAnswer(Long userId, Long answerId) {
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw new ResourceNotFoundException("回答不存在");
        }

        LambdaQueryWrapper<QuestionLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionLike::getUserId, userId)
                .eq(QuestionLike::getAnswerId, answerId);
        
        // 尝试删除，如果删除成功说明之前是点赞状态，现在取消点赞
        int deletedRows = questionLikeMapper.delete(wrapper);
        if (deletedRows > 0) {
            // 重新统计点赞数
            long newCount = questionLikeMapper.selectCount(
                new LambdaQueryWrapper<QuestionLike>().eq(QuestionLike::getAnswerId, answerId));
            answer.setLikeCount((int) newCount);
            answerMapper.updateById(answer);
            return false;
        } else {
            // 如果删除失败，说明之前没有点赞，现在尝试点赞
            QuestionLike like = new QuestionLike();
            like.setUserId(userId);
            like.setAnswerId(answerId);
            try {
                questionLikeMapper.insert(like);
                // 重新统计点赞数
                long newCount = questionLikeMapper.selectCount(
                    new LambdaQueryWrapper<QuestionLike>().eq(QuestionLike::getAnswerId, answerId));
                answer.setLikeCount((int) newCount);
                answerMapper.updateById(answer);

                // 通知回答者：有人点赞了你的回答
                if (!answer.getUserId().equals(userId)) {
                    User liker = userMapper.selectById(userId);
                    String nickname = liker != null ? liker.getNickname() : "用户";
                    sendInteractionNotification(answer.getUserId(),
                        "有人点赞了你的回答",
                        "「" + nickname + "」点赞了你的回答",
                        1, 1, answer.getQuestionId(), userId, nickname);
                }
                return true;
            } catch (DuplicateKeyException e) {
                logger.warn("用户 {} 尝试重复点赞回答 {}，但可能存在并发或状态不同步问题。", userId, answerId);
                return true;
            } catch (DataIntegrityViolationException e) {
                logger.error("点赞回答数据库完整性校验失败: {}", e.getMessage());
                throw new BusinessException("点赞回答失败，请检查数据完整性或稍后再试");
            }
        }
    }

    @Override
    @Transactional
    public boolean likeQuestion(Long userId, Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new ResourceNotFoundException("问题不存在");
        }

        LambdaQueryWrapper<QuestionLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionLike::getUserId, userId)
                .eq(QuestionLike::getQuestionId, questionId);

        // 尝试删除，如果删除成功说明之前是点赞状态，现在取消点赞
        int deletedRows = questionLikeMapper.delete(wrapper);
        if (deletedRows > 0) {
            // 重新统计点赞数
            long newCount = questionLikeMapper.selectCount(
                new LambdaQueryWrapper<QuestionLike>().eq(QuestionLike::getQuestionId, questionId));
            question.setLikeCount((int) newCount);
            questionMapper.updateById(question);
            return false;
        } else {
            // 如果删除失败，说明之前没有点赞，现在尝试点赞
            QuestionLike like = new QuestionLike();
            like.setUserId(userId);
            like.setQuestionId(questionId);
            try {
                questionLikeMapper.insert(like);
                // 重新统计点赞数
                long newCount = questionLikeMapper.selectCount(
                    new LambdaQueryWrapper<QuestionLike>().eq(QuestionLike::getQuestionId, questionId));
                question.setLikeCount((int) newCount);
                questionMapper.updateById(question);

                // 通知提问者：有人点赞了你的提问
                if (!question.getUserId().equals(userId)) {
                    User liker = userMapper.selectById(userId);
                    String nickname = liker != null ? liker.getNickname() : "用户";
                    sendInteractionNotification(question.getUserId(),
                        "有人点赞了你的提问",
                        "「" + nickname + "」点赞了你的提问「" + question.getTitle() + "」",
                        0, 1, questionId, userId, nickname);
                }
                return true;
            } catch (DuplicateKeyException e) {
                logger.warn("用户 {} 尝试重复点赞问题 {}，但可能存在并发或状态不同步问题。", userId, questionId);
                return true;
            } catch (DataIntegrityViolationException e) {
                logger.error("点赞问题数据库完整性校验失败: {}", e.getMessage());
                throw new BusinessException("点赞问题失败，请检查数据完整性或稍后再试");
            }
        }
    }

    @Override
    @Transactional
    public void acceptAnswer(Long userId, Long answerId) {
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw new ResourceNotFoundException("回答不存在");
        }
        Question question = questionMapper.selectById(answer.getQuestionId());
        if (question == null) {
            throw new ResourceNotFoundException("问题不存在");
        }
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(403, "只有提问者才能采纳回答");
        }

        if (question.getStatus() != null && question.getStatus() == 3) {
            throw new BusinessException("私密提问无法采纳回答，请先取消私密");
        }
        if (question.getStatus() != null && question.getStatus() == 2) {
            throw new BusinessException("已关闭的提问无法采纳回答，请先重新打开");
        }

        if (answer.getIsAccepted() != null && answer.getIsAccepted() == 1) {
            // 当前回答已采纳 → 取消采纳
            answer.setIsAccepted(0);
            answerMapper.updateById(answer);
            question.setAdoptAnswerId(null);
            question.setStatus(0);
            questionMapper.updateById(question);
        } else {
            // 取消之前已采纳的回答
            if (question.getAdoptAnswerId() != null) {
                Answer previous = answerMapper.selectById(question.getAdoptAnswerId());
                if (previous != null) {
                    previous.setIsAccepted(0);
                    answerMapper.updateById(previous);
                }
            }
            // 采纳当前回答
            answer.setIsAccepted(1);
            answerMapper.updateById(answer);
            question.setAdoptAnswerId(answerId);
            question.setStatus(1);
            questionMapper.updateById(question);

            // 通知回答者：你的回答被采纳
            if (!answer.getUserId().equals(userId)) {
                User questionUser = userMapper.selectById(userId);
                String nickname = questionUser != null ? questionUser.getNickname() : "提问者";
                sendInteractionNotification(answer.getUserId(),
                    "你的回答被采纳为优质答案",
                    "「" + nickname + "」采纳了你在「" + question.getTitle() + "」中的回答",
                    3, 1, question.getId(), userId, nickname);
            }
        }
    }

    private void checkBanStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            boolean isPermanentMuted = user.getBanEndTime() == null && user.getBanReason() != null;
            boolean isTimeMuted = user.getBanEndTime() != null && user.getBanEndTime().isAfter(LocalDateTime.now());
            if (isPermanentMuted || isTimeMuted) {
                String desc = isPermanentMuted ? "永久禁言" : "限时禁言至" + user.getBanEndTime();
                throw new BusinessException(403, "你已被禁言（" + desc + "），无法发布内容。原因：" + user.getBanReason());
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
    public void deleteAnswer(Long userId, Long answerId) {
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw new BusinessException("回答不存在");
        }
        // 检查权限：回答者本人 OR 提问者
        Question question = questionMapper.selectById(answer.getQuestionId());
        if (question == null) {
            throw new BusinessException("关联提问不存在");
        }
        boolean isAnswerOwner = answer.getUserId().equals(userId);
        boolean isQuestionOwner = question.getUserId().equals(userId);
        if (!isAnswerOwner && !isQuestionOwner) {
            throw new BusinessException(403, "无权删除此回答");
        }
        // 级联删除该回答的所有评论
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getAnswerId, answerId);
        commentMapper.delete(commentWrapper);
        // 删除回答
        answerMapper.deleteById(answerId);
        // 更新问题的回答数
        LambdaQueryWrapper<Answer> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Answer::getQuestionId, answer.getQuestionId());
        long remaining = answerMapper.selectCount(countWrapper);
        question.setAnswerCount((int) remaining);
        questionMapper.updateById(question);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long userId, Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException("提问不存在");
        }
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人提问");
        }
        // 级联删除回答
        LambdaQueryWrapper<Answer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(Answer::getQuestionId, questionId);
        answerMapper.delete(answerWrapper);
        // 删除问题点赞
        LambdaQueryWrapper<QuestionLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(QuestionLike::getQuestionId, questionId);
        questionLikeMapper.delete(likeWrapper);
        // 删除提问
        questionMapper.deleteById(questionId);
    }

    @Override
    @Transactional
    public void closeQuestion(Long userId, Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException("提问不存在");
        }
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人提问");
        }
        question.setStatus(2); // 2=已关闭
        questionMapper.updateById(question);
    }

    @Override
    @Transactional
    public void hideQuestion(Long userId, Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException("提问不存在");
        }
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人提问");
        }
        question.setStatus(3); // 3=私密
        questionMapper.updateById(question);
    }
    @Override
    @Transactional
    public void unhideQuestion(Long userId, Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException("提问不存在");
        }
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人提问");
        }
        question.setStatus(0); // 0=正常
        questionMapper.updateById(question);
    }

    @Override
    @Transactional
    public void reopenQuestion(Long userId, Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException("提问不存在");
        }
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人提问");
        }
        question.setStatus(0); // 0=正常
        questionMapper.updateById(question);
    }


    @Override
    public List<SimilarQuestionVO> getSimilarQuestions(Long courseId, Long excludeId, String title, String content, int limit) {
        if (courseId == null) return List.of();

        // 第一层：合并全文本 + 清洗
        String searchText = cleanText(title) + " " + cleanText(content);
        searchText = searchText.trim();
        if (searchText.isEmpty()) return List.of();

        // 第二层：MySQL FULLTEXT 自然语言检索粗筛，捞15条候选
        // BOOLEAN MODE: add + prefix and * wildcard for each word
        String ftKeyword = "";
        for (String word : searchText.split("\s+")) {
            if (word.length() >= 3) {
                ftKeyword += "+" + word + "* ";
            }
        }
        ftKeyword = ftKeyword.trim();
        if (ftKeyword.isEmpty()) return List.of();
        List<Question> candidates = questionMapper.searchSimilar(courseId, excludeId, ftKeyword);
        if (candidates.isEmpty()) return List.of();

        // 第三层：后端加权 N-Gram 精排
        List<SimilarQuestionVO> results = new ArrayList<>();
        for (Question q : candidates) {
            String qTitle = cleanText(q.getTitle());
            String qContent = cleanText(q.getContent());

            // 标题相似度（权重 70%）
            double titleSim = nGramSimilarity(searchText, qTitle);
            // 正文相似度（权重 30%）
            double contentSim = nGramSimilarity(searchText, qContent);
            // 加权总分
            double totalSim = titleSim * 0.7 + contentSim * 0.3;

            if (totalSim >= 60.0) {
                SimilarQuestionVO vo = new SimilarQuestionVO();
                vo.setId(q.getId());
                vo.setTitle(q.getTitle());
                vo.setViewCount(q.getViewCount());
                vo.setAnswerCount(q.getAnswerCount());
                vo.setSimilarity(Math.round(totalSim * 100.0) / 100.0);
                results.add(vo);
            }
        }

        // 按相似度降序排序
        results.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));

        // 返回最多 N 条
        if (results.size() > limit) {
            return results.subList(0, limit);
        }
        return results;
    }

    /**
     * 文本清洗：去除HTML标签、换行、多余空格、标点符号
     */
    private String cleanText(String text) {
        if (text == null) return "";
        // 去HTML标签
        String cleaned = text.replaceAll("<[^>]+>", "").replaceAll("&nbsp;", " ");
        // 统一换行和制表符为空格
        cleaned = cleaned.replaceAll("[\r\n\t]+", " ");
        // 去除无关标点符号（保留中文、英文、数字、空格）
        cleaned = cleaned.replaceAll("[？！，。、；：（）()\\[\\]【】\"\"''《》…—·,.\"!?;:\\-]+", " ");
        // 压缩多余空格
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    /**
     * N-Gram (bigram) 字符重叠相似度，返回 0-100
     */
    private double nGramSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        if (s1.isEmpty() && s2.isEmpty()) return 100;
        if (s1.isEmpty() || s2.isEmpty()) return 0;

        java.util.Set<String> bigrams1 = new java.util.HashSet<>();
        java.util.Set<String> bigrams2 = new java.util.HashSet<>();

        for (int i = 0; i < s1.length() - 1; i++) {
            bigrams1.add(s1.substring(i, i + 2));
        }
        for (int i = 0; i < s2.length() - 1; i++) {
            bigrams2.add(s2.substring(i, i + 2));
        }

        if (bigrams1.isEmpty() || bigrams2.isEmpty()) return 0;

        java.util.Set<String> intersection = new java.util.HashSet<>(bigrams1);
        intersection.retainAll(bigrams2);

        double dice = (2.0 * intersection.size()) / (bigrams1.size() + bigrams2.size());
        return dice * 100.0;
    }
}
