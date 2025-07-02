package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tour.model.Comment;
import org.apache.ibatis.annotations.Mapper;
/**
 * @Author Kuril
 * @Description 活动评论Mapper
 * @DateTime 2025/5/16 15:59
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
