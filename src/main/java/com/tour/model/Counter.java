package com.tour.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Counter implements Serializable {

  private Integer id;

  private Integer count;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  public Counter(Integer id, int i) {
    this.id = id;
    this.count = i;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }
}
