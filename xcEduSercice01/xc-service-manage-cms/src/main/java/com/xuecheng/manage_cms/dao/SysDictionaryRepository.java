package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.system.SysDictionary;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 字典查询接口
 */
public interface SysDictionaryRepository extends MongoRepository<SysDictionary, String> {
    //根据字典分类查询信息
    SysDictionary findByDType(String dType);
}
