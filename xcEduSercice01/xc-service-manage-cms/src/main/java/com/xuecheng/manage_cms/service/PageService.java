package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {
    @Autowired
    private CmsPageRepository cmsPageRepository;

    /*SpringMVC提供 RestTemplate请求http接口，RestTemplate的底层可以使用第三方的http客户端工具实现http 的
请求，常用的http客户端工具有Apache HttpClient、OkHttpClient等，本项目使用OkHttpClient完成http请求，
原因也是因为它的性能比较出众。*/
    @Autowired
    private RestTemplate restTemplate;//服务接口

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    /*Spring Data MongoDB提供了一个GridFsOperations接口以及相应的实现，GridFsTemplate让您与文件系统进行交互。
    你可以建立一个GridFsTemplate由它交给一个实例MongoDbFactory，以及一MongoConverter*/
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;//生成模板的类

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    CmsSiteRepository cmsSiteRepository;

    /**
     * 页面列表分页查询
     *
     * @param page             当前页码
     * @param size             页面显示个数
     * @param queryPageRequest 查询条件
     * @return 页面列表
     */
    public QueryResponseResult findList(Integer page, Integer size, QueryPageRequest queryPageRequest) {
        //条件查询
        if (queryPageRequest == null) {
            queryPageRequest = new QueryPageRequest();
        }
        //条件匹配器
        //页面名称模糊查询，需要自定义字符串的匹配器实现模糊查询
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("templateId", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("pageName", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("pageId", ExampleMatcher.GenericPropertyMatchers.startsWith())
                .withMatcher("siteId", ExampleMatcher.GenericPropertyMatchers.contains());

        //设置条件值
        CmsPage cmsPage = new CmsPage();
        //站点id
        if (StringUtils.isNoneEmpty(queryPageRequest.getSiteId())) {
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //页面id
        if (StringUtils.isNoneEmpty(queryPageRequest.getPageId())) {
            cmsPage.setPageId(queryPageRequest.getPageId());
        }
        //页面名称
        if (StringUtils.isNoneEmpty(queryPageRequest.getPageName())) {
            cmsPage.setPageName(queryPageRequest.getPageName());
        }
        //页面别名
        if (StringUtils.isNoneEmpty(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //模板id
        if (StringUtils.isNoneEmpty(queryPageRequest.getTemplateId())) {
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }

        //创建条件实例
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);

        //页码处理
        if (page == null || page <= 0) {
            page = 1;
        }
        page = page - 1;//MongoDB的分页从0开始
        if (size == null || size <= 0) {
            size = 20;
        }
        //分页对象
        Pageable pageable = PageRequest.of(page, size);

        //分页查询
        Page<CmsPage> cmsPages = cmsPageRepository.findAll(example, pageable);
        //cmsPages.forEach(System.out::println);
        QueryResult<CmsPage> queryResult = new QueryResult<CmsPage>();
        //框架自动封装pageBean
        queryResult.setList(cmsPages.getContent());
        queryResult.setTotal(cmsPages.getTotalElements());
        //返回结果
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);

    }

    /**
     * 添加页面
     *
     * @param cmsPage
     * @return
     */
    public CmsPageResult add(CmsPage cmsPage) {
        //通过唯一索引(页面名称、站点Id、页面webpath)查询页面是否存在
        CmsPage _cmsPage = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(
                cmsPage.getPageName(),
                cmsPage.getSiteId(),
                cmsPage.getPageWebPath());

        //判断页面是否已经存在
        if (_cmsPage == null) {
            //将插入的页面id置位null,MongoDB会自动生成随机id
            cmsPage.setPageId(null);
            //cmsPageRepository.save(cmsPage);
            CmsPage insert = cmsPageRepository.insert(cmsPage);
            //返回结果
            return new CmsPageResult(CommonCode.SUCCESS, insert);
        }
        //页面存在
        ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        return new CmsPageResult(CommonCode.FAIL, null);
    }


    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    public CmsPage getById(String id) {
        Optional<CmsPage> cmsPage = cmsPageRepository.findById(id);
        if (cmsPage.isPresent()) {
            return cmsPage.get();
        }
        //返回null
        return null;
    }

    /**
     * 更新页面数据
     *
     * @param id
     * @param cmsPage
     * @return
     */
    public CmsPageResult update(String id, CmsPage cmsPage) {
        //先通过id查询
        CmsPage one = this.getById(id);
        if (one != null) {
            cmsPage.setPageId(one.getPageId());
            //更新模板id
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新dataUrl
            one.setDataUrl(cmsPage.getDataUrl());

            //执行更新
            CmsPage save = cmsPageRepository.save(one);
            if (save != null) {
                //返回success
                return new CmsPageResult(CommonCode.SUCCESS, save);
            }
        }
        //返回fail
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    /**
     * 删除页面
     *
     * @param id
     * @return
     */
    public ResponseResult deleteById(String id) {
        //判断是否存在
        CmsPage one = this.getById(id);
        if (one != null) {
            //存在,即删除
            cmsPageRepository.deleteById(id);
            return ResponseResult.SUCCESS();
        }
        //不存在则删除失败
        return ResponseResult.FAIL();
    }

    /**
     * 页面静态化
     */
    public String getPageHtml(String pageId) {
        //获取页面信息
        CmsPage cmsPage = this.getById(pageId);
        //判断页面是否存在
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //获取页面模型数据
        Map model = getModelByPage(cmsPage);
        //判断model
        if (model == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }

        //获取页面模板
        String templateContent = getTemplateByPage(cmsPage);
        //判断页面
        if (StringUtils.isEmpty(templateContent)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //执行静态化

        String html = generateHtml(templateContent, model);

        if (StringUtils.isEmpty(html)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return html;
    }

    /**
     * 执行页面静态化
     *
     * @param templateContent
     * @param model
     * @return
     */
    private String generateHtml(String templateContent, Map model) {

        try {
            //生成配置类
            Configuration configuration = new Configuration(Configuration.getVersion());
            //生成模板加载器
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate("template", templateContent);
            //配置模板加载器
            configuration.setTemplateLoader(stringTemplateLoader);
            //获取模板
            Template template = configuration.getTemplate("template");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取页面模板
     *
     * @param cmsPage
     * @return
     */
    private String getTemplateByPage(CmsPage cmsPage) {
        //获取模板id
        String templateId = cmsPage.getTemplateId();
        //判断页面模板id是否存在
        if (StringUtils.isEmpty(templateId)) {
            //模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //获取模板内容
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        //判断模板是否存在
        if (optional.isPresent()) {
            //获取模板对象
            CmsTemplate cmsTemplate = optional.get();
            //获取模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //通过gridFsTemplate获取模板文件内容
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建GridFsResource
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取数据模型
     *
     * @param cmsPage
     * @return
     */
    private Map getModelByPage(CmsPage cmsPage) {
        //取出dataURL
        String dataUrl = cmsPage.getDataUrl();
        //判断dataURL
        if (StringUtils.isEmpty(dataUrl)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //获取数据信息
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }

    /**
     * 定义页面发布方法
     *
     * @param pageId
     * @return
     */
    public ResponseResult postPage(String pageId) {
        //执行静态化
        String pageHtml = this.getPageHtml(pageId);
        if (StringUtils.isEmpty(pageHtml)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        //保存静态化文件
        CmsPage cmsPage = saveHtml(pageId, pageHtml);
        //发送消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);

    }

    //发送页面发布消息
    private void sendPostPage(String pageId) {
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }

        Map<String, String> map = new HashMap<>();
        map.put("pageId", pageId);

        //消息的内容
        String json_message = JSON.toJSONString(map);

        //获取站点id作为路由key
        String siteId = cmsPage.getSiteId();

        //发布消息
        this.rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, siteId, json_message);


    }

    //保存静态页面内容
    private CmsPage saveHtml(String pageId, String pageHtml) {
        //查询页面
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();
        //先删除后插入
        String htmlFileId = cmsPage.getHtmlFileId();
        if (StringUtils.isNoneEmpty(htmlFileId)) {
            gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(htmlFileId)));
        }
        //保存html到GridFS
        InputStream inputStream = IOUtils.toInputStream(pageHtml);
        ObjectId objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        //文件id
        String fileId = objectId.toString();
        //把文件id存储到cmspage中
        cmsPage.setHtmlFileId(fileId);
        cmsPageRepository.save(cmsPage);
        return cmsPage;

    }

    /**
     * 保存课程页面==>如果存在则更新
     *
     * @param cmsPage
     * @return
     */
    public CmsPageResult save(CmsPage cmsPage) {
//校验页面是否存在，根据页面名称、站点Id、页面webpath查询
        CmsPage cmsPage_judge = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(
                cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());

        if (cmsPage_judge != null) {
            //更新
            return this.update(cmsPage_judge.getPageId(), cmsPage);
        } else {
            //添加
            return this.add(cmsPage);
        }
    }

    /**
     * 课程的一键发布页面
     *
     * @param cmsPage
     * @return
     */
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //添加页面
        CmsPageResult result = this.save(cmsPage);

        if (!result.isSuccess()) {
            // ExceptionCast.cast(CommonCode.FAIL);

            return new CmsPostPageResult(CommonCode.FAIL, null);
        }

        CmsPage resultCmsPage = result.getCmsPage();

        //要发布页面的id
        String pageId = resultCmsPage.getPageId();
        //发布页面
        ResponseResult responseResult = this.postPage(pageId);

        if (!responseResult.isSuccess()) {
            return new CmsPostPageResult(CommonCode.FAIL, null);
        }

        //获取页面的URL

        /*页面url=站点域名+站点webpath+页面webpath+页面名称
       页面Url= cmsSite.siteDomain+cmsSite.siteWebPath+ cmsPage.pageWebPath + cmsPage.pageName */

        //站点id
        String siteId = resultCmsPage.getSiteId();
        //查询站点信息
        CmsSite cmsSite = findCmsSiteById(siteId);
        //获取站点域名
        String siteDomain = cmsSite.getSiteDomain();
        //站点域名
        String siteWebPath = cmsSite.getSiteWebPath();

        //页面的物理路径
        String pageWebPath = resultCmsPage.getPageWebPath();
        //获取页面名称
        String pageName = resultCmsPage.getPageName();

        //页面访问的webpath
        String pageUrl = siteDomain + siteWebPath + pageWebPath + pageName;

        return new CmsPostPageResult(CommonCode.SUCCESS, pageUrl);
    }

    /**
     * 根据siteid获取站点信息
     *
     * @param siteId
     * @return
     */
    public CmsSite findCmsSiteById(String siteId) {
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
