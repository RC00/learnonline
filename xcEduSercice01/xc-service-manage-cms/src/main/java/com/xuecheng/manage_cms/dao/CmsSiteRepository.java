package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 获取站点的信息（站点域名、站点访问路径等）
 */
public interface CmsSiteRepository extends MongoRepository<CmsSite,String> {
}
