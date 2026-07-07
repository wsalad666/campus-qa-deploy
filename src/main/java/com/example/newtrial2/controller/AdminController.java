package com.example.newtrial2.controller;

import com.example.newtrial2.common.AdminAuthInterceptor;
import com.example.newtrial2.common.Result;
import com.example.newtrial2.dto.request.*;
import com.example.newtrial2.dto.response.*;
import com.example.newtrial2.entity.Course;
import com.example.newtrial2.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员后台模块", description = "课程分类管理、违规内容管控、数据统计、举报工单、禁言处罚")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return Result.success(adminService.login(request));
    }

    @Operation(summary = "获取所有课程分类")
    @GetMapping("/course/list")
    public Result<List<Course>> getAllCourses(
            @Parameter(description = "课程名称关键词") @RequestParam(required = false) String keyword) {
        return Result.success(adminService.getAllCourses(keyword));
    }

    @Operation(summary = "新增课程分类")
    @PostMapping("/course")
    public Result<Void> addCourse(@Valid @RequestBody CourseRequest request) {
        adminService.addCourse(request);
        return Result.success();
    }

    @Operation(summary = "编辑课程分类")
    @PutMapping("/course/{courseId}")
    public Result<Void> updateCourse(
            @Parameter(description = "课程分类ID") @PathVariable Long courseId,
            @Valid @RequestBody CourseRequest request) {
        adminService.updateCourse(courseId, request);
        return Result.success();
    }

    @Operation(summary = "删除课程分类")
    @DeleteMapping("/course/{courseId}")
    public Result<Void> deleteCourse(
            @Parameter(description = "课程分类ID") @PathVariable Long courseId) {
        adminService.deleteCourse(courseId);
        return Result.success();
    }

    @Operation(summary = "分页查询全部提问（支持模糊搜索）")
    @GetMapping("/question/list")
    public Result<PageResult<QuestionVO>> pageAllQuestions(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return Result.success(adminService.pageAllQuestions(pageNum, pageSize, keyword));
    }

    @Operation(summary = "分页查询全部资源（支持模糊搜索）")
    @GetMapping("/resource/list")
    public Result<PageResult<ResourceVO>> pageAllResources(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return Result.success(adminService.pageAllResources(pageNum, pageSize, keyword));
    }

    @Operation(summary = "下架/上架问题")
    @PutMapping("/question/{questionId}/offline")
    public Result<Void> toggleQuestionOffline(
            @Parameter(description = "问题ID") @PathVariable Long questionId) {
        adminService.toggleQuestionOffline(questionId);
        return Result.success();
    }

    @Operation(summary = "下架/上架资源")
    @PutMapping("/resource/{resourceId}/offline")
    public Result<Void> toggleResourceOffline(
            @Parameter(description = "资源ID") @PathVariable Long resourceId) {
        adminService.toggleResourceOffline(resourceId);
        return Result.success();
    }

    @Operation(summary = "数据统计")
    @GetMapping("/statistics")
    public Result<StatisticsResponse> getStatistics() {
        return Result.success(adminService.getStatistics());
    }

    // ========== 回答管理 ==========

    @Operation(summary = "分页查询全部回答（支持模糊搜索）")
    @GetMapping("/answer/list")
    public Result<PageResult<AnswerManageVO>> pageAllAnswers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return Result.success(adminService.pageAllAnswers(pageNum, pageSize, keyword));
    }

    @Operation(summary = "下架/上架回答")
    @PutMapping("/answer/{answerId}/offline")
    public Result<Void> toggleAnswerOffline(
            @Parameter(description = "回答ID") @PathVariable Long answerId) {
        adminService.toggleAnswerOffline(answerId);
        return Result.success();
    }

    // ========== 用户管理 ==========

    @Operation(summary = "分页查询用户列表（支持模糊搜索）")
    @GetMapping("/user/list")
    public Result<PageResult<UserManageVO>> pageUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return Result.success(adminService.pageUsers(pageNum, pageSize, keyword));
    }

    @Operation(summary = "禁言用户")
    @PostMapping("/user/ban")
    public Result<Void> banUser(
            @Valid @RequestBody BanUserRequest request,
            HttpServletRequest httpRequest) {
        Long adminId = (Long) httpRequest.getAttribute(AdminAuthInterceptor.ADMIN_ID_KEY);
        adminService.banUser(adminId, request);
        return Result.success();
    }

    @Operation(summary = "取消禁言")
    @PostMapping("/user/{userId}/unban")
    public Result<Void> unbanUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            HttpServletRequest httpRequest) {
        Long adminId = (Long) httpRequest.getAttribute(AdminAuthInterceptor.ADMIN_ID_KEY);
        adminService.unbanUser(adminId, userId);
        return Result.success();
    }

    // ========== 举报工单管理 ==========

    @Operation(summary = "分页查询举报工单（支持模糊搜索）")
    @GetMapping("/report/list")
    public Result<PageResult<ReportVO>> pageReports(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return Result.success(adminService.pageReports(pageNum, pageSize, status, keyword));
    }

    @Operation(summary = "获取待处理举报数量")
    @GetMapping("/report/pending-count")
    public Result<Long> countPendingReports() {
        return Result.success(adminService.countPendingReports());
    }

    @Operation(summary = "查询举报工单详情（含被举报内容）")
    @GetMapping("/report/{reportId}")
    public Result<ReportVO> getReportDetail(
            @Parameter(description = "举报工单ID") @PathVariable Long reportId) {
        return Result.success(adminService.getReportDetail(reportId));
    }

    @Operation(summary = "处理举报工单")
    @PutMapping("/report/{reportId}/handle")
    public Result<Void> handleReport(
            @Parameter(description = "举报工单ID") @PathVariable Long reportId,
            @Valid @RequestBody HandleReportRequest request,
            HttpServletRequest httpRequest) {
        Long adminId = (Long) httpRequest.getAttribute(AdminAuthInterceptor.ADMIN_ID_KEY);
        adminService.handleReport(adminId, reportId, request);
        return Result.success();
    }

    // ========== 处罚日志 ==========

    @Operation(summary = "分页查询处罚日志（支持模糊搜索）")
    @GetMapping("/ban-log/list")
    public Result<PageResult<UserBanLogVO>> pageBanLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return Result.success(adminService.pageBanLogs(pageNum, pageSize, keyword));
    }
}