package com.tour.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.dto.BusLocationDTO;
import com.tour.query.BusLocationQuery;
import com.tour.vo.BusLocationVO;

import java.util.List;

public interface BusLocationService {
    void addBusLocation(BusLocationDTO busLocationDTO);

    void deleteBusLocations(List<Long> busLocationIds);

    IPage<BusLocationVO> findBusLocationsByPage(BusLocationQuery busLocationQuery);

    BusLocationVO findBusLocationById(Long id);

    void editBusLocation(BusLocationDTO busLocationDTO, Long id);

    List<BusLocationVO> selectAllData();
}
