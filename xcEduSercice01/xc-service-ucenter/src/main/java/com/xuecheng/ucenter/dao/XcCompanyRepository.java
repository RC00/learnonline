package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XcCompanyRepository extends JpaRepository<XcCompany,String> {
}
