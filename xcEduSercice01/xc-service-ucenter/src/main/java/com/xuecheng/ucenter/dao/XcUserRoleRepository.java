package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XcUserRoleRepository extends JpaRepository<XcUserRole,String> {
}
