package com.example.newtrial2.controller;

import com.example.newtrial2.common.Result;
import com.example.newtrial2.dto.response.CourseVO;
import com.example.newtrial2.service.UserCourseFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户常用课程", description = "用户自定义常用课程管理")
@RestController
@RequestMapping("/api/user/course")
public class UserCourseFavoriteController {

    private final UserCourseFavoriteService favoriteService;

    public UserCourseFavoriteController(UserCourseFavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Operation(summary = "获取用户常用课程列表")
    @GetMapping("/favorites")
    public Result<List<CourseVO>> getFavorites(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(favoriteService.getUserFavoriteCourses(userId));
    }

    @Operation(summary = "更新用户常用课程（全量替换）")
    @PutMapping("/favorites")
    public Result<Void> updateFavorites(
            HttpServletRequest httpRequest,
            @RequestBody List<Long> courseIds) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        favoriteService.updateFavoriteCourses(userId, courseIds);
        return Result.success();
    }
}