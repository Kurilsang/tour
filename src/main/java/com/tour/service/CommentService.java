package com.tour.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.dto.CommentDTO;
import com.tour.model.Comment;
import com.tour.query.CommentQuery;
import com.tour.vo.CommentVO;

/**
 * @Author Kuril
 * @Description 评论服务接口
 * @DateTime 2025/5/16 16:30
 */
public interface CommentService {

    /**
     * 添加评论
     *
     * @param commentDTO 评论数据传输对象
     * @return 是否添加成功
     */
    boolean addComment(CommentDTO commentDTO);

    /**
     * 根据查询条件分页加载评论
     *
     * @param commentQuery 评论查询条件
     * @return 评论分页结果
     */
    IPage<CommentVO> loadComments(CommentQuery commentQuery);
    
    /**
     * 批量删除评论
     *
     * @param ids 评论ID数组
     * @return 是否全部删除成功
     */
    boolean batchDeleteComments(Long[] ids);
    
    /**
     * 更新评论状态
     *
     * @param id 评论ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateCommentStatus(Long id, Integer status);
} 