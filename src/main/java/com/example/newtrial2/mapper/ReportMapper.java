package com.example.newtrial2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.newtrial2.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReportMapper extends BaseMapper<Report> {
    @Select("SELECT COUNT(*) FROM report WHERE status = 0")
    Long countPending();
}