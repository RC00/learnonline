package com.xuecheng.learning.client;


import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 在学习服务创建搜索服务的客户端接口，
 * 此接口会生成代理对象，
 * 调用搜索服务
 */
@Component
@FeignClient("xc-service-search")
@RequestMapping("/search/course")
public interface CourseSearchClient {
    @GetMapping("/getmedia/{teachplanId}")
    public TeachplanMediaPub getMedia(@PathVariable("teachplanId") String teachplanId);
}
