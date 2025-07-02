package com.tour.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.dto.ProductDTO;
import com.tour.model.Product;
import com.tour.query.ProductQuery;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @description product服务层
 * @author kuril
 * @date 2025-05-08
 */
public interface ProductService{


    IPage<Product> findByPage(ProductQuery productQuery);

    Object findProductById(Long id);

    void addProduct(ProductDTO productDTO);

    void deleteProductById(Long id);

    /**
     * 批量删除产品
     *
     * @param ids 产品ID列表
     */
    void batchDeleteProducts(List<Long> ids);

    /**
     * 切换产品状态
     *
     * @param id 产品ID
     * @param status 目标状态(1-下架 2-上架)
     */
    void toggleProductStatus(Long id, Integer status);

    void updateProduct(Long id, ProductDTO productDTO);
}