package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tour.model.Enrollment;
import com.tour.vo.SignUpListVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EnrollmentMapper extends BaseMapper<Enrollment> {

    List<SignUpListVO> loadSignupListByActivityId(Long activityId);
}