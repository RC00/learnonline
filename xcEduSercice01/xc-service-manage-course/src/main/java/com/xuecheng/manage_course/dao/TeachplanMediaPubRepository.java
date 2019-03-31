package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeachplanMediaPubRepository extends JpaRepository<TeachplanMediaPub, String> {
    //根据课程id删除课程计划媒资信息
    Long deleteByCourseId(String courseId);
    //从TeachplanMedia查询课程计划媒资信息

}
