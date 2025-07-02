package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tour.model.WishComment;
import com.tour.query.WishCommentQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @Author Abin
 * @Description 心愿评论数据访问接口
 */
@Mapper
@Repository
public interface WishCommentMapper extends BaseMapper<WishComment> {
    
    /**
     * 分页查询心愿评论列表
     *
     * @param page 分页参数
     * @param wishId 心愿ID
     * @return 分页评论列表
     */
    IPage<WishComment> queryWishCommentList(Page<WishComment> page, @Param("wishId") Long wishId);
    
    /**
     * 管理员高级查询评论列表
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<WishComment> queryCommentListForAdmin(Page<WishComment> page, @Param("query") WishCommentQuery query);
} 