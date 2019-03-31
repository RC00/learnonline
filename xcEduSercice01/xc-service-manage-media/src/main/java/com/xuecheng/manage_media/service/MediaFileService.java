package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

/**
 * 实现媒资文件查询列表。
 */
@Service
public class MediaFileService {
    private static Logger logger = LoggerFactory.getLogger(MediaUploadService.class);
    @Autowired
    MediaFileRepository mediaFileRepository;

    //文件列表分页查询
    public QueryResponseResult findList(Integer page, Integer size, QueryMediaFileRequest queryMediaFileRequest) {
        //查询条件
        MediaFile mediaFile = new MediaFile();
        if (queryMediaFileRequest == null) {
            queryMediaFileRequest = new QueryMediaFileRequest();
        }
        /*------=========条件匹配器==============----------*/
        // tag字段模糊匹配 //文件原始名称模糊匹配//处理状态精确匹配（默认）
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("tag", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("processStatus", ExampleMatcher.GenericPropertyMatchers.exact());

        //查询条件对象
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getTag())) {
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())) {
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())) {
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }
        //定义example实例
        Example<MediaFile> ex = Example.of(mediaFile, matcher);

        //分页参数
        if (page == null || page <= 0) {
            page = 1;
        }
        page = page - 1;
        if (size == null || size <= 0) {
            size = 10;
        }
        Pageable pageable = PageRequest.of(page, size);
        //分页查询
        Page<MediaFile> pageResult = mediaFileRepository.findAll(ex, pageable);
        QueryResult<MediaFile> mediaFileQueryResult = new QueryResult<>();
        mediaFileQueryResult.setList(pageResult.getContent());
        mediaFileQueryResult.setTotal(pageResult.getTotalElements());
        return new QueryResponseResult(CommonCode.SUCCESS, mediaFileQueryResult);
    }
}
