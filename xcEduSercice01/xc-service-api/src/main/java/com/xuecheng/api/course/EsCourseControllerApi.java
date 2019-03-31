package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

/**
 * 搜索服务
 */
@Api(value = "课程搜索", description = "课程搜索", tags = {"课程搜索"})
public interface EsCourseControllerApi {

    @ApiOperation("课程搜索")
    public QueryResponseResult<CoursePub> list(Integer page, Integer size, CourseSearchParam courseSearchParam);

    @ApiOperation("根据id查看课程信息")
    public Map<String, CoursePub> getAll(String id);

    @ApiOperation("根据课程计划查询媒资信息")
    public TeachplanMediaPub getMedia(String teachplanId);
}
