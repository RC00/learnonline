package com.xuecheng.manage_cms;


import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsPageParam;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CmsPageRepository cmsPageRepository;

    //分页查询测试
    @Test
    public void findByPage() {
        int page = 0;//分页从0开始
        int size = 10;//每页的记录数
        Pageable pageable = PageRequest.of(page, size);

        Page<CmsPage> pages = cmsPageRepository.findAll(pageable);
        pages.forEach(System.out::println);
    }

    //添加操作测试
    @Test
    public void testSave() {
        //创建实体类
        CmsPage cmsPage = new CmsPage();
        //赋值
        cmsPage.setPageName("哈哈insert");
        cmsPage.setDataUrl("我是dataURL");
        cmsPage.setSiteId("s01");
        cmsPage.setTemplateId("t01");
        cmsPage.setPageCreateTime(new Date());

        List<CmsPageParam> cmsPageParams = new ArrayList<>();
        CmsPageParam cmsPageParam = new CmsPageParam();
        cmsPageParam.setPageParamName("param1");
        cmsPageParam.setPageParamValue("value1");
        cmsPageParams.add(cmsPageParam);
        cmsPage.setPageParams(cmsPageParams);


        cmsPageRepository.insert(cmsPage);
        System.out.println(cmsPage);
    }

    //测试修改
    @Test
    public void testUpdate() {
        Optional<CmsPage> optional = cmsPageRepository.findById("5bdfd6d4bda02731a4a3fc98");
        System.out.println(cmsPageRepository.findById("5bdfd6d4bda02731a4a3fc98"));
        if (optional.isPresent()) {
            CmsPage cmsPage = optional.get();
            cmsPage.setPageName("测试页面0222");
            cmsPageRepository.save(cmsPage);
        }
        System.out.println(cmsPageRepository.findById("5bdfd6d4bda02731a4a3fc98"));
    }

    //测试删除
    @Test
    public void testDelete() {
        cmsPageRepository.deleteById("5bdfd6d4bda02731a4a3fc98");
    }

    ////根据页面名称查询
    //CmsPage findByPageName(String pageName);
    @Test
    public void testfindByPageName() {
        CmsPage page = cmsPageRepository.findByPageName("哈哈");
        System.out.println(page);
    }

    ////根据页面名称和类型查询
    //CmsPage findByPageNameAndPageType(String pageName,String pageType);
    @Test
    public void testfindByPageNameAndPageType() {
        CmsPage page = cmsPageRepository.findByPageNameAndPageType("哈哈", null);
        System.out.println(page);
    }

    ////根据站点和页面类型查询记录数
    //int countBySiteIdAndPageType(String siteId,String pageType);
    @Test
    public void testcountBySiteIdAndPageType() {
        int count = cmsPageRepository.countBySiteIdAndPageType("s01", null);
        System.out.println(count);
    }

    ////根据站点和页面类型分页查询
    //Page<CmsPage> findBySiteIdAndPageType(String siteId,String pageType, Pageable pageable);
    @Test
    public void testfindBySiteIdAndPageType() {
        int page = 0;//分页从0开始
        int size = 10;//每页的记录数
        Pageable pageable = PageRequest.of(page, size);

        Page<CmsPage> cmsPagePage = cmsPageRepository.findBySiteIdAndPageType("s01", null, pageable);
        System.out.println(cmsPagePage);
    }

    //查询所有
    //Long countAll();
    @Test
    public void testcountAll() {
        Long count = cmsPageRepository.countAllByPageName("哈哈");
        System.out.println(count);
    }

    //根据查询所有
    @Test
    public void testfindAllbyExample() {
        int page = 0;//分页从0开始
        int size = 10;//每页的记录数
        Pageable pageable = PageRequest.of(page, size);

        CmsPage cmsPage = new CmsPage();
        cmsPage.setPageAliase("页面");
        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<CmsPage> cmsPageExample = Example.of(cmsPage, exampleMatcher);

        Page<CmsPage> cmsPages = cmsPageRepository.findAll(cmsPageExample, pageable);
        cmsPages.forEach(System.out::println);
    }

    //根据查询所有
    @Test
    public void testExample() {
        int page = 0;//分页从0开始
        int size = 10;//每页的记录数
        Pageable pageable = PageRequest.of(page, size);

        CmsPage cmsPage = new CmsPage();
        cmsPage.setPageAliase("页面");

        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);

        Page<CmsPage> cmsPages = cmsPageRepository.findAll(example, pageable);
        cmsPages.forEach(System.out::println);
    }

    //3、测试RestTemplate
    //根据url获取数据，并转为map格式。
    @Test
    public void testRestTemplate() {
        ResponseEntity<Map> entity = restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f", Map.class);
        System.out.println(entity);
    }
}
