package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFsTest {
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;

    /**
     * 读取文件
     * @throws IOException
     */
    @Test
    public void queryFile() throws IOException {
        String fileId = "5be59ffafc5d1931e0f7e8a4";
        //根据id查询文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));

        //打开下载流对象
        GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建gridFsResource,用于获取流对象
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, downloadStream);
        //获取流中的数据
        String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
        System.out.println(content);
    }
    /**
     * 删除文件
     */
    @Test
    public void testDelFile(){
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is("5be59ffafc5d1931e0f7e8a4")));
    }
}
