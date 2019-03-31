package com.xuecheng.manage_media.service;


import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;


@Service
public class MediaUploadService {

    private final static Logger LOGGER = LoggerFactory.getLogger(MediaUploadService.class);

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    //上传文件的目录
    @Value("${xc-service-manage-media.upload-location}")
    String uploadPath;

    //视频处理队列
    @Value("${xc-service-manage-media.mq.queue-media-video-processor}")
    public String queue_media_video_processtask;

    //视频处理路由
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    public String routingkey_media_video;


    /*-------------------------===========上传注册==========================-----------------------*/

    /**
     * 文件上传注册
     *
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimeType
     * @param fileExt
     * @return
     */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String
            mimeType, String fileExt) {
        //检查文件是否上传
        //1.得到文件路径
        String filePath = getFilePath(fileMd5, fileExt);
        File file = new File(filePath);


        //2.查询数据库判定文件是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        //如若问价存在直接返回
        if (file.exists() && optional.isPresent()) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        boolean fileFold = createFileFold(fileMd5);
        if (!fileFold) {
            //上传文件目录创建失败
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_FAIL);
        }
        return ResponseResult.SUCCESS();
    }

    /**
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     *
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return 文件路径
     */
    private String getFilePath(String fileMd5, String fileExt) {
        StringBuilder filePath_sbr = new StringBuilder(uploadPath);
        String filePath = filePath_sbr.append(fileMd5.substring(0, 1))
                .append("/").append(fileMd5.substring(1, 2)).append("/")
                .append(fileMd5).append("/")
                .append(fileMd5).append(".").append(fileExt).toString();
        return filePath;
    }

    //得到文件目录的相对路径，路径中去掉根路径
    private String getFileFolderRelativePath(String fileMd5, String fileExt) {
        StringBuilder fileRelativePath_sbr = new StringBuilder();
        String fileRelativePath = fileRelativePath_sbr.append(fileMd5.substring(0, 1))
                .append("/").append(fileMd5.substring(1, 2)).append("/")
                .append(fileMd5).append("/").toString();
        return fileRelativePath;
    }

    //得到文件所在目录
    private String getFileFolderPath(String fileMd5) {
        StringBuilder fileFolderPath_sbr = new StringBuilder(uploadPath);
        String fileFolderPath = fileFolderPath_sbr.append(fileMd5.substring(0, 1))
                .append("/").append(fileMd5.substring(1, 2)).append("/")
                .append(fileMd5).append("/").toString();
        return fileFolderPath;
    }

    //创建文件目录
    private boolean createFileFold(String fileMd5) {
        //创建上传文件目录
        String fileFolderPath = getFileFolderPath(fileMd5);
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            //创建文件夹
            boolean mkdirs = fileFolder.mkdirs();
            return mkdirs;
        }
        return true;
    }

    /*-------------------------===========分块检查==========================-----------------------*/
    //得到文件所在目录
    private String getChunkFileFolderPath(String fileMd5) {
        String fileChunkFolderPath = getFileFolderPath(fileMd5) + "chunks" + "/";
        return fileChunkFolderPath;
    }

    //检查分块文件
    public CheckChunkResult checkChunk(String fileMd5, Integer chunk, Integer chunkSize) {
        System.out.println("检查分块文件" + chunk);
        System.out.println("检查分块文件" + chunkSize);
        //得到块文件所在的路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //块文件以序号命名，并无扩展名
        File chunkFile = new File(chunkFileFolderPath + chunk);
        System.out.println("检查分块文件" + chunkFile.getName());
        if (chunkFile.exists()) {
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, true);
        }
        return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, false);

    }


    /*-------------------------===========上传分块==========================-----------------------*/

    /**
     * 块文件上传
     *
     * @param file
     * @param fileMd5
     * @param chunk
     * @return
     */
    public ResponseResult uploadChunk(MultipartFile file, Integer chunk, String fileMd5) {
        if (file == null) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_ISNULL);
        }
        //创建块文件目录
        boolean fileFold = createChunkFileFolder(fileMd5);
        //块文件
        File chunkFile = new File(getChunkFileFolderPath(fileMd5) + chunk);
        //上传块文件
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(chunkFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("upload chunk file fail:{}", e.getMessage(), Throwables.getStackTraceAsString(e));
            ExceptionCast.cast(MediaCode.CHUNK_FILE_UPLOAD_FAIL);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ResponseResult.SUCCESS();
    }

    /*创建块文件目录*/
    private boolean createChunkFileFolder(String fileMd5) {
        //创建上传文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            //创建文件夹
            boolean mkdirs = chunkFileFolder.mkdirs();
            return mkdirs;
        }
        return true;
    }


    /*-------------------------===========合并分块==========================-----------------------*/

    /**
     * 合并块文件
     *
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimeType
     * @param fileExt
     * @return
     */
    public ResponseResult mergeChunks(String fileMd5, String fileName, Long fileSize, String
            mimeType, String fileExt) {
        //获取块问价路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            chunkFileFolder.mkdirs();
        }
        //合并文件路径
        File mergeFile = new File(getFilePath(fileMd5, fileExt));
        //创建合并文件
        //合并文件存在时，先删除后创建
        if (mergeFile.exists()) {
            mergeFile.delete();
        }
        boolean newFile = false;
        try {
            newFile = mergeFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("mergechunks..create mergeFile fail:{}", e.getMessage(), Throwables.getStackTraceAsString(e));
        }
        if (!newFile) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //获取块文件，此列表是已经排好序的列表
        List<File> chunkFiles = getChunkFiles(chunkFileFolder);
        //合并文件
        mergeFile = mergeFile(mergeFile, chunkFiles);
        if (mergeFile == null) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }
        //检验文件
        boolean checkResult = this.checkFileMd5(mergeFile, fileMd5);
        if (!checkResult) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //将文件信息保存到数据库
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5 + "." + fileExt);
        mediaFile.setFileOriginalName(fileName);
        //文件路径保存相对路径
        mediaFile.setFilePath(getFileFolderRelativePath(fileMd5, fileExt));
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimeType);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        MediaFile save = mediaFileRepository.save(mediaFile);
        /*删除分块文件*/
        try {
            chunkFiles.forEach(File::delete);
            new File(chunkFileFolderPath).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String fileId = mediaFile.getFileId();
        //向MQ发送视频处理消息
        sendProcessVideoMsg(fileId);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /*校验文件的md5值*/
    private boolean checkFileMd5(File mergeFile, String fileMd5) {
        if (mergeFile == null || StringUtils.isEmpty(fileMd5)) {
            return false;
        }
        //进行md5校验
        FileInputStream mergeFileInputstream = null;
        try {
            mergeFileInputstream = new FileInputStream(mergeFile);
            //得到文件的md5
            String mergeFileMd5 = DigestUtils.md5Hex(mergeFileInputstream);

            //比较MD5
            if (mergeFileMd5.equalsIgnoreCase(fileMd5)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("checkFileMd5 error,file is:{},md5 is:{}", mergeFile.getAbsoluteFile(), fileMd5);
        } finally {
            try {
                mergeFileInputstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*合并文件*/
    private File mergeFile(File mergeFile, List<File> chunkFiles) {
        System.out.println("chunkFiles.size()" + chunkFiles.size());
        try {
            //创建文件写对象
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
            //遍历分块开始合并
            //读取缓冲区
            byte[] bytes = new byte[1024];
            for (File chunkFile : chunkFiles) {
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
                int len = -1;
                //读取分块文件
                while ((len = raf_read.read(bytes)) != -1) {
                    //想文件中写入数据
                    raf_write.write(bytes, 0, len);
                }
                raf_read.close();
            }
            raf_write.close();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("merge file error:{}", e.getMessage(), Throwables.getStackTraceAsString(e));
            return null;
        }
        return mergeFile;
    }

    /*获取所有块文件*/
    private List<File> getChunkFiles(File chunkFileFolder) {
        //获取路径下所有文件
        File[] chunkFiles = chunkFileFolder.listFiles();
        //将文件数组转成list并排序
        List<File> chunkFileList = new ArrayList<>();
        chunkFileList.addAll(Arrays.asList(chunkFiles));
        //升序排列
        Collections.sort(chunkFileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                    return -1;
                }
                return 1;
            }
        });
        return chunkFileList;
    }


    /*-------------================向MQ发送视频处理消息===================---------------*/
    public ResponseResult sendProcessVideoMsg(String mediaId) {
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            return ResponseResult.FAIL();
        }
        MediaFile mediaFile = optional.get();
        //发送视频处理消息
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("mediaId", mediaId);
        //发送消息
        String msg = JSON.toJSONString(msgMap);

        try {
            this.rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK, routingkey_media_video, msg);
            LOGGER.info("send media process task msg:{}", msg);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("send media process task error,msg is:{},error:{}", msg, e.getMessage());
            return ResponseResult.FAIL();
        }
        return ResponseResult.SUCCESS();
    }

}
