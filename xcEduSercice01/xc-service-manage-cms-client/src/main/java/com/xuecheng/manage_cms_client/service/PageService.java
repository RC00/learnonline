package com.xuecheng.manage_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

/**
 * 保存页面静态文件到服务器物理路径
 */
@Service
public class PageService {
    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;


    //将页面html保存到页面物理路径
    public void savePageToServerPath(String pageId) {
        //获取页面信息
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        //判断
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
            return;
        }
        CmsPage cmsPage = optional.get();

        //获取路径
        String pagePath = getPagePath(cmsPage);
        if (pagePath == null) {
            return;
        }
        System.out.println(pagePath+"===============================");
        //查询htmlfileId
        String htmlFileId = cmsPage.getHtmlFileId();
        //查询页面静态文件
        InputStream fileInputStream = this.getFileById(htmlFileId);
        if (fileInputStream == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        //写出到服务器本地
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(pagePath));
            //将文件内容copy到服务物理路径
            IOUtils.copy(fileInputStream, fileOutputStream);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //取出页面的物理路径
    private String getPagePath(CmsPage cmsPage) {
        //获取页面所属站点
        String siteId = cmsPage.getSiteId();
        CmsSite cmsSite = this.getCmsSiteById(siteId);
        if (cmsSite == null) {
            return null;
        }
        //获取页面路径信息==>服务器页面物理路径=站点物理路径+页面物理路径+页面名称
        String sitePhysicalPath = cmsSite.getSitePhysicalPath();
        String pagePath = (sitePhysicalPath == null ? "" : sitePhysicalPath)
                + cmsPage.getPagePhysicalPath() + cmsPage.getPageName();

        return pagePath;
    }


    //通过htmlFileId;查询页面静态文件
    private InputStream getFileById(String htmlFileId) {
        try {
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(htmlFileId)));
            GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, downloadStream);
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //根据站点id得到站点
    public CmsSite getCmsSiteById(String siteId) {
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
            return null;
        }
        return optional.get();
    }
}
