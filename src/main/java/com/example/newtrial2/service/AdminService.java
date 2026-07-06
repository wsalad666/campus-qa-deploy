package com.example.newtrial2.service;

import com.example.newtrial2.dto.request.*;
import com.example.newtrial2.dto.response.*;
import com.example.newtrial2.entity.Course;

import java.util.List;

public interface AdminService {

    AdminLoginResponse login(AdminLoginRequest request);

    List<Course> getAllCourses(String keyword);

    void addCourse(CourseRequest request);

    void updateCourse(Long courseId, CourseRequest request);

    void deleteCourse(Long courseId);

    PageResult<QuestionVO> pageAllQuestions(Integer pageNum, Integer pageSize, String keyword);

    PageResult<ResourceVO> pageAllResources(Integer pageNum, Integer pageSize, String keyword);

    void toggleQuestionOffline(Long questionId);

    void toggleResourceOffline(Long resourceId);

    StatisticsResponse getStatistics();

    // ========== 举报工单管理 ==========
    void submitReport(Long userId, ReportRequest request);

    PageResult<ReportVO> pageReports(Integer pageNum, Integer pageSize, Integer status, String keyword);

    Long countPendingReports();

    ReportVO getReportDetail(Long reportId);

    void handleReport(Long adminId, Long reportId, HandleReportRequest request);

    // ========== 回答管理 ==========
    PageResult<AnswerManageVO> pageAllAnswers(Integer pageNum, Integer pageSize, String keyword);

    void toggleAnswerOffline(Long answerId);

    // ========== 用户管理 ==========
    PageResult<UserManageVO> pageUsers(Integer pageNum, Integer pageSize, String keyword);

    void banUser(Long adminId, BanUserRequest request);

    // ========== 处罚日志 ==========
    PageResult<UserBanLogVO> pageBanLogs(Integer pageNum, Integer pageSize, String keyword);

    // ========== 通知管理 ==========
    PageResult<NotificationVO> pageNotifications(Long userId, Integer pageNum, Integer pageSize, Integer type);

    Long countUnreadNotifications(Long userId);

    void markNotificationRead(Long notificationId, Long userId);

    void batchMarkRead(Long userId);

    void deleteNotification(Long notificationId, Long userId);

    // ========== 禁言管理 ==========
    void unbanUser(Long adminId, Long userId);
}