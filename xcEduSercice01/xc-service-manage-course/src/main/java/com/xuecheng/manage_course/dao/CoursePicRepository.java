package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CoursePic;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 课程图片管理
 */
public interface CoursePicRepository extends JpaRepository<CoursePic, String> {
    /*根据课程id删除课程图片*/
    public Long deleteByCourseid(String courseId);//根据返回的数值(影响行数)来判断是否删除成功
}
