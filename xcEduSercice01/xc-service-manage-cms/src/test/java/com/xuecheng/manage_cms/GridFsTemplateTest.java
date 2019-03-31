package com.xuecheng.manage_cms;


import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFsTemplateTest {
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Test//5be7c68bbda0272230c52b72
    public void testGridFs() throws FileNotFoundException {
        //要存储的文件
        File file = new File("e:/index_banner.ftl");
        //定义输入流
        FileInputStream inputStream = new FileInputStream(file);
        //向gridfs储存 文件
        ObjectId objectId = gridFsTemplate.store(inputStream, "轮播图文件01", "");
        //返回文件的id
        String fileId = objectId.toString();
        System.out.println(fileId);
    }

    //文件存储2
    @Test
    public void testStore2() throws FileNotFoundException {
        File file = new File("e:/course.ftl");
        FileInputStream inputStream = new FileInputStream(file);
//保存模版文件内容
        ObjectId objectId = gridFsTemplate.store(inputStream, "课程详情模板文件", "");
        //返回文件的id
        String fileId = objectId.toString();
        System.out.println(fileId);
    }

}
