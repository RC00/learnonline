package com.xuecheng.manage_media.controller;

import com.xuecheng.api.media.MediaFileControllerApi;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.manage_media.service.MediaFileService;
import com.xuecheng.manage_media.service.MediaUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/media/file")
public class MediaFileController implements MediaFileControllerApi {
    @Autowired
    MediaFileService mediaFileService;
    @Autowired
    MediaUploadService mediaUploadService;

    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") Integer page,
                                        @PathVariable("size") Integer size,
                                        QueryMediaFileRequest queryMediaFileRequest) {
        //媒资文件查询
        return mediaFileService.findList(page, size, queryMediaFileRequest);
    }
}
