package com.xuecheng.search.service;

import com.google.common.base.Throwables;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 课程查询
 */
@Service
public class EsCourseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EsCourseService.class);

    @Value("${xuecheng.elasticsearch.course.index}")
    private String es_index;
    @Value("${xuecheng.elasticsearch.course.type}")
    private String es_type;
    @Value("${xuecheng.elasticsearch.course.source_field}")
    private String source_field;

    @Value("${xuecheng.elasticsearch.media.index}")
    private String media_index;
    @Value("${xuecheng.elasticsearch.media.type}")
    private String media_type;
    @Value("${xuecheng.elasticsearch.media.source_field}")
    private String media_source_field;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * 按关键字搜索
     *
     * @param page
     * @param size
     * @param courseSearchParam
     * @return
     */
    public QueryResponseResult<CoursePub> list(Integer page, Integer size, CourseSearchParam courseSearchParam) {

        //设置索引
        SearchRequest searchRequest = new SearchRequest(es_index);
        //设置类型
        searchRequest.types(es_type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //设置过滤源
        String[] source_fields = source_field.split(",");
        searchSourceBuilder.fetchSource(source_fields, new String[]{});

        //keyword
        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())) {
            //match key word
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "teachplan", "description");

            //设置匹配占比
            multiMatchQueryBuilder.minimumShouldMatch("70%");
            //提升另一字段的boots值
            multiMatchQueryBuilder.field("name", 10);
            //and  条件
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }

        /*-=-----=============按分类和难度等级搜索*==============-----------------/
        //过滤
        /*一级分类*/
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt", courseSearchParam.getMt()));
        }
        /*二级分类*/
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("st", courseSearchParam.getSt()));
        }
        /*难度等级*/
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
        }
         /*-=-----=============按分类和难度等级搜索*==============-----------------/


        /*---------------==================分页与高亮*====================-------------*/
        //分页
        if (page == null || page <= 0) {
            page = 1;
        }
        if (size == null || size <= 0) {
            size = 20;
        }

        //起始索引
        int start = (page - 1) * size;
        //参数赋值
        searchSourceBuilder.from(start);
        searchSourceBuilder.size(size);


        /*----====布尔查询=====----*/
        searchSourceBuilder.query(boolQueryBuilder);

        /*--------------------=================高亮显示===================------------------*/
        //高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");

        //设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);
        /*--------------------=================高亮显示===================------------------*/
        /*---------------==================分页与高亮*====================-------------*/



        /*-------=====请求搜索=======--------*/
        searchRequest.source(searchSourceBuilder);

        //结果响应
        SearchResponse searchResponse = null;

        try {
            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("catch exception:{}", e.getMessage(), Throwables.getStackTraceAsString(e));
            return new QueryResponseResult<CoursePub>(CommonCode.FAIL, new QueryResult<CoursePub>());
        }

        /*结果集处理*/
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();

        //总记录数
        long totalHits = hits.totalHits;
        //数据列表
        List<CoursePub> list = new ArrayList<CoursePub>();

        for (SearchHit hit : searchHits) {
            CoursePub coursePub = new CoursePub();

            //取出source
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //设置课程id
            String id = (String) sourceAsMap.get("id");
            coursePub.setId(id);

            //取出名称
            String name = (String) sourceAsMap.get("name");
            //取出高亮字段内容
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null) {
                HighlightField nameField = highlightFields.get("name");
                if (nameField != null) {
                    Text[] fragments = nameField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text str : fragments) {
                        stringBuffer.append(str.string());
                    }
                    name = stringBuffer.toString();
                }
            }
            coursePub.setName(name);

            //图片
            String pic = (String) sourceAsMap.get("pic");
            coursePub.setPic(pic);
            //价格==>新
            Object _price = sourceAsMap.get("price");
            Double price = null;
            try {
                if (sourceAsMap.get("price") != null) {
                    price = (Double) sourceAsMap.get("price");
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                LOGGER.error("catch exception:{}", e.getMessage(), Throwables.getStackTraceAsString(e));
            }
            coursePub.setPrice(price);

            //价格==>旧
            Double price_old = null;
            try {
                if (sourceAsMap.get("price_old") != null) {
                    price_old = (Double) sourceAsMap.get("price_old");
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                LOGGER.error("catch exception:{}", e.getMessage(), Throwables.getStackTraceAsString(e));
            }
            coursePub.setPrice_old(price_old);


            list.add(coursePub);
        }

        //封装结果
        QueryResult<CoursePub> queryResult = new QueryResult<>();
        queryResult.setList(list);
        queryResult.setTotal(totalHits);

        System.out.println(queryResult);
        return new QueryResponseResult<CoursePub>(CommonCode.SUCCESS, queryResult);
    }

    /**
     * 根据id查询课程信息
     *
     * @param id
     * @return
     */
    public Map<String, CoursePub> getAll(String id) {
        //设置索引库
        SearchRequest searchRequest = new SearchRequest(es_index);
        //设置类型
        searchRequest.types(es_type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //根据条件==》课程id查询
        searchSourceBuilder.query(QueryBuilders.termsQuery("id", id));

        //取消source员字段过滤，查询所有字段
        /*searchSourceBuilder.fetchSource(new String[]{"name", "grade", "charge", "pic"}, new String[]{});*/

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;
        try {
            //执行搜索
            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("catch exception:{}", e.getMessage(), Throwables.getStackTraceAsString(e));
        }

        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        Map<String, CoursePub> map = new HashMap<>();
        for (SearchHit searchHit : searchHits) {
            String searchHitId = searchHit.getId();
            System.out.println("searchHitId" + searchHitId);

            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String courseId = (String) sourceAsMap.get("id");
            System.out.println("courseId" + courseId);
            System.out.println(searchHitId.equals(courseId));

            String name = (String) sourceAsMap.get("name");
            String grade = (String) sourceAsMap.get("grade");
            String charge = (String) sourceAsMap.get("charge");
            String pic = (String) sourceAsMap.get("pic");
            String description = (String) sourceAsMap.get("description");
            String teachplan = (String) sourceAsMap.get("teachplan");

            CoursePub coursePub = new CoursePub();
            coursePub.setId(courseId);
            coursePub.setName(name);
            coursePub.setPic(pic);
            coursePub.setGrade(grade);
            ////////////////////////////
            coursePub.setCharge(charge);
            coursePub.setTeachplan(teachplan);
            coursePub.setDescription(description);
            map.put(searchHitId, coursePub);
        }
        return map;
    }

    /**
     * 根据课程计划查询媒资信息
     *
     * @param teachplanIds
     * @return
     */
    public QueryResponseResult<TeachplanMediaPub> getMedia(String[] teachplanIds) {
        //设置索引
        SearchRequest searchRequest = new SearchRequest(media_index);
        //设置类型
        searchRequest.types(media_type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //source源字段过滤
        String[] source_fields = media_source_field.split(",");
        searchSourceBuilder.fetchSource(source_fields, new String[]{});
        //查询条件，根据课程计划id查询(可传入多个id)
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id", teachplanIds));
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;
        try {
            //执行搜索

            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("catch exception:{}", e.getMessage(), Throwables.getStackTraceAsString(e));
        }
        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        //Map<String, CoursePub> map = new HashMap<>();
        //数据列表
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            String searchHitId = searchHit.getId();


            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            //取出课程计划媒资信息
            String courseid = (String) sourceAsMap.get("courseid");
            String media_id = (String) sourceAsMap.get("media_id");
            String media_url = (String) sourceAsMap.get("media_url");
            String teachplan_id = (String) sourceAsMap.get("teachplan_id");
            String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");
            teachplanMediaPub.setCourseId(courseid);
            teachplanMediaPub.setMediaUrl(media_url);
            teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
            teachplanMediaPub.setMediaId(media_id);
            teachplanMediaPub.setTeachplanId(teachplan_id);

            //将数据加入列表
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        //构建返回课程媒资信息对象
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(teachplanMediaPubList);
        return new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
    }
}
