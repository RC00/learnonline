package com.xuecheng.manage_course.client;

import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.CmsPostPageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * 定义FeignClient接口
 */

@Component

//指定了cms的服务名称，Feign会从注册中心获取cms服务列表，并通过负载均衡算法进行服务调用
@FeignClient(XcServiceList.XC_SERVICE_MANAGE_CMS)
public interface CmsPageClient {
    /**
     * 查询页面
     *
     * @param id
     * @return
     */
    @GetMapping("/cms/page/get/{id}")//指定调用的url，Feign将根据url进行远程调用。
    public CmsPage findById(@PathVariable("id") String id);

    /**
     * 保存页面
     *
     * @param cmsPage
     * @return
     */
    @PostMapping("/cms/page/save")//通过此Client远程请求cms添加页面
    public CmsPageResult save(@RequestBody CmsPage cmsPage);

    /**
     * 页面的一键发布
     *
     * @param cmsPage
     * @return
     */
    @PostMapping("/cms/page/postPageQuick")
    public CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage);
}

