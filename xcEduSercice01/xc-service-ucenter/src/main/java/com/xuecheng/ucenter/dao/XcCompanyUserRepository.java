package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XcCompanyUserRepository extends JpaRepository<XcCompanyUser, String> {
    //根据id查询所属企业id
    XcCompanyUser findByUserId(String userId);
}
