package com.xuecheng.manage_course.mapper;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CategoryMapper {
    /**
     * 查询分类
     *
     * @return
     */
    public CategoryNode selectList();
}
