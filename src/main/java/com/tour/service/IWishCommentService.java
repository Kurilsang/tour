package com.tour.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tour.dto.WishCommentDTO;
import com.tour.model.WishComment;
import com.tour.query.WishCommentQuery;
import com.tour.vo.WishCommentVO;

/**
 * 心愿路线评论服务接口
 *
 * @Author Abin
 */
public interface IWishCommentService extends IService<WishComment> {
    
    /**
     * 添加心愿评论
     *
     * @param wishCommentDTO 评论DTO
     * @param userOpenid 用户openid
     * @return 添加后的评论信息
     */
    WishCommentVO addComment(WishCommentDTO wishCommentDTO, String userOpenid);
    
    /**
     * 分页查询心愿评论列表
     *
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param wishId 心愿ID
     * @return 分页评论列表
     */
    IPage<WishCommentVO> queryCommentList(Integer pageNo, Integer pageSize, Long wishId);
    
    /**
     * 分页查询评论列表，并判断当前用户是否是评论所有者
     *
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param wishId 心愿路线ID
     * @param currentUserOpenid 当前用户openid
     * @return 分页结果
     */
    IPage<WishCommentVO> queryCommentList(Integer pageNo, Integer pageSize, Long wishId, String currentUserOpenid);
    
    /**
     * 查询用户的所有评论
     *
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param userOpenid 用户openid
     * @return 分页评论列表
     */
    IPage<WishCommentVO> queryUserCommentList(Integer pageNo, Integer pageSize, String userOpenid);
    
    /**
     * 删除评论（仅允许评论发布者或管理员删除）
     *
     * @param commentId 评论ID
     * @param userOpenid 当前用户openid
     * @return 是否删除成功
     */
    boolean deleteComment(Long commentId, String userOpenid);
    
    /**
     * 为管理员提供的高级评论查询接口
     *
     * @param query 查询条件
     * @return 分页评论列表
     */
    IPage<WishCommentVO> queryCommentListForAdmin(WishCommentQuery query);
    
    /**
     * 管理员删除评论
     *
     * @param commentId 评论ID
     * @param adminOpenid 管理员openid
     * @return 是否删除成功
     */
    boolean deleteCommentByAdmin(Long commentId, String adminOpenid);
} 