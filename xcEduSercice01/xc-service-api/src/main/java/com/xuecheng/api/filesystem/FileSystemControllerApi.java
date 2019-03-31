package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "文件管理服务", description = "文件管理服务", tags = {"文件管理服务"})
public interface FileSystemControllerApi {
    /**
     * 上传文件
     *
     * @param multipartFile 文件
     * @param filetag       文件标签
     * @param businesskey   业务key
     * @param metadata      元信息,json格式
     * @return
     */
    @ApiOperation("保存上传的课程图片信息到MongoDB")
    public UploadFileResult upload(MultipartFile multipartFile,
                                   String filetag,
                                   String businesskey,
                                   String metadata);

}
