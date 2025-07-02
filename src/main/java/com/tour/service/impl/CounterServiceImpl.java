package com.tour.service.impl;

import com.tour.dao.CountersMapper;
import com.tour.model.Counter;
import com.tour.service.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CounterServiceImpl implements CounterService {

  public final static Map<Integer,Counter> map = new ConcurrentHashMap<>();

  @Override
  public Optional<Counter> getCounter(Integer id) {
    return Optional.ofNullable(map.getOrDefault(id, new Counter(id, 0)));
  }

  @Override
  public void upsertCount(Counter counter) {
    map.put(counter.getId(), counter);
    // 直接使用map的put方法会覆盖原来的值
    // map.merge(counter.getId(), counter, (oldValue, newValue) -> {
    //   oldValue.setCount(oldValue.getCount() + newValue.getCount());
    //   return oldValue;
    // });
  }

  @Override
  public void clearCount(Integer id) {
    Counter counter = map.get(id);
    if (counter != null) {
      counter.setCount(0);
      map.put(id, counter);
    }
  }
}
