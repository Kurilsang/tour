package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tour.model.User;
import com.tour.query.UserQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 分页查询用户列表
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 分页用户列表
     */
    IPage<User> queryUserList(Page<User> page, @Param("query") UserQuery query);
} 