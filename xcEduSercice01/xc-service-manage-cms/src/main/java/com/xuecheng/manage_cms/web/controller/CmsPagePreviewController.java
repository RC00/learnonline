package com.xuecheng.manage_cms.web.controller;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.PageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

@Controller
public class CmsPagePreviewController extends BaseController {

    @Autowired
    private PageService pageService;

    //接收页面id
    @RequestMapping(value="/cms/preview/{pageId}",method = RequestMethod.GET)
    public void preview(@PathVariable("pageId") String pageId) {
        

        String pageHtml = pageService.getPageHtml(pageId);
        System.out.println("============================================================");
        System.out.println(pageHtml);
        System.out.println("============================================================");

        if (StringUtils.isNoneEmpty(pageHtml)) {
            try {
                response.setHeader("Content-type","text/html;charset=utf-8");
                //获取输出流==>输出静态化页面
                ServletOutputStream outputStream = response.getOutputStream();
                outputStream.write(pageHtml.getBytes("utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
