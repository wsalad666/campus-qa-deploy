package com.example.newtrial2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.newtrial2.common.AdminJwtUtil;
import com.example.newtrial2.dto.request.*;
import com.example.newtrial2.dto.response.*;
import com.example.newtrial2.entity.*;
import com.example.newtrial2.exception.BusinessException;
import com.example.newtrial2.exception.ResourceNotFoundException;
import com.example.newtrial2.mapper.*;
import com.example.newtrial2.service.AdminService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final CourseMapper courseMapper;
    private final QuestionMapper questionMapper;
    private final ResourceMapper resourceMapper;
    private final UserMapper userMapper;
    private final AnswerMapper answerMapper;
    private final ReportMapper reportMapper;
    private final UserBanLogMapper userBanLogMapper;
    private final NotificationMapper notificationMapper;
    private final AdminJwtUtil adminJwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminServiceImpl(AdminMapper adminMapper, CourseMapper courseMapper,
                            QuestionMapper questionMapper, ResourceMapper resourceMapper,
                            UserMapper userMapper, AnswerMapper answerMapper,
                            ReportMapper reportMapper, UserBanLogMapper userBanLogMapper,
                            NotificationMapper notificationMapper, AdminJwtUtil adminJwtUtil) {
        this.adminMapper = adminMapper;
        this.courseMapper = courseMapper;
        this.questionMapper = questionMapper;
        this.resourceMapper = resourceMapper;
        this.userMapper = userMapper;
        this.answerMapper = answerMapper;
        this.reportMapper = reportMapper;
        this.userBanLogMapper = userBanLogMapper;
        this.notificationMapper = notificationMapper;
        this.adminJwtUtil = adminJwtUtil;
    }

    @Override
    public AdminLoginResponse login(AdminLoginRequest request) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, request.getUsername());
        Admin admin = adminMapper.selectOne(wrapper);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException("密码错误");
        }
        
        // 确保管理员有关联的user记录（用于前台个人主页）
        Long userId = admin.getUserId();
        if (userId == null) {
            // 检查是否已有同名user记录
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(User::getUsername, admin.getUsername());
            User user = userMapper.selectOne(userWrapper);
            if (user == null) {
                user = new User();
                user.setStudentNo("ADMIN_" + admin.getId());
                user.setUsername(admin.getUsername());
                user.setNickname(admin.getNickname());
                user.setAvatar(admin.getAvatar());
                user.setPassword(passwordEncoder.encode("admin_" + admin.getUsername()));
                user.setSignature("校园管理员");
                userMapper.insert(user);
            }
            userId = user.getId();
            admin.setUserId(userId);
            adminMapper.updateById(admin);
        }
        
        String token = adminJwtUtil.generateToken(admin.getId(), userId);
        return new AdminLoginResponse(token, admin.getId(), admin.getUsername(), admin.getNickname(), admin.getAvatar(), userId);
    }

    @Override
    public List<Course> getAllCourses(String keyword) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Course::getName, keyword.trim());
        }
        wrapper.orderByAsc(Course::getSortOrder);
        return courseMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void addCourse(CourseRequest request) {
        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setIcon(request.getIcon());
        course.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        course.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        courseMapper.insert(course);
    }

    @Override
    @Transactional
    public void updateCourse(Long courseId, CourseRequest request) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new ResourceNotFoundException("课程分类不存在");
        }
        course.setName(request.getName());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getIcon() != null) course.setIcon(request.getIcon());
        if (request.getParentId() != null) course.setParentId(request.getParentId());
        if (request.getSortOrder() != null) course.setSortOrder(request.getSortOrder());
        courseMapper.updateById(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        if (courseMapper.selectById(courseId) == null) {
            throw new ResourceNotFoundException("课程分类不存在");
        }
        courseMapper.deleteById(courseId);
    }

    @Override
    public PageResult<QuestionVO> pageAllQuestions(Integer pageNum, Integer pageSize, String keyword) {
        Page<Question> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                .like(Question::getTitle, keyword)
                .or()
                .like(Question::getContent, keyword)
                .or()
                .eq(Question::getId, safeParseLong(keyword))
            );
        }
        wrapper.orderByDesc(Question::getCreateTime);
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
            vo.setIsOffline(q.getIsOffline());
            vo.setViewCount(q.getViewCount());
            vo.setAnswerCount(q.getAnswerCount());
            vo.setLikeCount(q.getLikeCount());
            vo.setFavoriteCount(q.getFavoriteCount());
            vo.setCreateTime(q.getCreateTime());
            User u = userMapper.selectById(q.getUserId());
            if (u != null) {
                vo.setUserNickname(u.getNickname());
                vo.setUserAvatar(u.getAvatar());
            }
            Course c = courseMapper.selectById(q.getCourseId());
            if (c != null) {
                vo.setCourseName(c.getName());
            }
            records.add(vo);
        }
        return new PageResult<>(result.getTotal(), pageNum, pageSize, records);
    }

    @Override
    public PageResult<ResourceVO> pageAllResources(Integer pageNum, Integer pageSize, String keyword) {
        Page<Resource> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                .like(Resource::getTitle, keyword)
                .or()
                .like(Resource::getDescription, keyword)
                .or()
                .eq(Resource::getId, safeParseLong(keyword))
            );
        }
        wrapper.orderByDesc(Resource::getCreateTime);
        IPage<Resource> result = resourceMapper.selectPage(page, wrapper);

        List<ResourceVO> records = new ArrayList<>();
        for (Resource r : result.getRecords()) {
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
            vo.setFileUrl(r.getFileUrl());
            vo.setCreateTime(r.getCreateTime());
            User u = userMapper.selectById(r.getUserId());
            if (u != null) vo.setUserNickname(u.getNickname());
            Course c = courseMapper.selectById(r.getCourseId());
            if (c != null) vo.setCourseName(c.getName());
            records.add(vo);
        }
        return new PageResult<>(result.getTotal(), pageNum, pageSize, records);
    }

    @Override
    @Transactional
    public void toggleQuestionOffline(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new ResourceNotFoundException("问题不存在");
        }
        int newOffline = question.getIsOffline() == 1 ? 0 : 1;
        question.setIsOffline(newOffline);
        questionMapper.updateById(question);

        // 发送通知给提问者
        User user = userMapper.selectById(question.getUserId());
        if (user != null) {
            String title = newOffline == 1 ? "你的提问已被下架" : "你的提问已恢复上架";
            String content = newOffline == 1
                ? "你的提问「" + question.getTitle() + "」因违规已被管理员下架"
                : "你的提问「" + question.getTitle() + "」已被管理员恢复上架";
            sendSystemNotification(user.getId(), title, content, 5, 1, questionId);
        }
    }

    @Override
    @Transactional
    public void toggleResourceOffline(Long resourceId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new ResourceNotFoundException("资源不存在");
        }
        int newOffline = resource.getIsOffline() == 1 ? 0 : 1;
        resource.setIsOffline(newOffline);
        resourceMapper.updateById(resource);

        User user = userMapper.selectById(resource.getUserId());
        if (user != null) {
            String title = newOffline == 1 ? "你的资源已被下架" : "你的资源已恢复上架";
            String content = newOffline == 1
                ? "你的资源「" + resource.getTitle() + "」因违规已被管理员下架"
                : "你的资源「" + resource.getTitle() + "」已被管理员恢复上架";
            sendSystemNotification(user.getId(), title, content, 5, null, null);
        }
    }

    @Override
    public StatisticsResponse getStatistics() {
        StatisticsResponse response = new StatisticsResponse();
        response.setTotalUsers(userMapper.countTotalUsers());
        response.setTodayNewUsers(userMapper.countTodayNewUsers());
        response.setTotalQuestions(questionMapper.countTotalQuestions());
        response.setTodayQuestions(questionMapper.countTodayQuestions());
        response.setTotalResources(resourceMapper.countTotalResources());
        response.setTotalDownloads(resourceMapper.countTotalDownloads());
        return response;
    }

    // ========== 举报工单管理 ==========

    @Override
    @Transactional
    public void submitReport(Long userId, ReportRequest request) {
        // 检查被举报目标是否存在
        if (request.getTargetType() == 1) {
            if (questionMapper.selectById(request.getTargetId()) == null) {
                throw new ResourceNotFoundException("被举报的提问不存在");
            }
        } else if (request.getTargetType() == 2) {
            if (answerMapper.selectById(request.getTargetId()) == null) {
                throw new ResourceNotFoundException("被举报的回答不存在");
            }
        } else {
            throw new BusinessException("无效的举报目标类型");
        }

        Report report = new Report();
        report.setReporterId(userId);
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason());
        report.setStatus(0); // 待处理
        reportMapper.insert(report);
    }

    @Override
    public PageResult<ReportVO> pageReports(Integer pageNum, Integer pageSize, Integer status, String keyword) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                .like(Report::getReason, keyword)
                .or()
                .eq(Report::getId, safeParseLong(keyword))
            );
        }
        wrapper.orderByAsc(Report::getStatus).orderByDesc(Report::getCreateTime);
        IPage<Report> result = reportMapper.selectPage(page, wrapper);

        List<ReportVO> records = new ArrayList<>();
        for (Report r : result.getRecords()) {
            ReportVO vo = buildReportVO(r);
            records.add(vo);
        }
        return new PageResult<>(result.getTotal(), pageNum, pageSize, records);
    }

    @Override
    public Long countPendingReports() {
        return reportMapper.countPending();
    }

    @Override
    public ReportVO getReportDetail(Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new ResourceNotFoundException("举报工单不存在");
        }
        return buildReportVO(report);
    }

    @Override
    @Transactional
    public void handleReport(Long adminId, Long reportId, HandleReportRequest request) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new ResourceNotFoundException("举报工单不存在");
        }
        if (report.getStatus() != 0) {
            throw new BusinessException("该工单已处理");
        }

        report.setStatus(request.getStatus());
        report.setHandlerId(adminId);
        report.setHandleNote(request.getHandleNote());
        report.setHandleTime(LocalDateTime.now());
        reportMapper.updateById(report);

        // 如果管理员选择下架（status=1），则下架对应内容
        if (request.getStatus() == 1) {
            if (report.getTargetType() == 1) {
                Question question = questionMapper.selectById(report.getTargetId());
                if (question != null && question.getIsOffline() == 0) {
                    question.setIsOffline(1);
                    questionMapper.updateById(question);
                    sendSystemNotification(question.getUserId(), "你的提问已被下架",
                        "你的提问「" + question.getTitle() + "」因被举报违规已被管理员下架", 5, 1, question.getId());
                }
            } else if (report.getTargetType() == 2) {
                Answer answer = answerMapper.selectById(report.getTargetId());
                if (answer != null && answer.getIsOffline() == 0) {
                    answer.setIsOffline(1);
                    answerMapper.updateById(answer);
                    sendSystemNotification(answer.getUserId(), "你的回答已被下架",
                        "你的回答因被举报违规已被管理员下架", 5, 1, answer.getQuestionId());
                }
            }
        }

        // 如果指定了禁言，则禁言被举报内容的发布者
        if (request.getBanType() != null) {
            Long targetUserId = null;
            if (report.getTargetType() == 1) {
                Question q = questionMapper.selectById(report.getTargetId());
                if (q != null) targetUserId = q.getUserId();
            } else if (report.getTargetType() == 2) {
                Answer a = answerMapper.selectById(report.getTargetId());
                if (a != null) targetUserId = a.getUserId();
            }
            if (targetUserId != null) {
                BanUserRequest banRequest = new BanUserRequest();
                banRequest.setUserId(targetUserId);
                banRequest.setBanType(request.getBanType());
                banRequest.setBanReason(request.getBanReason() != null ? request.getBanReason() : "举报处理");
                banRequest.setSourceType(1); // 来源：举报
                banRequest.setSourceId(reportId);
                banUser(adminId, banRequest);
            }
        }

        // 通知举报人
        String feedbackTitle = request.getStatus() == 1 ? "你举报的内容已被处理" : "你举报的内容已被驳回";
        String feedbackContent = request.getStatus() == 1
            ? "你举报的内容经管理员核实，已做下架处理。感谢你的监督！"
            : "你举报的内容经管理员核实，暂不构成违规。感谢你的监督！";
        Long feedbackLinkId = report.getTargetType() == 1 ? report.getTargetId()
            : answerMapper.selectById(report.getTargetId()) != null
                ? answerMapper.selectById(report.getTargetId()).getQuestionId() : null;
        sendSystemNotification(report.getReporterId(), feedbackTitle, feedbackContent, 7, 1, feedbackLinkId);
    }

    // ========== 回答管理 ==========

    @Override
    public PageResult<AnswerManageVO> pageAllAnswers(Integer pageNum, Integer pageSize, String keyword) {
        Page<Answer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Answer> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                .like(Answer::getContent, keyword)
                .or()
                .eq(Answer::getId, safeParseLong(keyword))
            );
        }
        wrapper.orderByDesc(Answer::getCreateTime);
        IPage<Answer> result = answerMapper.selectPage(page, wrapper);

        List<AnswerManageVO> records = new ArrayList<>();
        for (Answer a : result.getRecords()) {
            AnswerManageVO vo = new AnswerManageVO();
            vo.setId(a.getId());
            vo.setQuestionId(a.getQuestionId());
            vo.setUserId(a.getUserId());
            vo.setContent(a.getContent() != null && a.getContent().length() > 200 ? a.getContent().substring(0, 200) + "..." : a.getContent());
            vo.setLikeCount(a.getLikeCount());
            vo.setCommentCount(a.getCommentCount());
            vo.setIsAccepted(a.getIsAccepted());
            vo.setIsOffline(a.getIsOffline());
            vo.setCreateTime(a.getCreateTime());

            Question q = questionMapper.selectById(a.getQuestionId());
            if (q != null) vo.setQuestionTitle(q.getTitle());

            User u = userMapper.selectById(a.getUserId());
            if (u != null) vo.setUserNickname(u.getNickname());

            records.add(vo);
        }
        return new PageResult<>(result.getTotal(), pageNum, pageSize, records);
    }

    @Override
    @Transactional
    public void toggleAnswerOffline(Long answerId) {
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw new ResourceNotFoundException("回答不存在");
        }
        int newOffline = answer.getIsOffline() == 1 ? 0 : 1;
        answer.setIsOffline(newOffline);
        answerMapper.updateById(answer);

        User user = userMapper.selectById(answer.getUserId());
        if (user != null) {
            String title = newOffline == 1 ? "你的回答已被下架" : "你的回答已恢复上架";
            String content = newOffline == 1
                ? "你的回答因违规已被管理员下架"
                : "你的回答已被管理员恢复上架";
            sendSystemNotification(user.getId(), title, content, 5, 1, answer.getQuestionId());
        }
    }

    // ========== 用户管理 ==========

    @Override
    public PageResult<UserManageVO> pageUsers(Integer pageNum, Integer pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                .like(User::getUsername, keyword)
                .or()
                .like(User::getNickname, keyword)
                .or()
                .like(User::getStudentNo, keyword)
                .or()
                .eq(User::getId, safeParseLong(keyword))
            );
        }
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> result = userMapper.selectPage(page, wrapper);

        List<UserManageVO> records = new ArrayList<>();
        for (User u : result.getRecords()) {
            UserManageVO vo = new UserManageVO();
            vo.setId(u.getId());
            vo.setStudentNo(u.getStudentNo());
            vo.setUsername(u.getUsername());
            vo.setNickname(u.getNickname());
            vo.setAvatar(u.getAvatar());
            vo.setStatus(u.getStatus());
            vo.setIsOffline(u.getIsOffline());
            vo.setBanEndTime(u.getBanEndTime());
            vo.setBanReason(u.getBanReason());
            vo.setCreateTime(u.getCreateTime());
            records.add(vo);
        }
        return new PageResult<>(result.getTotal(), pageNum, pageSize, records);
    }

    @Override
    @Transactional
    public void banUser(Long adminId, BanUserRequest request) {
        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime banEndTime = null;
        switch (request.getBanType()) {
            case 1: banEndTime = now.plusDays(3); break;  // 轻度3天
            case 2: banEndTime = now.plusDays(7); break;  // 中度7天
            case 3: banEndTime = null; break;             // 重度永久
            default: throw new BusinessException("无效的禁言类型");
        }

        // 如果用户已有禁言，比较严重程度，取最重处罚
        LocalDateTime existingBanEnd = user.getBanEndTime();
        boolean shouldApply = true;
        if (existingBanEnd != null) {
            // 当前是临时禁言
            if (banEndTime == null) {
                // 新处罚是永久的 -> 升级，应用
                shouldApply = true;
            } else if (banEndTime.isAfter(existingBanEnd)) {
                // 新处罚时间更长 -> 升级，应用
                shouldApply = true;
            } else {
                // 新处罚更轻 -> 不降级，只记录日志
                shouldApply = false;
            }
        } else if (existingBanEnd == null) {
            // 当前已是永久禁言，不接受降级
            shouldApply = false;
        }

        if (shouldApply) {
            user.setBanEndTime(banEndTime);
            user.setBanReason(request.getBanReason());
            userMapper.updateById(user);
        }

        // 记录处罚日志
        UserBanLog log = new UserBanLog();
        log.setUserId(request.getUserId());
        log.setAdminId(adminId);
        log.setBanType(request.getBanType());
        log.setBanReason(request.getBanReason());
        log.setBanStartTime(now);
        log.setBanEndTime(banEndTime);
        log.setSourceType(request.getSourceType());
        log.setSourceId(request.getSourceId());
        log.setIsActive(1);
        userBanLogMapper.insert(log);

        // 发送通知给用户
        String banDesc = request.getBanType() == 1 ? "轻度禁言3天" : request.getBanType() == 2 ? "中度禁言7天" : "永久禁言";
        sendSystemNotification(user.getId(), "你已被管理员禁言",
            "你因「" + request.getBanReason() + "」被" + banDesc + "。禁言期间无法发布提问、回答和评论，浏览不受影响。", 6, null, null);
    }

    // ========== 处罚日志 ==========

    @Override
    public PageResult<UserBanLogVO> pageBanLogs(Integer pageNum, Integer pageSize, String keyword) {
        Page<UserBanLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserBanLog> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                .like(UserBanLog::getBanReason, keyword)
                .or()
                .eq(UserBanLog::getUserId, safeParseLong(keyword))
                .or()
                .eq(UserBanLog::getId, safeParseLong(keyword))
            );
        }
        wrapper.orderByDesc(UserBanLog::getCreateTime);
        IPage<UserBanLog> result = userBanLogMapper.selectPage(page, wrapper);

        List<UserBanLogVO> records = new ArrayList<>();
        for (UserBanLog log : result.getRecords()) {
            UserBanLogVO vo = new UserBanLogVO();
            vo.setId(log.getId());
            vo.setUserId(log.getUserId());
            vo.setAdminId(log.getAdminId());
            vo.setBanType(log.getBanType());
            vo.setBanReason(log.getBanReason());
            vo.setBanStartTime(log.getBanStartTime());
            vo.setBanEndTime(log.getBanEndTime());
            vo.setSourceType(log.getSourceType());
            vo.setSourceId(log.getSourceId());
            vo.setIsActive(log.getIsActive());
            vo.setCreateTime(log.getCreateTime());

            User u = userMapper.selectById(log.getUserId());
            if (u != null) vo.setUserNickname(u.getNickname());

            Admin admin = adminMapper.selectById(log.getAdminId());
            if (admin != null) vo.setAdminNickname(admin.getNickname());

            records.add(vo);
        }
        return new PageResult<>(result.getTotal(), pageNum, pageSize, records);
    }

    // ========== 通知管理 ==========

    @Override
    public PageResult<NotificationVO> pageNotifications(Long userId, Integer pageNum, Integer pageSize, Integer type) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (type != null) {
            if (type == 0) {
                // 互动提醒：type 0-4
                wrapper.between(Notification::getType, 0, 4);
            } else if (type == 1) {
                // 系统通知：type 5-7
                wrapper.between(Notification::getType, 5, 7);
            }
        }
        wrapper.orderByDesc(Notification::getCreateTime);
        IPage<Notification> result = notificationMapper.selectPage(page, wrapper);

        List<NotificationVO> records = new ArrayList<>();
        for (Notification n : result.getRecords()) {
            NotificationVO vo = buildNotificationVO(n);
            records.add(vo);
        }
        return new PageResult<>(result.getTotal(), pageNum, pageSize, records);
    }

    @Override
    public Long countUnreadNotifications(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markNotificationRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new ResourceNotFoundException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此通知");
        }
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
    }

    @Override
    @Transactional
    public void batchMarkRead(Long userId) {
        notificationMapper.batchMarkRead(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new ResourceNotFoundException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此通知");
        }
        if (notification.getIsDeletable() != null && notification.getIsDeletable() == 0) {
            throw new BusinessException("系统通知不可删除");
        }
        notificationMapper.deleteById(notificationId);
    }

    // ========== 禁言管理 ==========

    @Override
    @Transactional
    public void unbanUser(Long adminId, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }
        if (user.getBanEndTime() == null && user.getBanReason() == null) {
            throw new BusinessException("该用户当前未被禁言");
        }

        // 使用原生 SQL 更新，确保 null 值能正确写入数据库
        userMapper.clearBan(userId);

        // 更新处罚日志状态
        LambdaQueryWrapper<UserBanLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(UserBanLog::getUserId, userId)
                .eq(UserBanLog::getIsActive, 1);
        List<UserBanLog> activeLogs = userBanLogMapper.selectList(logWrapper);
        for (UserBanLog log : activeLogs) {
            log.setIsActive(0);
            userBanLogMapper.updateById(log);
        }

        // 发送通知
        sendNotification(userId, "你的禁言已被解除",
            "管理员已解除你的禁言状态，你现在可以正常发言了。", 6, null, null, null, null, 0);
    }

    // ========== 私有辅助方法 ==========

    private NotificationVO buildNotificationVO(Notification n) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setUserId(n.getUserId());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setType(n.getType());
        vo.setIsRead(n.getIsRead());
        vo.setLinkType(n.getLinkType());
        vo.setLinkId(n.getLinkId());
        vo.setSenderId(n.getSenderId());
        vo.setSenderNickname(n.getSenderNickname());
        vo.setIsDeletable(n.getIsDeletable());
        vo.setCreateTime(n.getCreateTime());
        return vo;
    }

    private ReportVO buildReportVO(Report r) {
        ReportVO vo = new ReportVO();
        vo.setId(r.getId());
        vo.setReporterId(r.getReporterId());
        vo.setTargetType(r.getTargetType());
        vo.setTargetId(r.getTargetId());
        vo.setReason(r.getReason());
        vo.setStatus(r.getStatus());
        vo.setHandlerId(r.getHandlerId());
        vo.setHandleNote(r.getHandleNote());
        vo.setHandleTime(r.getHandleTime());
        vo.setCreateTime(r.getCreateTime());

        // 举报人信息
        User reporter = userMapper.selectById(r.getReporterId());
        if (reporter != null) vo.setReporterNickname(reporter.getNickname());

        // 处理人信息
        if (r.getHandlerId() != null) {
            Admin handler = adminMapper.selectById(r.getHandlerId());
            if (handler != null) vo.setHandlerNickname(handler.getNickname());
        }

        // 被举报内容信息 + 完整问答上下文
        Question contextQuestion = null;
        if (r.getTargetType() == 1) {
            // 举报提问：获取该提问
            contextQuestion = questionMapper.selectById(r.getTargetId());
            if (contextQuestion != null) {
                vo.setTargetTitle(contextQuestion.getTitle());
                vo.setTargetContent(contextQuestion.getContent());
            }
            vo.setReportedAnswerId(null);
        } else if (r.getTargetType() == 2) {
            // 举报回答：获取该回答所属的提问
            Answer a = answerMapper.selectById(r.getTargetId());
            if (a != null) {
                vo.setTargetTitle("回答 #" + a.getId());
                vo.setTargetContent(a.getContent());
                contextQuestion = questionMapper.selectById(a.getQuestionId());
            }
            vo.setReportedAnswerId(r.getTargetId());
        }

        // 填充完整问答上下文
        if (contextQuestion != null) {
            vo.setQuestionId(contextQuestion.getId());
            vo.setQuestionTitle(contextQuestion.getTitle());
            vo.setQuestionContent(contextQuestion.getContent());
            vo.setQuestionCreateTime(contextQuestion.getCreateTime());

            User qUser = userMapper.selectById(contextQuestion.getUserId());
            if (qUser != null) vo.setQuestionUserNickname(qUser.getNickname());

            // 获取该问题下所有回答（含已下架）
            LambdaQueryWrapper<Answer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.eq(Answer::getQuestionId, contextQuestion.getId())
                    .orderByDesc(Answer::getIsAccepted)
                    .orderByDesc(Answer::getLikeCount);
            List<Answer> answers = answerMapper.selectList(answerWrapper);

            List<ReportVO.AnswerContextVO> answerVOs = new ArrayList<>();
            for (Answer answer : answers) {
                ReportVO.AnswerContextVO avo = new ReportVO.AnswerContextVO();
                avo.setId(answer.getId());
                avo.setUserId(answer.getUserId());
                avo.setContent(answer.getContent());
                avo.setLikeCount(answer.getLikeCount());
                avo.setCommentCount(answer.getCommentCount());
                avo.setIsAccepted(answer.getIsAccepted());
                avo.setIsOffline(answer.getIsOffline());
                avo.setCreateTime(answer.getCreateTime());
                // 高亮被举报的回答
                avo.setIsReported(r.getTargetType() == 2 && answer.getId().equals(r.getTargetId()));

                User au = userMapper.selectById(answer.getUserId());
                if (au != null) {
                    avo.setUserNickname(au.getNickname());
                    avo.setUserAvatar(au.getAvatar());
                }
                answerVOs.add(avo);
            }
            vo.setAnswers(answerVOs);
        }

        return vo;
    }

    private void sendNotification(Long userId, String title, String content, Integer type,
            Integer linkType, Long linkId, Long senderId, String senderNickname, Integer isDeletable) {
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
        notification.setIsDeletable(isDeletable != null ? isDeletable : 1);
        notificationMapper.insert(notification);
    }

    // 简化调用：互动提醒（可删除，有发送者信息）
    private void sendInteractionNotification(Long userId, String title, String content, Integer type,
            Integer linkType, Long linkId, Long senderId, String senderNickname) {
        sendNotification(userId, title, content, type, linkType, linkId, senderId, senderNickname, 1);
    }

    // 简化调用：系统通知（不可删除）
    private void sendSystemNotification(Long userId, String title, String content, Integer type,
            Integer linkType, Long linkId) {
        sendNotification(userId, title, content, type, linkType, linkId, null, null, 0);
    }

    private Long safeParseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return -1L; // 不会匹配任何ID
        }
    }
}