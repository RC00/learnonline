package com.xuecheng.manage_cms.web.controller;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cms/page")
public class CmsPageController implements CmsPageControllerApi {

    /*@Autowired
    private CmsPageRepository cmsPageRepository;*/

    @Autowired
    private PageService pageService;

    //查询页
    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") Integer page,
                                        @PathVariable("size") Integer size,
                                        QueryPageRequest queryPageRequest) {
       /* QueryResult queryResult = new QueryResult();//测试接口是否可以正常运行
        queryResult.setTotal(2L);
        //静态数据
        List list = new ArrayList();
        Pageable pageable = PageRequest.of(page, size);

        Page<CmsPage> pages = cmsPageRepository.findAll(pageable);
        pages.forEach(list::add);

        queryResult.setList(list);
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);*/


        return pageService.findList(page, size, queryPageRequest);
    }


    /**
     * 添加页
     *
     * @param cmsPage
     * @return
     */
    @Override
    @PostMapping("/add")
    public CmsPageResult add(@RequestBody CmsPage cmsPage) {
        System.out.println(cmsPage);
        return pageService.add(cmsPage);
    }

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    @Override
    @GetMapping("/get/{id}")
    public CmsPage findById(@PathVariable("id") String id) {
        return pageService.getById(id);
    }

    /**
     * 更新页面数据
     *
     * @param id
     * @param cmsPage
     * @return
     */
    @Override
    @PutMapping("/edit/{id}")
    public CmsPageResult edit(@PathVariable("id") String id, @RequestBody CmsPage cmsPage) {
        return pageService.update(id, cmsPage);
    }

    /**
     * 通过ID删除页面
     *
     * @param id
     * @return
     */
    @Override
    @DeleteMapping("/del/{id}")
    public ResponseResult delete(@PathVariable("id") String id) {
        return pageService.deleteById(id);
    }

    /**
     * 定义页面发布方法
     *
     * @param pageId
     * @return
     */
    @Override
    @PostMapping("/postPage/{pageId}")
    public ResponseResult post(@PathVariable("pageId") String pageId) {
        return pageService.postPage(pageId);
    }

    /**
     * 保存课程页面
     *
     * @param cmsPage
     * @return
     */
    @Override
    @PostMapping("/save")
    public CmsPageResult save(@RequestBody CmsPage cmsPage) {
        return pageService.save(cmsPage);
    }

    /**
     * 课程一键发布页面
     *
     * @param cmsPage
     * @return
     */
    @Override
    @PostMapping("/postPageQuick")
    public CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage) {
        return pageService.postPageQuick(cmsPage);
    }


}
