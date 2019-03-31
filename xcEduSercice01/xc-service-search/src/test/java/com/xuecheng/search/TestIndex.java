package com.xuecheng.search;


import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestIndex {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Autowired
    RestClient restClient;

    //创建索引库
    @Test
    public void testCreateIndex() throws IOException {
        //创建索引对象,并创建索引名称
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("haha");
        //设置索引参数
        createIndexRequest.settings(Settings.builder().put("number_of_shards", 1).put("number_of_replicas", 0));


        //设置映射
        createIndexRequest.mapping("doc", " {\n" +
                " \t\"properties\": {\n" +
                " \"name\": {\n" +
                " \"type\": \"text\",\n" +
                " \"analyzer\":\"ik_max_word\",\n" +
                " \"search_analyzer\":\"ik_smart\"\n" +
                " },\n" +
                " \"description\": {\n" +
                " \"type\": \"text\",\n" +
                " \"analyzer\":\"ik_max_word\",\n" +
                " \"search_analyzer\":\"ik_smart\"\n" +
                " },\n" +
                " \"studymodel\": {\n" +
                " \"type\": \"keyword\"\n" +
                " },\n" +
                " \"price\": {\n" +
                " \"type\": \"float\"\n" +
                " }\n" +
                " }\n" +
                "}", XContentType.JSON);

        //创建索引操作客户端
        IndicesClient indicesClient = restHighLevelClient.indices();

        //创建相应对象
        CreateIndexResponse createIndexResponse = indicesClient.create(createIndexRequest);

        //得到响应结果
        boolean acknowledged = createIndexResponse.isAcknowledged();
        System.out.println(acknowledged);
    }


    //删除索引库
    @Test
    public void testDeleteIndex() throws IOException {
        //删除索引请求对象
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("haha");

        //删除索引
        DeleteIndexResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest);

        //删除索引响应结果
        boolean acknowledged = delete.isAcknowledged();
        System.out.println(acknowledged);
    }

    //添加文档
    @Test
    public void testAddDoc() throws IOException {
//准备json数据
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "spring cloud实战");
        jsonMap.put("description", "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud基础入门 3.实战Spring Boot 4.注册中心eureka。");
        jsonMap.put("studymodel", "201001");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy‐MM‐dd HH:mm:ss");
        jsonMap.put("timestamp", dateFormat.format(new Date()));
        jsonMap.put("price", 5.6f);
//索引请求对象
        IndexRequest indexRequest = new IndexRequest("xc_course", "doc");
//指定索引文档内容
        indexRequest.source(jsonMap);
//索引响应对象
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest);
        //获取响应结果
        DocWriteResponse.Result result = indexResponse.getResult();
        System.out.println(result);
    }

    //查询文档
    @Test
    public void getDoc() throws IOException {
        GetRequest getRequest = new GetRequest(
                "xc_course",
                "doc",
                "7XrqK2cBJsUadfMl6FfI");
        GetResponse getResponse = restHighLevelClient.get(getRequest);
        boolean exists = getResponse.isExists();
        Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
        System.out.println(sourceAsMap);
    }

    //更新文档
    @Test
    public void updateDoc() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("xc_course", "doc",
                "7XrqK2cBJsUadfMl6FfI");
        Map<String, String> map = new HashMap<>();
        map.put("name", "spring cloud实战");
        updateRequest.doc(map);
        UpdateResponse update = restHighLevelClient.update(updateRequest);
        RestStatus status = update.status();
        System.out.println(status);
    }

    //根据id删除文档
    @Test
    public void testDelDoc() throws IOException {
//删除文档id
        String id = "7XrqK2cBJsUadfMl6FfI";
//删除索引请求对象
        DeleteRequest deleteRequest = new DeleteRequest("xc_course", "doc", id);
//响应对象
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest);
//获取响应结果
        DocWriteResponse.Result result = deleteResponse.getResult();
        System.out.println(result);
    }


    //搜索type下的全部记录
    @Test
    public void testSearchAll() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel"}, new String[]{});

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        SearchHits hits = searchResponse.getHits();

        SearchHit[] searchHits = hits.getHits();

        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }

    //搜索type下的全部记录=>分页查询
    @Test
    public void testSearchByPage() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//分页查询，设置起始下标，从0开始
        searchSourceBuilder.from(0);
//每页显示个数
        searchSourceBuilder.size(2);
//source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }

    //搜索type下的全部记录=>Term Query
    @Test
    public void testSearchByTerm() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("name", "spring"));
//source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }

    //搜索type下的全部记录=>根据id精确匹配
    @Test
    public void testSearchById() throws IOException {


        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

//source源字段过虑
        String[] split = new String[]{"297e7c7c62b8aa9d0162b8ab13910000", "402885816243d2dd016243f24c030002"};
        List<String> idList = Arrays.asList(split);
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id", idList));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }

    //搜索type下的全部记录=>match Query
    //根据关键字搜索
    @Test
    public void testMatchQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//source源字段过虑


        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "description"}, new String[]{});
//匹配关键字
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "spring开发").operator(Operator.OR));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            System.out.println("======================");
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }

    //根据关键字搜索
    @Test
    public void testMatchQuerySearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "description"}, new String[]{});
//匹配关键字
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "spring 开发").operator(Operator.OR));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            System.out.println("======================");
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }

    //根据关键字搜索
    @Test
    public void testMatchQuerySearchName() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "description"}, new String[]{});
//匹配关键字


        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring框架",
                "name", "description")
                .minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name", 10);//提升boost
        searchSourceBuilder.query(multiMatchQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();

        for (SearchHit hit : searchHits) {
            System.out.println("======================");
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }

    //BoolQuery，将搜索关键字分词，拿分词去索引库搜索
    @Test
    public void testBoolQuery() throws IOException {
//创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
//创建搜索源配置对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{"name", "pic", "studymodel", "description"}, new String[]{});
//multiQuery
        String keyword = "spring开发框架";
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring框架",
                "name", "description")
                .minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name", 10);
//TermQuery
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel", "201001");
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.must(termQueryBuilder);
//设置布尔查询对象

        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);//设置搜索源配置
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            System.out.println("======================");

            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            System.out.println(sourceAsMap);
        }
    }

    //布尔查询使用过虑器
    @Test
    public void testFilter() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "description"},
                new String[]{});

//匹配关键字
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring框架", " name", " description");
//设置匹配占比
        multiMatchQueryBuilder.minimumShouldMatch("50%");
//提升另个字段的Boost值
        multiMatchQueryBuilder.field("name", 10);
        searchSourceBuilder.query(multiMatchQueryBuilder);
//布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(searchSourceBuilder.query());
//过虑
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            System.out.println("====================");
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }

    //过虑0--10元价格范围的文档，并且对结果进行排序，先按studymodel降序，再按价格升序
    @Test
    public void testSort() throws IOException, ParseException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "description"},
                new String[]{});

//布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

//过虑
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        //排序
        searchSourceBuilder.sort(new FieldSortBuilder("studymodel").order(SortOrder.DESC));
        searchSourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.ASC));

        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();

        int count = 1;
        for (SearchHit hit : searchHits) {
            System.out.println("=======" + count++ + "============");
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();

            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            String description = (String) sourceAsMap.get("description");
            // String timestamp = sourceAsMap.get("timestamp").toString();
            System.out.println(name);
            System.out.println("price==>" + price);
            System.out.println(studymodel);
            System.out.println(description);
            //  System.out.println(timestamp);
            //  System.out.println(sourceAsMap.get("timestamp"));
            //   SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            //  Date parse = simpleDateFormat.parse(timestamp);
            //  System.out.println(parse);
        }
    }

    //高亮显示
    @Test
    public void testHighlight() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "description"},
                new String[]{});
        searchRequest.source(searchSourceBuilder);
//匹配关键字
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("开发",
                "name", "description");
        searchSourceBuilder.query(multiMatchQueryBuilder);
//布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(searchSourceBuilder.query());
//过虑
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
//排序
        searchSourceBuilder.sort(new FieldSortBuilder("studymodel").order(SortOrder.DESC));
        searchSourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.ASC));
//高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tag>");//设置前缀
        highlightBuilder.postTags("</tag>");//设置后缀
// 设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
// highlightBuilder.fields().add(new HighlightBuilder.Field("description"));
        searchSourceBuilder.highlighter(highlightBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//名称
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
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }

}


