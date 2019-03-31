package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import com.xuecheng.manage_course.mapper.CourseMapper;
import com.xuecheng.manage_course.mapper.TeachplanMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 课程的正删改查
 */
@Service
public class CourseService {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CmsPageClient cmsPageClient;//远程调用注入的接口==>com.xuecheng.manage_course.client.CmsPageClient

    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;

    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    /*读物yml文件中的参数注入到对应的变量中*/
    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;

    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;

    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;

    @Value("${course-publish.siteId}")
    private String publish_siteId;

    @Value("${course-publish.templateId}")
    private String publish_templateId;

    @Value("${course-publish.previewUrl}")
    private String previewUrl;


    /**
     * 查询课程计划
     *
     * @param courseId
     * @return
     */
    public TeachplanNode findTeachplanList(String courseId) {
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return teachplanNode;
    }

    /*获取课程根结点，如果没有则添加根结点*/
    public String getTeachplanRoot(String courseId) {

        //校验课程id
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            return null;
        }
        CourseBase courseBase = optional.get();

        //取出课程计划根结点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if (teachplanList == null || teachplanList.size() <= 0) {
            //新增一个根结点
            Teachplan teachplanNode = new Teachplan();
            teachplanNode.setCourseid(courseId);
            teachplanNode.setPname(courseBase.getName());
            teachplanNode.setParentid("0");
            teachplanNode.setGrade("1");//1级
            teachplanNode.setStatus("0");//未发布
            teachplanRepository.save(teachplanNode);
            //返回添加的id
            return teachplanNode.getId();
        }
        return teachplanList.get(0).getId();
    }

    /**
     * 添加课程计划
     *
     * @param teachplan
     * @return
     */
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        //取出课程id
        String courseid = teachplan.getCourseid();
        //校验课程id和课程计划名称
        if (teachplan == null ||
                StringUtils.isEmpty(courseid) ||
                StringUtils.isEmpty(teachplan.getPname())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        //取出父结点id
        String parentid = teachplan.getParentid();

        if (StringUtils.isEmpty(parentid)) {
            //如果父结点为空则获取根结点
            parentid = getTeachplanRoot(courseid);
        }

        //取出父结点信息==>确认该节点的级别
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //父结点
        Teachplan teachplan_parent = optional.get();

        //父结点级别
        String parent_grade = teachplan_parent.getGrade();
        //向==>设置父结点
        teachplan.setParentid(parentid);
        //未所添加的课程==>设置为发布
        teachplan.setStatus("0");
        //子结点的级别，根据来判断
        int p_grade = Integer.parseInt(parent_grade);
        teachplan.setGrade((p_grade + 1) + "");//该课程节点为==>父结点+1

        //设置课程id
        teachplan.setCourseid(teachplan_parent.getCourseid());

        teachplanRepository.save(teachplan);
        return ResponseResult.SUCCESS();
    }

    /**
     * 课程列表分页查询
     *
     * @param page
     * @param size
     * @param courseListRequest
     * @return
     */
    public QueryResponseResult<CourseInfo> findCourseList(String companyId, Integer page, Integer size, CourseListRequest courseListRequest) {
        //判断查询条件
        if (courseListRequest == null) {
            courseListRequest = new CourseListRequest();
        }
        //企业Id
        if (companyId == null) {
            companyId = "";
        }
        courseListRequest.setCompanyId(companyId);
        //分页参数设置
        if (page == null || page <= 0) {
            page = 1;
        }
        if (size == null || size <= 0) {
            size = 10;
        }
        //设置分页参数
        PageHelper.startPage(page, size);
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);

        //查询列表
        List<CourseInfo> pageResultList = courseListPage.getResult();
        //总记录数
        long total = courseListPage.getTotal();
        //查询的结果集
        QueryResult<CourseInfo> queryResult = new QueryResult<CourseInfo>();
        //设置参数
        queryResult.setTotal(total);
        queryResult.setList(pageResultList);
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }

    /**
     * 添加课程的基本信息
     *
     * @param courseBase
     * @return
     */
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        //课程状态默认设置为==>未发布
        courseBase.setStatus("202001");
        CourseBase save = courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS, save.getId());
    }

    /**
     * 获取课程的基本信息
     *
     * @param courseId
     * @return
     */
    public CourseBase getCoursebaseById(String courseId) {
        if (StringUtils.isEmpty(courseId)) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
            return null;
        }
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.FAIL);
            return null;
        }
        return optional.get();
    }

    /**
     * 更新课程基本信息
     *
     * @param id
     * @param courseBase
     * @return
     */
    @Transactional
    public ResponseResult updateCoursebase(String id, CourseBase courseBase) {
        CourseBase one = this.getCoursebaseById(id);
        if (one == null) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //修改课程信息
        one.setName(courseBase.getName());
        one.setMt(courseBase.getMt());
        one.setSt(courseBase.getSt());
        one.setGrade(courseBase.getGrade());
        one.setStudymodel(courseBase.getStudymodel());
        one.setUsers(courseBase.getUsers());
        one.setDescription(courseBase.getDescription());
        CourseBase save = courseBaseRepository.save(one);
        System.out.println(save);
        return ResponseResult.SUCCESS();
    }

    /**
     * 获取课程的营销信息
     *
     * @param courseId
     * @return
     */
    public CourseMarket getCourseMarketById(String courseId) {
        if (StringUtils.isEmpty(courseId)) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
            return null;
        }
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if (!optional.isPresent()) {
            return null;
        }
        return optional.get();
    }

    /**
     * 更新课程营销信息
     *
     * @param id
     * @param courseMarket
     * @return
     */
    public CourseMarket updateCourseMarketById(String id, CourseMarket courseMarket) {
        CourseMarket one = this.getCourseMarketById(id);
        if (one == null) {
            //添加课程的基本信息
            one = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, one);
            //设置id
            one.setId(id);
            //保存修改信息
            courseMarketRepository.save(one);

        } else {
            one.setCharge(courseMarket.getCharge());
            one.setStartTime(courseMarket.getStartTime());//课程有效期，开始时间
            one.setEndTime(courseMarket.getEndTime());//课程有效期，结束时间
            one.setPrice(courseMarket.getPrice());
            one.setQq(courseMarket.getQq());
            one.setValid(courseMarket.getValid());
            courseMarketRepository.save(one);
        }
        return one;
    }

    /**
     * 保存课程图片
     *
     * @param courseId
     * @param pic
     * @return
     */
    public ResponseResult saveCoursePic(String courseId, String pic) {
        try {
            //查询课程图片
            Optional<CoursePic> optional = coursePicRepository.findById(courseId);
            CoursePic coursePic = null;
            if (optional.isPresent()) {
                coursePic = optional.get();
            }

            //没有课程图片则新建对象
            if (coursePic == null) {
                coursePic = new CoursePic();
            }
            //添加参数
            coursePic.setCourseid(courseId);
            coursePic.setPic(pic);

            //保存课程图片
            coursePicRepository.save(coursePic);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.FAIL();
        }
        return ResponseResult.SUCCESS();
    }

    /**
     * 删除课程图片
     *
     * @param courseId
     * @return
     */
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {

        Long delete = coursePicRepository.deleteByCourseid(courseId);
        if (delete > 0) {
            return ResponseResult.SUCCESS();
        }
        return ResponseResult.FAIL();
    }

    /**
     * 查询课程图片
     *
     * @param courseId
     * @return
     */
    public CoursePic findCoursepic(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    /**
     * 课程视图查询
     *
     * @param id
     * @return
     */
    public CourseView getCoruseView(String id) {
        CourseView courseView = new CourseView();

        //查询课程的基本信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if (courseBaseOptional.isPresent()) {
            courseView.setCourseBase(courseBaseOptional.get());
        }

        //课程营销的基本信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if (courseMarketOptional.isPresent()) {
            courseView.setCourseMarket(courseMarketOptional.get());
        }

        //课程图片的基本信息
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(id);
        if (coursePicOptional.isPresent()) {
            courseView.setCoursePic(coursePicOptional.get());
        }

        //课程计划的信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);

        return courseView;
    }

    /**
     * 远程调用cms==>课程预览
     *
     * @param courseId
     * @return
     */
    public CoursePublishResult preview(String courseId) {
        CourseBase one = this.findCourseBaseById(courseId);

        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(courseId + ".html");
        //页面别名
        cmsPage.setPageAliase(one.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre + courseId);

        //远程请求cms保存页面信息
        //cmsPageClient==>Client注入接口

        CmsPageResult pageResult = cmsPageClient.save(cmsPage);

        //返回失败
        if (!pageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        //获取返回的cmspage
        CmsPage resultCmsPage = pageResult.getCmsPage();

        //成功
        String pageId = resultCmsPage.getPageId();
        //页面的URL

        String pageUrl = previewUrl + pageId;

        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    /**
     * 根据id查询课程基本信息
     *
     * @param courseId
     * @return
     */
    public CourseBase findCourseBaseById(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
            return null;
        }
        return optional.get();
    }

    /**
     * 课程页面的一键发布
     *
     * @param courseId
     * @return
     */
    @Transactional
    public CoursePublishResult publish(String courseId) {
        //课程信息
        CourseBase courseBase = this.findCourseBaseById(courseId);
        /*发布课程的详情页面*/
        CmsPostPageResult result = publish_page(courseId, courseBase);
        if (!result.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }

        //更新课程状态
        // 创建课程索引信息
        CourseBase courseBase1 = saveCoursePubState(courseBase);

        //创建课程索引
        CoursePub coursePub = createCoursePub(courseId);

        //向数据库保存课程索引信息
        CoursePub saveCoursePub = saveCoursePub(courseId, coursePub);
        if (saveCoursePub == null) {
            //创建课程索引信息失败
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_CREATE_INDEX_ERROR);
        }

        //保存课程计划媒资信息到待索引表
        saveTeachplanMediaPub(courseId);
        //页面URL
        String pageUrl = result.getPageUrl();

        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    /*发布课程的详情页面*/
    public CmsPostPageResult publish_page(String courseId, CourseBase courseBase) {

        //发布课程预览页面
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(courseId + ".html");
        //页面别名
        cmsPage.setPageAliase(courseBase.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre + courseId);


        //发布页面
        CmsPostPageResult result = cmsPageClient.postPageQuick(cmsPage);
        return result;
    }

    /*更新课程的发布状态*/
    private CourseBase saveCoursePubState(CourseBase courseBase) {
        //更新发布状态
        courseBase.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBase);
        return save;
    }

    /*保存CoursePub*/
    public CoursePub saveCoursePub(String id, CoursePub coursePub) {

        if (StringUtils.isEmpty(id)) {
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }

        CoursePub coursePub1 = null;
        Optional<CoursePub> optional = coursePubRepository.findById(id);
        if (optional.isPresent()) {
            coursePub1 = optional.get();
        }
        if (coursePub1 == null) {
            coursePub1 = new CoursePub();
        }

        //copy  bean属性
        BeanUtils.copyProperties(coursePub, coursePub1);

        //设置主键
        coursePub1.setId(id);
        //更新时间戳为最新时间
        coursePub1.setTimestamp(new Date());

        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String date = simpleDateFormat.format(new Date());
        coursePub1.setPubTime(date);

        CoursePub save = coursePubRepository.save(coursePub1);
        return save;
    }

    /*创建coursePub对象*/
    public CoursePub createCoursePub(String id) {
        CoursePub coursePub = new CoursePub();
        coursePub.setId(id);

        //course base
        Optional<CourseBase> courseBase = courseBaseRepository.findById(id);
        if (courseBase.isPresent()) {
            BeanUtils.copyProperties(courseBase.get(), coursePub);
        }
        //course pic
        Optional<CoursePic> coursePic = coursePicRepository.findById(id);
        if (coursePic.isPresent()) {
            BeanUtils.copyProperties(coursePic.get(), coursePub);
        }
        //course market
        Optional<CourseMarket> courseMarket = courseMarketRepository.findById(id);
        if (courseMarket.isPresent()) {
            BeanUtils.copyProperties(courseMarket.get(), coursePub);
        }
        //teach plan
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        //teachplanNode to json
        String jsonString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(jsonString);

        return coursePub;
    }

    /**
     * 保存媒资信息
     *
     * @param teachplanMedia
     * @return
     */
    public ResponseResult saveMedia(TeachplanMedia teachplanMedia) {
        if (teachplanMedia == null) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        //查询课程计划
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        Teachplan teachplan = optional.get();
        //只允许为也只节点的课程计划选择视频
        String grade = teachplan.getGrade();
        if (StringUtils.isEmpty(grade) || !grade.equals("3")) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        TeachplanMedia one = null;
        Optional<TeachplanMedia> optionalMediaFile = teachplanMediaRepository.findById(teachplanId);
        if (!optionalMediaFile.isPresent()) {
            one = new TeachplanMedia();
        } else {
            one = optionalMediaFile.get();
        }
        //保存媒资信息与课程计划信息
        one.setTeachplanId(teachplanId);
        one.setCourseId(teachplanMedia.getCourseId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaUrl(teachplanMedia.getMediaUrl());

        teachplanMediaRepository.save(one);
        return ResponseResult.SUCCESS();
    }

    /*保存课程计划媒资信息*/
    private void saveTeachplanMediaPub(String courseId) {
        //查询课程媒资信息
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        //将课程计划媒资信息存储带索引列表
        Long deleteResult = teachplanMediaPubRepository.deleteByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for (TeachplanMedia teachplanMedia : teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia, teachplanMediaPub);
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
    }
}