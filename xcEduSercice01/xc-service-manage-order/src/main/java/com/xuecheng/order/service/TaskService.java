package com.xuecheng.order.service;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private XcTaskRepository xcTaskRepository;

    @Autowired
    private XcTaskHisRepository xcTaskHisRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //取出前n条任务,取出指定时间之前处理的任务
    public List<XcTask> findTaskList(Date updateTime, int size) {
        //设置分页参数，取出前n 条记录
        Pageable pageable = PageRequest.of(0, size);
        Page<XcTask> xcTasks = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        return xcTasks.getResult();
    }

    /**
     * //发送消息
     *
     * @param taskId     任务对象
     * @param ex         交换机id
     * @param routingKey
     */
    @Transactional
    public void publish(String taskId, String ex, String routingKey) {

        //查询任务
        Optional<XcTask> taskOptional = xcTaskRepository.findById(taskId);
        if (taskOptional.isPresent()) {

            XcTask xcTask = taskOptional.get();
            rabbitTemplate.convertAndSend(ex, routingKey, xcTask);
            //更新任务时间
            xcTask.setUpdateTime(new Date());
            xcTaskRepository.save(xcTask);
        }
    }

    //删除已完成任务
    @Transactional
    public void finishTask(String taskId) {
        Optional<XcTask> xcTaskOptional = xcTaskRepository.findById(taskId);
        if (xcTaskOptional.isPresent()) {
            XcTask xcTask = xcTaskOptional.get();
            xcTask.setDeleteTime(new Date());

            //历史事务
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask,xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
            xcTaskRepository.delete(xcTask);
        }

    }

}
