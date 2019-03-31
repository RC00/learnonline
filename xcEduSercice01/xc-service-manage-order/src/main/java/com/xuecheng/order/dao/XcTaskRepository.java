package com.xuecheng.order.dao;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.task.XcTask;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface XcTaskRepository extends JpaRepository<XcTask, String> {
    //取出指定时间之前的记录
    Page<XcTask> findByUpdateTimeBefore(Pageable pageable, Date updateTime);

    //更新任务处理时间
    @Modifying
    @Query(" update XcTask t set t.updateTime  = :updateTime where t.id = :id ")
    public int updateTaskTime(@Param(value = "id") String id, @Param(value = "updateTime") Date updateTime);
}
