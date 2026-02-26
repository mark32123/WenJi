package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Pojo.SiteImage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SiteImageMapper extends BaseMapper<SiteImage> {
    // 根据景点ID查询所有图片
    List<SiteImage> findBySiteId(String siteId);
}
