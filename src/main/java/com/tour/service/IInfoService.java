/**
 * @Author Abin
 * @Description 信息存储服务接口
 */
package com.tour.service;

import com.tour.dto.BusinessInfoDTO;
import com.tour.dto.ContractInfoDTO;
import com.tour.dto.InfoDTO;
import com.tour.vo.InfoVO;

/**
 * 信息存储服务接口
 * 用于处理键值对数据存储和查询
 */
public interface IInfoService {

  /**
   * 保存或更新信息
   * 
   * @param infoDTO        信息数据传输对象
   * @param operatorOpenid 操作人openid
   * @return 是否成功
   */
  boolean saveOrUpdateInfo(InfoDTO infoDTO, String operatorOpenid);

  /**
   * 根据键名获取信息
   * 
   * @param key 键名
   * @return 信息视图对象，如果不存在则返回null
   */
  InfoVO getInfoByKey(String key);

  /**
   * 保存或更新商务信息
   * 
   * @param businessInfoDTO 商务信息DTO
   * @param operatorOpenid  操作人openid
   * @return 是否成功
   */
  boolean saveBusinessInfo(BusinessInfoDTO businessInfoDTO, String operatorOpenid);

  /**
   * 获取商务信息
   * 
   * @return 商务信息DTO
   */
  BusinessInfoDTO getBusinessInfo();

  /**
   * 保存或更新合同信息
   * 
   * @param contractInfoDTO 合同信息DTO
   * @param operatorOpenid  操作人openid
   * @return 是否成功
   */
  boolean saveContractInfo(ContractInfoDTO contractInfoDTO, String operatorOpenid);

  /**
   * 获取合同信息
   * 
   * @return 合同信息DTO
   */
  ContractInfoDTO getContractInfo();
}