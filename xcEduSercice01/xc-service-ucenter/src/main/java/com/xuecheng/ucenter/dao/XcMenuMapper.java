package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

//实现根据用户id查询权限。
@Mapper
public interface XcMenuMapper {
    public List<XcMenu> selectPermissionByUserId(String userid);
}
