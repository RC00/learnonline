package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.task.XcTaskHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XcTaskHisRepository extends JpaRepository<XcTaskHis,String> {
}
