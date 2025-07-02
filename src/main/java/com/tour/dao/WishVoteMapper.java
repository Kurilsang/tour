package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tour.model.WishVote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 心愿路线投票 Mapper 接口
 *
 * @Author Abin
 */
@Mapper
public interface WishVoteMapper extends BaseMapper<WishVote> {

} 