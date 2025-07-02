/**
 * @Author Abin
 * @Description 用户查询参数
 */
package com.tour.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends BaseParam {
    
    /**
     * 搜索关键词（用于匹配用户昵称或openid）
     */
    private String keyword;
    
    /**
     * 用户角色
     */
    private String role;
    
    /**
     * 注册开始时间
     */
    private Date startTime;
    
    /**
     * 注册结束时间
     */
    private Date endTime;
    
    /**
     * 排序方向，默认降序（desc/asc）
     */
    private String sortDirection;
} 