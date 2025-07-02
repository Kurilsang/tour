package com.tour.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tour.common.constant.Constants;
import com.tour.common.exception.ServiceException;
import com.tour.dao.BusLocationMapper;
import com.tour.dao.LocationMapper;
import com.tour.dto.BusLocationDTO;
import com.tour.enums.PageSize;
import com.tour.model.BusLocation;
import com.tour.model.Location;
import com.tour.query.BusLocationQuery;
import com.tour.service.BusLocationService;
import com.tour.vo.BusLocationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Kuril
 * @Description 上车点服务实现类
 * @DateTime 2025/5/13 9:45
 */
@Service("busLocationService")
@Slf4j
public class BusLocationServiceImpl implements BusLocationService {

    private final LocationMapper locationMapper;
    private final BusLocationMapper busLocationMapper;

    public BusLocationServiceImpl(LocationMapper locationMapper, BusLocationMapper busLocationMapper) {
        this.locationMapper = locationMapper;
        this.busLocationMapper = busLocationMapper;
    }

    @Override
    @Transactional
    public void addBusLocation(BusLocationDTO busLocationDTO) {
//        处理传入的信息分别插入
//插入location表
        Location location=busLocationDTO.getLocation();
        location.setId(null);
        location.setCreateTime(LocalDateTime.now());
        location.setUpdatedTime(LocalDateTime.now());
        location.setName(busLocationDTO.getBusLocation());
        location.setAddress(busLocationDTO.getBusLocation());
        location.setLocationActivityId(null);
        location.setLocationProductId(null);
        locationMapper.insert(location);
//插入bus_location表
        BusLocation busLocation = new BusLocation();
        busLocation.setBusLocation(busLocationDTO.getBusLocation());
        busLocation.setLocationId(location.getId());
        busLocation.setCreateTime(LocalDateTime.now());
        busLocationMapper.insert(busLocation);
    }

    @Override
    @Transactional
    public void deleteBusLocations(List<Long> busLocationIds) {
//        优雅地把关联locationId批量查出并删除location表中数据
        List<BusLocation> busLocationList = busLocationMapper.selectBatchIds(busLocationIds);
        List<Long> locationIdList= busLocationList.stream()
                .map(BusLocation::getLocationId)
                .collect(Collectors.toList());
        locationMapper.deleteBatchIds(locationIdList);
//        批量删除bus_location表数据
        busLocationMapper.deleteBatchIds(busLocationIds);
    }

    @Override
    public IPage<BusLocationVO> findBusLocationsByPage(BusLocationQuery busLocationQuery) {
        // 构建分页参数
        Integer pageNo = busLocationQuery.getPageNo();
        Integer pageSize = busLocationQuery.getPageSize();
        if (pageNo == null) {
            pageNo = Constants.defaultPageNo;
        }
        if (pageSize == null) {
            pageSize = PageSize.SIZE10.getSize();
        }
        Page<BusLocation> page = new Page<>(pageNo, pageSize);

        QueryWrapper<BusLocation> queryWrapper = new QueryWrapper<>();

        // 根据 BusLocationQuery 中的属性动态添加查询条件
        if (busLocationQuery.getId() != null) {
            queryWrapper.eq("id", busLocationQuery.getId());
        }
        if (busLocationQuery.getBusLocation() != null && !busLocationQuery.getBusLocation().isEmpty()) {
            queryWrapper.like("bus_location", busLocationQuery.getBusLocation());
        }

        // 处理排序信息
        String orderBy = busLocationQuery.getOrderBy();
        log.info("排序字段: {}", orderBy);
        if (orderBy != null && !orderBy.isEmpty()) {
            queryWrapper.last("ORDER BY " + orderBy);
        }

        // 执行数据库查询
        IPage<BusLocation> locationPage = busLocationMapper.selectPage(page, queryWrapper);

        // 将实体列表转换为VO列表
        IPage<BusLocationVO> voPage = new Page<>(locationPage.getCurrent(), locationPage.getSize(), locationPage.getTotal());
        List<BusLocationVO> voList = locationPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public BusLocationVO findBusLocationById(Long id) {

        BusLocation busLocation = busLocationMapper.selectById(id);
        if(busLocation==null)
        {
            throw new ServiceException("id对应数据不存在");
        }
        Location location = locationMapper.selectById(busLocation.getLocationId());
        if(location == null)
        {
            throw  new ServiceException("对应地址数据不存在");
        }

//        转化为vo对象
        BusLocationVO busLocationVO =  new BusLocationVO();
        busLocationVO.setLocation(location);
        busLocationVO.setId(id);
        busLocationVO.setBusLocation(busLocation.getBusLocation());
        return busLocationVO;
    }

    @Override
    public void editBusLocation(BusLocationDTO busLocationDTO, Long id) {
        BusLocation busLocation = busLocationMapper.selectById(id);
        if(busLocation==null)
        {
            throw new ServiceException("对应数据不存在");
        }

        Location location = locationMapper.selectById(busLocation.getLocationId());
        if(location==null)
        {
            throw  new ServiceException("对应地址数据不存在");
        }
//        更新location
        Location updateLocation = busLocationDTO.getLocation();
        updateLocation.setId(location.getId());
        updateLocation.setLocationProductId(null);
        updateLocation.setLocationActivityId(null);
        updateLocation.setName(busLocationDTO.getBusLocation());
        updateLocation.setAddress(busLocationDTO.getBusLocation());
        updateLocation.setUpdatedTime(LocalDateTime.now());

        locationMapper.updateById(updateLocation);

//        更新bus_location
        busLocation.setBusLocation(busLocationDTO.getBusLocation());

        busLocationMapper.updateById(busLocation);



    }

    @Override
    public List<BusLocationVO> selectAllData() {

        //       将所有信息查出并转化为VO对象
        List<BusLocation> busLocationList = busLocationMapper.selectList(null);
        List<BusLocationVO> busLocationVOList = new ArrayList<>();
        for (BusLocation busLocation : busLocationList) {
            busLocationVOList.add(convertToVO(busLocation));
            log.info("上车点信息{}",convertToVO(busLocation).toString());
        }


        return busLocationVOList;
    }

    // 实体到VO的转换方法
    private BusLocationVO convertToVO(BusLocation busLocation) {
        BusLocationVO vo = new BusLocationVO();
        vo.setId(busLocation.getId());
        vo.setBusLocation(busLocation.getBusLocation());


        Location location = locationMapper.selectById(busLocation.getLocationId());
        vo.setLocation(location);

        return vo;
    }


}
