package com.xuecheng.filesystem.dao;

import com.xuecheng.framework.domain.filesystem.FileSystem;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 将文件信息存入数据库，主要存储文件系统中的文件路径。
 */
public interface FileSystemRepository extends MongoRepository<FileSystem, String> {
}
