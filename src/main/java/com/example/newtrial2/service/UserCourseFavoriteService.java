package com.example.newtrial2.service;

import com.example.newtrial2.dto.response.CourseVO;

import java.util.List;

public interface UserCourseFavoriteService {

    /** 获取用户常用课程列表 */
    List<CourseVO> getUserFavoriteCourses(Long userId);

    /** 更新用户常用课程（全量替换） */
    void updateFavoriteCourses(Long userId, List<Long> courseIds);
}