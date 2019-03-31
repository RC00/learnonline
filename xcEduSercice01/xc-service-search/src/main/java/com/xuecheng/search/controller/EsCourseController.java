package com.xuecheng.search.controller;

import com.xuecheng.api.course.EsCourseControllerApi;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.search.service.EsCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/search/course")
public class EsCourseController implements EsCourseControllerApi {
    @Autowired
    private EsCourseService esCourseService;

    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult<CoursePub> list(@PathVariable("page") Integer page, @PathVariable("size") Integer size, CourseSearchParam courseSearchParam) {

        QueryResponseResult<CoursePub> list = esCourseService.list(page, size, courseSearchParam);
        System.out.println(list);
        return list;
    }

    @Override
    @GetMapping("/getall/{id}")
    public Map<String, CoursePub> getAll(@PathVariable("id") String id) {
        return esCourseService.getAll(id);
    }


    @Override
    @GetMapping("/getmedia/{teachplanId}")
    public TeachplanMediaPub getMedia(@PathVariable("teachplanId") String teachplanId) {
        //将课程计划id放在数组中，为调用service作准备
        String[] teachplanIds = new String[]{teachplanId};
        //通过service查询es获取课程的媒资信息
        QueryResponseResult<TeachplanMediaPub> queryResponseResult = esCourseService.getMedia(teachplanIds);
        QueryResult<TeachplanMediaPub> queryResult = queryResponseResult.getQueryResult();
        if (queryResult != null && queryResult.getList() != null && queryResult.getList().size() > 0) {
            //返回课程计划对应的课程媒资
            return queryResult.getList().get(0);
        }
        return new TeachplanMediaPub();
    }
}
