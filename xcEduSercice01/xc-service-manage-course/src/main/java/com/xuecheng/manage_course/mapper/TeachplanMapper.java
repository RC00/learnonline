package com.xuecheng.manage_course.mapper;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface TeachplanMapper {
    public TeachplanNode selectList(String courseId);
}
