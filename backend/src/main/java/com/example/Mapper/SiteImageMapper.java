package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Pojo.SiteImage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SiteImageMapper extends BaseMapper<SiteImage> {
    /**
     * 根据站点ID查询所有图片
     * @param siteId 站点ID
     * @return 图片列表
     */
    List<SiteImage> findBySiteId(String siteId);
}
