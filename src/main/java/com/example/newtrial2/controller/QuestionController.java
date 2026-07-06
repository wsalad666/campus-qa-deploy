package com.example.newtrial2.controller;

import com.example.newtrial2.common.JwtUtil;
import com.example.newtrial2.common.Result;
import com.example.newtrial2.dto.request.*;
import com.example.newtrial2.dto.response.NotificationVO;
import com.example.newtrial2.dto.response.PageResult;
import com.example.newtrial2.dto.response.QuestionDetailResponse;
import com.example.newtrial2.dto.response.QuestionVO;
import com.example.newtrial2.dto.response.SimilarQuestionVO;
import com.example.newtrial2.service.AdminService;
import com.example.newtrial2.service.CommentService;
import com.example.newtrial2.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "问答模块", description = "提问、回答、评论、点赞、采纳、举报、通知")
@RestController
@RequestMapping("/api/qa")
public class QuestionController {

    private final QuestionService questionService;
    private final CommentService commentService;
    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    public QuestionController(QuestionService questionService, CommentService commentService,
                              AdminService adminService, JwtUtil jwtUtil) {
        this.questionService = questionService;
        this.commentService = commentService;
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "发布提问")
    @PostMapping("/question")
    public Result<Void> publishQuestion(HttpServletRequest httpRequest,
                                         @Valid @RequestBody QuestionRequest request) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        questionService.publishQuestion(userId, request);
        return Result.success();
    }

    @Operation(summary = "分页查询问题列表")
    @GetMapping("/question/list")
    public Result<PageResult<QuestionVO>> pageQuestions(
            HttpServletRequest httpRequest,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "课程分类ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序方式(time/hot)") @RequestParam(defaultValue = "time") String sort,
            @Parameter(description = "用户ID过滤") @RequestParam(required = false) Long userId) {
        Long currentUserId = resolveUserId(httpRequest);
        return Result.success(questionService.pageQuestions(courseId, keyword, sort, pageNum, pageSize, currentUserId, userId));
    }

    @Operation(summary = "问题详情")
    @GetMapping("/question/{questionId}")
    public Result<QuestionDetailResponse> getQuestionDetail(
            HttpServletRequest httpRequest,
            @Parameter(description = "问题ID") @PathVariable Long questionId) {
        Long userId = resolveUserId(httpRequest);
        return Result.success(questionService.getQuestionDetail(questionId, userId));
    }

    @Operation(summary = "回答问题")
    @PostMapping("/answer")
    public Result<Void> answerQuestion(HttpServletRequest httpRequest,
                                        @Valid @RequestBody AnswerRequest request) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        questionService.answerQuestion(userId, request);
        return Result.success();
    }

    @Operation(summary = "评论/回复")
    @PostMapping("/comment")
    public Result<Void> addComment(HttpServletRequest httpRequest,
                                    @Valid @RequestBody CommentRequest request) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        commentService.addComment(userId, request);
        return Result.success();
    }

    @Operation(summary = "删除评论（学生自主删除）")
    @DeleteMapping("/comment/{commentId}")
    public Result<Void> deleteComment(HttpServletRequest httpRequest,
                                       @Parameter(description = "评论ID") @PathVariable Long commentId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        commentService.deleteComment(userId, commentId);
        return Result.success();
    }

    @Operation(summary = "点赞/取消点赞回答")
    @PostMapping("/like/{answerId}")
    public Result<Boolean> likeAnswer(HttpServletRequest httpRequest,
                                    @Parameter(description = "回答ID") @PathVariable Long answerId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(questionService.likeAnswer(userId, answerId));
    }

    @Operation(summary = "点赞问题")
    @PostMapping("/question/{questionId}/like")
    public Result<Boolean> likeQuestion(HttpServletRequest httpRequest,
                                      @Parameter(description = "问题ID") @PathVariable Long questionId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(questionService.likeQuestion(userId, questionId));
    }

    @Operation(summary = "采纳回答")
    @PostMapping("/accept/{answerId}")
    public Result<Void> acceptAnswer(HttpServletRequest httpRequest,
                                      @Parameter(description = "回答ID") @PathVariable Long answerId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        questionService.acceptAnswer(userId, answerId);
        return Result.success();
    }

    @Operation(summary = "删除提问（无回答时彻底删除）")
    @DeleteMapping("/question/{questionId}")
    public Result<Void> deleteQuestion(HttpServletRequest httpRequest,
                                        @Parameter(description = "提问ID") @PathVariable Long questionId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        questionService.deleteQuestion(userId, questionId);
        return Result.success();
    }

    @Operation(summary = "关闭提问（已有回答时）")
    @PutMapping("/question/{questionId}/close")
    public Result<Void> closeQuestion(HttpServletRequest httpRequest,
                                       @Parameter(description = "提问ID") @PathVariable Long questionId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        questionService.closeQuestion(userId, questionId);
        return Result.success();
    }

    @Operation(summary = "设为私密（已有回答时）")
    @PutMapping("/question/{questionId}/hide")
    public Result<Void> hideQuestion(HttpServletRequest httpRequest,
                                      @Parameter(description = "提问ID") @PathVariable Long questionId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        questionService.hideQuestion(userId, questionId);
        return Result.success();
    }

    @Operation(summary = "获取相似问题推荐")
    @GetMapping("/question/similar")
    public Result<List<SimilarQuestionVO>> getSimilarQuestions(
            @Parameter(description = "课程ID") @RequestParam Long courseId,
            @Parameter(description = "排除问题ID") @RequestParam(required = false) Long excludeId,
            @Parameter(description = "标题") @RequestParam(required = false) String title,
            @Parameter(description = "内容") @RequestParam(required = false) String content,
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "6") int limit) {
        return Result.success(questionService.getSimilarQuestions(courseId, excludeId, title, content, limit));
    }

    @Operation(summary = "举报")
    @PostMapping("/report")
    public Result<Void> submitReport(HttpServletRequest httpRequest,
                                     @Valid @RequestBody ReportRequest request) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        adminService.submitReport(userId, request);
        return Result.success();
    }

    @Operation(summary = "获取我的通知列表")
    @GetMapping("/notification/list")
    public Result<PageResult<NotificationVO>> pageNotifications(
            HttpServletRequest httpRequest,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "通知类型(0互动提醒 1系统通知)") @RequestParam(required = false) Integer type) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(adminService.pageNotifications(userId, pageNum, pageSize, type));
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/notification/unread-count")
    public Result<Long> countUnreadNotifications(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(adminService.countUnreadNotifications(userId));
    }

    @Operation(summary = "标记通知已读")
    @PutMapping("/notification/{notificationId}/read")
    public Result<Void> markNotificationRead(
            HttpServletRequest httpRequest,
            @Parameter(description = "通知ID") @PathVariable Long notificationId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        adminService.markNotificationRead(notificationId, userId);
        return Result.success();
    }

    @Operation(summary = "批量标记所有通知已读")
    @PutMapping("/notification/read-all")
    public Result<Void> batchMarkRead(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        adminService.batchMarkRead(userId);
        return Result.success();
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/notification/{notificationId}")
    public Result<Void> deleteNotification(
            HttpServletRequest httpRequest,
            @Parameter(description = "通知ID") @PathVariable Long notificationId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        adminService.deleteNotification(notificationId, userId);
        return Result.success();
    }

    /**
     * 从请求中解析用户ID，优先从拦截器设置的属性获取，否则从JWT token中解析
     */
    private Long resolveUserId(HttpServletRequest request) {
        // 先尝试从拦截器设置的属性获取
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr instanceof Long) {
            return (Long) userIdAttr;
        }
        // 拦截器未设置（被排除的路径），手动解析JWT token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
                return jwtUtil.getUserIdFromToken(token);
            }
        }
        return null;
    }
}