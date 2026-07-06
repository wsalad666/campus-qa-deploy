package com.example.newtrial2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.newtrial2.dto.response.CourseVO;
import com.example.newtrial2.entity.Course;
import com.example.newtrial2.entity.UserCourseFavorite;
import com.example.newtrial2.mapper.CourseMapper;
import com.example.newtrial2.mapper.UserCourseFavoriteMapper;
import com.example.newtrial2.service.UserCourseFavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserCourseFavoriteServiceImpl implements UserCourseFavoriteService {

    private final UserCourseFavoriteMapper favoriteMapper;
    private final CourseMapper courseMapper;

    public UserCourseFavoriteServiceImpl(UserCourseFavoriteMapper favoriteMapper, CourseMapper courseMapper) {
        this.favoriteMapper = favoriteMapper;
        this.courseMapper = courseMapper;
    }

    @Override
    public List<CourseVO> getUserFavoriteCourses(Long userId) {
        LambdaQueryWrapper<UserCourseFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCourseFavorite::getUserId, userId)
                .orderByAsc(UserCourseFavorite::getCreateTime);
        List<UserCourseFavorite> favorites = favoriteMapper.selectList(wrapper);

        List<CourseVO> result = new ArrayList<>();
        for (UserCourseFavorite f : favorites) {
            Course course = courseMapper.selectById(f.getCourseId());
            if (course != null) {
                CourseVO vo = new CourseVO();
                vo.setId(course.getId());
                vo.setName(course.getName());
                vo.setDescription(course.getDescription());
                vo.setIcon(course.getIcon());
                vo.setSortOrder(course.getSortOrder());
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void updateFavoriteCourses(Long userId, List<Long> courseIds) {
        // 删除旧的
        LambdaQueryWrapper<UserCourseFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCourseFavorite::getUserId, userId);
        favoriteMapper.delete(wrapper);

        // 插入新的
        if (courseIds != null && !courseIds.isEmpty()) {
            List<UserCourseFavorite> list = courseIds.stream().map(courseId -> {
                UserCourseFavorite f = new UserCourseFavorite();
                f.setUserId(userId);
                f.setCourseId(courseId);
                return f;
            }).collect(Collectors.toList());
            for (UserCourseFavorite f : list) {
                favoriteMapper.insert(f);
            }
        }
    }
}