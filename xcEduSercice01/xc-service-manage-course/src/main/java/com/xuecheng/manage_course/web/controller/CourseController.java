package com.xuecheng.manage_course.web.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.XcOauth2Util;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/course")
public class CourseController implements CourseControllerApi {
    @Autowired
    private CourseService courseService;

    /**
     * 查询课程计划
     *
     * @param courseId
     * @return
     */
    @PreAuthorize("hasAuthority('course_teachplan_list')")
    @Override
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachplanList(@PathVariable("courseId") String courseId) {
        return courseService.findTeachplanList(courseId);
    }

    /**
     * 添加课程计划
     *
     * @param teachplan
     * @return
     */
    @Override
    @PostMapping("/teachplan/add")
    public ResponseResult addTeachplan(@RequestBody Teachplan teachplan) {
        return courseService.addTeachplan(teachplan);
    }

    /**
     * 课程列表分页查询
     *
     * @param page
     * @param size
     * @param courseListRequest
     * @return
     */

    /*方法上添加权限控制*/
    @PreAuthorize("hasAuthority('course_find_list')")
    @Override
    @GetMapping("/coursebase/list/{page}/{size}")
    public QueryResponseResult<CourseInfo> findCourseList(@PathVariable("page") Integer page,
                                                          @PathVariable("size") Integer size,
                                                          CourseListRequest courseListRequest) {//先使用静态数据测试

        /**将companyId的静态数据改为动态获取
         * String companyId = "1";
         */

        //调用工具类取出用户信息
        XcOauth2Util xcOauth2Util = new XcOauth2Util();
        //获取request对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        XcOauth2Util.UserJwt userJwt = xcOauth2Util.getUserJwtFromHeader(request);
        if (userJwt==null){
            //没登录
            ExceptionCast.cast(CommonCode.UNAUTHENTICATED);
        }
        String companyId = userJwt.getCompanyId();
        return courseService.findCourseList(companyId, page, size, courseListRequest);
    }

    /**
     * 添加课程的基础信息
     *
     * @param courseBase
     * @return
     */
    @Override
    @PostMapping("/coursebase/add")
    public AddCourseResult addCourseBase(@RequestBody CourseBase courseBase) {
        return courseService.addCourseBase(courseBase);
    }

    /**
     * 获取课程基础信息
     *
     * @param courseId
     * @return
     * @throws RuntimeException
     */

    /*方法上添加权限控制*/
    //@PreAuthorize("hasAuthority('course_get_baseinfo')")
    @Override
    @GetMapping("/coursebase/get/{courseId}")
    public CourseBase getCourseBase(@PathVariable("courseId") String courseId) {
        return courseService.getCoursebaseById(courseId);
    }

    /**
     * 更新课程基础信息
     *
     * @param id
     * @param courseBase
     * @return
     */
    @Override
    @PutMapping("/coursebase/update/{id}")
    public ResponseResult updateCourseBase(@PathVariable("id") String id, @RequestBody CourseBase courseBase) {
        return courseService.updateCoursebase(id, courseBase);
    }

    /**
     * 查询课程营销信息
     *
     * @param courseId
     * @return
     */
    @Override
    @GetMapping("/coursemarket/get/{courseId}")
    public CourseMarket getCourseMarketById(@PathVariable("courseId") String courseId) {
        return courseService.getCourseMarketById(courseId);
    }

    /**
     * 更新课程营销信息
     *
     * @param id
     * @param courseMarket
     * @return
     */
    @Override
    @PostMapping("/coursemarket/update/{id}")
    public ResponseResult updateCourseMarketById(@PathVariable("id") String id, @RequestBody CourseMarket courseMarket) {

        CourseMarket market = courseService.updateCourseMarketById(id, courseMarket);
        if (market != null) {
            return ResponseResult.SUCCESS();
        }
        return ResponseResult.FAIL();
    }

    /**
     * 添加课程图片
     *
     * @param courseId
     * @param pic
     * @return
     */
    @Override
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId") String courseId,
                                       @RequestParam("pic") String pic) {
        //保存课程图片
        return courseService.saveCoursePic(courseId, pic);
    }

    /**
     * 删除课程图片
     *
     * @param courseId
     * @return
     */
    @Override
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {

        return courseService.deleteCoursePic(courseId);
    }

    /**
     * 查询==>展示课程图片
     *
     * @param courseId
     * @return
     */
    @Override
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findCoursePic(@PathVariable("courseId") String courseId) {
        return courseService.findCoursepic(courseId);
    }

    /**
     * 课程视图查询
     *
     * @param id
     * @return
     */
    @Override
    @GetMapping("/courseview/{id}")
    public CourseView courseview(@PathVariable("id") String id) {
        return courseService.getCoruseView(id);
    }

    /**
     * 远程调用cms==>课程预览
     *
     * @param id
     * @return
     */
    @Override
    @PostMapping("/preview/{id}")
    public CoursePublishResult preview(@PathVariable("id") String id) {
        return courseService.preview(id);
    }

    /**
     * 页面的一键发布
     *
     * @param id
     * @return
     */
    @Override
    @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable("id") String id) {
        return courseService.publish(id);
    }

    /**
     * 保存媒资信息
     *
     * @param teachplanMedia
     * @return
     */
    @Override
    @PostMapping("/savemedia")
    public ResponseResult saveMedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.saveMedia(teachplanMedia);
    }


}
