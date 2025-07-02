/**
 * @Author Abin
 * @Description 心愿路线查询参数
 */
package com.tour.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class WishQuery extends BaseParam {
    
    /**
     * 搜索关键词（用于匹配心愿标题或描述）
     */
    private String keyword;
    
    /**
     * 状态：0-待成团，1-已成团，2-已关闭
     */
    private Integer status;
    
    /**
     * 排序方式：0-最新，1-最热（投票数最多）
     */
    private Integer sortType;
    
    /**
     * 创建开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 创建结束时间
     */
    private LocalDateTime endTime;
} 