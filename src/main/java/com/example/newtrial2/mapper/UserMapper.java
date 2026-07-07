package com.example.newtrial2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.newtrial2.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    Long countTotalUsers();

    Long countTodayNewUsers();

    @Update("UPDATE user SET ban_end_time = NULL, ban_reason = NULL WHERE id = #{userId}")
    void clearBan(Long userId);
}