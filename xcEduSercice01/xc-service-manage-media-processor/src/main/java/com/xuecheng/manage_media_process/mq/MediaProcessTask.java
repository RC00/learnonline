package com.xuecheng.manage_media_process.mq;


import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MediaProcessTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaProcessTask.class);
    //ffmpeg绝对路径
    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpeg_path;
    //上传文件的根目录
    @Value("${xc-service-manage-media.video-location}")
    String servicePath;

    @Autowired
    MediaFileRepository mediaFileRepository;

    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}", containerFactory = "customContainerFactory")
    public void receiveMediaProcessTask(String msg) {
        Map msgMap = JSON.parseObject(msg, Map.class);
        LOGGER.info("receive media process task msg :{} ", msgMap);

        //解析信息；获取媒资文件id
        String mediaId = (String) msgMap.get("mediaId");
        //获取媒资文件信息
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            return;
        }
        MediaFile mediaFile = optional.get();
        //媒资文件类型
        String fileType = mediaFile.getFileType();
        if (fileType == null || !fileType.equalsIgnoreCase("avi")) {
            //目前只实现了对avi文件的处理//此处处理状态为无需处理
            mediaFile.setProcessStatus("303004");
            mediaFileRepository.save(mediaFile);
            return;
        }
        //处理状态为未处理
        mediaFile.setProcessStatus("303001");
        mediaFileRepository.save(mediaFile);
        //调用工具类生成mp4
        String video_path = servicePath + mediaFile.getFilePath() + mediaFile.getFileName();
        String mp4_name = mediaFile.getFileId() + ".mp4";
        String mp4folder_path = servicePath + mediaFile.getFilePath();
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4folder_path);
        String result = mp4VideoUtil.generateMp4();

        /*-=--------========操作失败==》执行写入日志=========--------*/
        if (result == null || !result.equalsIgnoreCase("success")) {
            //处理状态为处理失败
            mediaFile.setProcessStatus("303003");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }

        /*-=--------========操作成功==》视频处理生成m3u8=========--------*/
        //获取mp4的地址
        video_path = servicePath + mediaFile.getFilePath() + mp4_name;
        String m3u8_name = mediaFile.getFileId() + ".m3u8";
        String m3u8folder_path = servicePath + mediaFile.getFilePath() + "hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path, video_path, m3u8_name, m3u8folder_path);
        result = hlsVideoUtil.generateM3u8();
        if (result == null || !result.equalsIgnoreCase("success")) {
            /*-=--------========操作失败==》执行写入日志=========--------*/
            //处理状态为处理失败
            mediaFile.setProcessStatus("303003");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        //获取m3u8列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        //更新处理状态为成功//处理状态为处理成功
        mediaFile.setProcessStatus("303002");
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);

        //m3u8的文件url
        mediaFile.setFileUrl(mediaFile.getFilePath() + "hls/" + m3u8_name);
        mediaFileRepository.save(mediaFile);
    }


}
