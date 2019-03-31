package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XcMenuRepository extends JpaRepository<XcMenu,String> {
}
