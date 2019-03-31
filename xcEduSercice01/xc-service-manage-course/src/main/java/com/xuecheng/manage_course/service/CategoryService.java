package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.manage_course.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 课程分类查询
 */
@Service
public class CategoryService {

    @Autowired
    CategoryMapper categoryMapper;

    /**
     * 查询分类
     *
     * @return
     */
    public CategoryNode findList() {
        return categoryMapper.selectList();
    }
}
