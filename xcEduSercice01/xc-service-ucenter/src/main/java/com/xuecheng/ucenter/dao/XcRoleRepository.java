package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XcRoleRepository extends JpaRepository<XcRole,String> {
}
