package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_course.client.CmsPageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FeignTest {

    @Autowired
    CmsPageClient cmsPageClient;

    @Test
    public void testFeign() {
        //通过id调用cms的查询页面接口
        CmsPage cmsPage = cmsPageClient.findById("5abefd525b05aa293098fca6");
        System.out.println(cmsPage);
    }
}
