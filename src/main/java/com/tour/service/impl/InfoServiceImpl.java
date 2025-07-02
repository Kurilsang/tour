/**
 * @Author Abin
 * @Description 信息存储服务实现类
 */
package com.tour.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.dao.InfoMapper;
import com.tour.dto.BusinessInfoDTO;
import com.tour.dto.ContractInfoDTO;
import com.tour.dto.InfoDTO;
import com.tour.enums.RoleEnum;
import com.tour.model.Info;
import com.tour.model.User;
import com.tour.service.IInfoService;
import com.tour.service.IUserService;
import com.tour.vo.InfoVO;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 信息存储服务实现类
 */
@Slf4j
@Service
public class InfoServiceImpl extends ServiceImpl<InfoMapper, Info> implements IInfoService {

  @Autowired
  private IUserService userService;

  @Override
  public boolean saveOrUpdateInfo(InfoDTO infoDTO, String operatorOpenid) {

    // 构建信息对象
    Info info = new Info();
    info.setInfoKey(infoDTO.getKey());
    info.setInfoValue(infoDTO.getValue());
    // 查询是否已存在该键
    LambdaQueryWrapper<Info> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Info::getInfoKey, infoDTO.getKey());
    Info existInfo = this.getOne(queryWrapper);

    if (existInfo != null) {
      // 更新已有信息
      info.setId(existInfo.getId());
      return this.updateById(info);
    } else {
      // 保存新信息
      return this.save(info);
    }
  }

  @Override
  public InfoVO getInfoByKey(String key) {
    // 查询指定键名的信息
    LambdaQueryWrapper<Info> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Info::getInfoKey, key);
    Info info = this.getOne(queryWrapper);

    if (info == null) {
      // 如果不存在，返回null
      return null;
    }

    // 将实体类转换为视图对象
    InfoVO infoVO = new InfoVO();
    infoVO.setKey(info.getInfoKey());
    infoVO.setValue(info.getInfoValue());

    return infoVO;
  }

  @Override
  public boolean saveBusinessInfo(BusinessInfoDTO businessInfoDTO, String operatorOpenid) {
    // 将BusinessInfoDTO转换为JSON字符串
    String businessInfoJson = JSON.toJSONString(businessInfoDTO);

    // 构建信息DTO
    InfoDTO infoDTO = new InfoDTO();
    infoDTO.setKey("businessInfo");
    infoDTO.setValue(businessInfoJson);

    // 调用保存信息的方法
    return saveOrUpdateInfo(infoDTO, operatorOpenid);
  }

  @Override
  public BusinessInfoDTO getBusinessInfo() {
    // 查询商务信息
    InfoVO infoVO = getInfoByKey("businessInfo");

    if (infoVO == null) {
      // 如果不存在，返回空对象
      return new BusinessInfoDTO();
    }

    try {
      // 将JSON字符串转换为BusinessInfoDTO
      return JSON.parseObject(infoVO.getValue(), BusinessInfoDTO.class);
    } catch (Exception e) {
      log.error("解析商务信息失败", e);
      // 如果解析失败，返回空对象
      return new BusinessInfoDTO();
    }
  }

  @Override
  public boolean saveContractInfo(ContractInfoDTO contractInfoDTO, String operatorOpenid) {
    // 将ContractInfoDTO转换为JSON字符串
    String contractInfoJson = JSON.toJSONString(contractInfoDTO);

    // 构建信息DTO
    InfoDTO infoDTO = new InfoDTO();
    infoDTO.setKey("contractInfo");
    infoDTO.setValue(contractInfoJson);

    // 调用保存信息的方法
    return saveOrUpdateInfo(infoDTO, operatorOpenid);
  }

  @Override
  public ContractInfoDTO getContractInfo() {
    // 查询合同信息
    InfoVO infoVO = getInfoByKey("contractInfo");

    if (infoVO == null) {
      // 如果不存在，返回空对象
      return new ContractInfoDTO();
    }

    try {
      // 将JSON字符串转换为ContractInfoDTO
      return JSON.parseObject(infoVO.getValue(), ContractInfoDTO.class);
    } catch (Exception e) {
      log.error("解析合同信息失败", e);
      // 如果解析失败，返回空对象
      return new ContractInfoDTO();
    }
  }
}