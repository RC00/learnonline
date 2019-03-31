package com.xuecheng.manage_course.dao;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseView;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.manage_course.mapper.CourseMapper;
import com.xuecheng.manage_course.service.CourseService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDao {
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    CourseService courseService;

    @Test
    public void testCourseBaseRepository() {
        Optional<CourseBase> optional = courseBaseRepository.findById("402885816240d276016240f7e5000002");
        if (optional.isPresent()) {
            CourseBase courseBase = optional.get();
            System.out.println(courseBase);
        }

    }

    @Test
    public void testCourseMapper() {
        CourseBase courseBase = courseMapper.findCourseBaseById("402885816240d276016240f7e5000002");
        System.out.println(courseBase);

    }

    //测试分页
    @Test
    public void testPageHelper() {
        PageHelper.startPage(1, 10);//查询第一页，每页显示10条记录
        CourseListRequest courseListRequest = new CourseListRequest();
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        List<CourseInfo> content = courseListPage.getResult();
        for (CourseInfo courseInfo : content) {
            System.out.println(courseInfo);
        }
    }

    @Test
    public void testPageHelper1() {
        //查询第1页，每页显示10条记录
        PageHelper.startPage(1, 1);
        CourseListRequest courseListRequest = new CourseListRequest();
       Page<CourseInfo> courseList = courseMapper.findCourseListPage(courseListRequest);
        List<CourseInfo> result = courseList.getResult();
        long total = courseList.getTotal();

        System.out.println("================" + JSON.toJSONString(result) + "==================");
    }

    //负载均衡调用
    @Test
    public void testRibbon() {
        //服务id
        String serviceId = "XC-SERVICE-MANAGE-CMS";
        for (int i = 0; i < 10; i++) {
            //通过id调用
            ResponseEntity<CmsPage> forEntity = restTemplate.getForEntity("http://" + serviceId + "/cms/page/get/5abefd525b05aa293098fca6", CmsPage.class);
            CmsPage cmsPage = forEntity.getBody();
            System.out.println(cmsPage);
        }
    }

    @Test
    public void testView() {
        CourseView coruseView = courseService.getCoruseView("4028858162bec7f30162becad8590000");
        System.out.println(coruseView);
    }

    @Test
    public void testCourseMapper1() {
        CourseListRequest courseListRequest =new CourseListRequest();
        courseListRequest.setCompanyId("1");
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        for (CourseBase courseBase : courseListPage.getResult()) {
            System.out.println("====================================");
            System.out.println(courseBase);
            System.out.println(courseBase.getCompanyId());
        }


    }

}
