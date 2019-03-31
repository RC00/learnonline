package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.service.PageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * 发布页面的消费客户端，监听
 * 页面发布队列的消息，收到消息后从mongodb下载文件，保存在本地。
 */
@Component
public class ConsumerPostPage {
    //日志打印
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerPostPage.class);

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    PageService pageService;

    @RabbitListener(queues = {"${xuecheng.mq.queue}"})
    public void postPage(String msg) {

        //解析消息
        Map map = JSON.parseObject(msg, Map.class);
        LOGGER.info("receive cms post page:{}", msg.toString());

        //取出页面信息
        String pageId = (String) map.get("pageId");
        //查询页面信息
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()) {
            LOGGER.error("receive cms post page,cmsPage is null:{}", msg.toString());
            return;
        }

        // 页面保存到服务器物理路径
        pageService.savePageToServerPath(pageId);
    }
}
