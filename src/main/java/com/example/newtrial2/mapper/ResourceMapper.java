package com.example.newtrial2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.newtrial2.entity.Resource;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    Long countTotalResources();

    Long countTotalDownloads();
}