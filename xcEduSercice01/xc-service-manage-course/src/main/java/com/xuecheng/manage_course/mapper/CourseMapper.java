package com.xuecheng.manage_course.mapper;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CourseMapper {
    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    CourseBase findCourseBaseById(String id);

    /**
     * 通过查询条件分页查询
     *
     * @param courseListRequest
     * @return
     */
    Page<CourseInfo> findCourseListPage(CourseListRequest courseListRequest);
}
