package com.example.newtrial2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.newtrial2.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    Long countTotalQuestions();

    Long countTodayQuestions();

    @Select("SELECT * FROM question WHERE course_id = #{courseId} AND is_offline = 0 AND status IN (0, 1) AND (#{excludeId} IS NULL OR id != #{excludeId}) AND MATCH(title, content) AGAINST(#{keyword} IN BOOLEAN MODE) LIMIT 15")
    List<Question> searchSimilar(@Param("courseId") Long courseId, @Param("excludeId") Long excludeId, @Param("keyword") String keyword);
}