package com.tour.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tour.common.constant.Constants;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.CopyTools;
import com.tour.dao.LocationMapper;
import com.tour.dao.ProductMapper;
import com.tour.dao.ProductOrderMapper;
import com.tour.dto.ProductDTO;
import com.tour.enums.PageSize;
import com.tour.enums.ProductOrderStatusEnum;
import com.tour.enums.ProductStatusEnum;
import com.tour.model.Location;
import com.tour.model.Product;
import com.tour.model.ProductOrder;
import com.tour.query.ProductQuery;
import com.tour.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final LocationMapper locationMapper;
    private final ProductOrderMapper productOrderMapper;

    public ProductServiceImpl(ProductMapper productMapper, LocationMapper locationMapper, ProductOrderMapper productOrderMapper) {
        this.productMapper = productMapper;
        this.locationMapper = locationMapper;
        this.productOrderMapper = productOrderMapper;
    }

    @Override
    public IPage<Product> findByPage(ProductQuery productQuery) {
        // 构建分页参数
        Integer pageNo = productQuery.getPageNo();
        Integer pageSize = productQuery.getPageSize();
        if (pageNo == null) {
            pageNo = Constants.defaultPageNo;
        }
        if (pageSize == null) {
            pageSize = PageSize.SIZE10.getSize();
        }
        Page<Product> page = new Page<>(pageNo, pageSize);

        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();

        // 根据 ProductQuery 中的属性动态添加查询条件
        if (productQuery.getId() != null) {
            queryWrapper.eq("id", productQuery.getId());
        }
        if (productQuery.getName() != null && !productQuery.getName().isEmpty()) {
            queryWrapper.like("name", productQuery.getName());
        }
        if (productQuery.getDescription() != null && !productQuery.getDescription().isEmpty()) {
            queryWrapper.like("description", productQuery.getDescription());
        }
        if (productQuery.getPrice() != null) {
            queryWrapper.eq("price", productQuery.getPrice());
        }
        if (productQuery.getStock() != null) {
            queryWrapper.eq("stock", productQuery.getStock());
        }
        if (productQuery.getCoverImage() != null && !productQuery.getCoverImage().isEmpty()) {
            queryWrapper.eq("cover_image", productQuery.getCoverImage());
        }
        if (productQuery.getCreateTime() != null) {
            queryWrapper.ge("create_time", productQuery.getCreateTime());
        }
        if (productQuery.getCreatedBy() != null) {
            queryWrapper.eq("created_by", productQuery.getCreatedBy());
        }
        if (productQuery.getUpdateTime() != null) {
            queryWrapper.ge("update_time", productQuery.getUpdateTime());
        }
        if (productQuery.getUpdatedBy() != null) {
            queryWrapper.eq("updated_by", productQuery.getUpdatedBy());
        }
        if (productQuery.getStatus() != null) {
            queryWrapper.eq("status", productQuery.getStatus());
        }

        // 处理排序信息
        String orderBy = productQuery.getOrderBy();
        log.info("{}内容", orderBy);
        queryWrapper.last("ORDER BY " + orderBy);

        return productMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Object findProductById(Long id) {
//        查找
        Product product = productMapper.selectById(id);
        ProductDTO productDTO = CopyTools.copy(product, ProductDTO.class);
//        添加地址信息
        QueryWrapper<Location> locationQueryWrapper = new QueryWrapper<>();
        locationQueryWrapper.eq("location_product_id", product.getId());
        Location location = locationMapper.selectOne(locationQueryWrapper);
        productDTO.setLocation(location);
        return productDTO;
    }

    @Override
    @Transactional
    public void addProduct(ProductDTO productDTO) {
        // 校验价格
        validateProductPrice(productDTO);
        
//        去除id，id用自增的
        if(productDTO.getId()!=null)
        {
            productDTO.setId(null);
        }
        productDTO.setUpdateTime(LocalDateTime.now());
        productDTO.setCreateTime(LocalDateTime.now());
        Product product = CopyTools.copy(productDTO, Product.class);
        productMapper.insert(product);
//      添加对应地址信息
        Long productId = product.getId();
        log.info("这是productID{}", productId);
        Location newLocation = productDTO.getLocation();
        if(newLocation!=null)
        {
            if(newLocation.getId()!=null)
            {
                newLocation.setId(null);
            }
            newLocation.setLocationProductId(productId);
            newLocation.setCreateTime(LocalDateTime.now());
            newLocation.setUpdatedTime(LocalDateTime.now());
            newLocation.setLocationActivityId(0L);
            log.info("这是newLocation{}",newLocation.toString());
            locationMapper.insert(newLocation);
        }
    }

    @Override
    public void deleteProductById(Long id) {
        if(id==null)
        {
            log.error("没有传入产品ID");
            throw new ServiceException("没有传入产品ID");
        }
        
        // 查询产品信息
        Product product = productMapper.selectById(id);
        if (product == null) {
            log.error("产品不存在，ID: {}", id);
            throw new ServiceException("产品不存在");
        }
        
        // 检查产品是否为下架状态
        if (!ProductStatusEnum.OFF.getCode().equals(product.getStatus())) {
            log.error("产品不是下架状态，无法删除, 产品ID: {}, 产品名称: {}", id, product.getName());
            throw new ServiceException("只能删除下架状态的产品，请先下架产品: " + product.getName());
        }
        
        productMapper.deleteById(id);

        // 删除关联地址
        QueryWrapper<Location> locationQueryWrapper = new QueryWrapper<>();
        locationQueryWrapper.eq("location_product_id", id);
        locationMapper.delete(locationQueryWrapper);
    }

    @Override
    @Transactional
    public void batchDeleteProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.error("没有传入产品ID列表");
            throw new ServiceException("没有传入产品ID列表");
        }

        // 查询所有待删除商品的信息
        List<Product> products = productMapper.selectBatchIds(ids);
        
        // 检查是否所有商品都存在
        if (products.size() != ids.size()) {
            log.error("部分产品不存在");
            throw new ServiceException("部分产品不存在");
        }
        
        // 检查是否所有商品都是下架状态
        List<Product> nonOffProducts = products.stream()
                .filter(product -> !ProductStatusEnum.OFF.getCode().equals(product.getStatus()))
                .collect(Collectors.toList());
        
        if (!nonOffProducts.isEmpty()) {
            List<String> nonOffProductNames = nonOffProducts.stream()
                    .map(Product::getName)
                    .collect(Collectors.toList());
            
            log.error("以下产品不是下架状态，无法删除: {}", nonOffProductNames);
            throw new ServiceException("只能删除下架状态的产品，请先下架以下产品: " + String.join("，", nonOffProductNames));
        }
        
        // 批量删除产品
        productMapper.deleteBatchIds(ids);
        
        // 删除关联地址
        QueryWrapper<Location> locationQueryWrapper = new QueryWrapper<>();
        locationQueryWrapper.in("location_product_id", ids);
        locationMapper.delete(locationQueryWrapper);
        
        log.info("批量删除产品完成，共删除{}个产品", ids.size());
    }
    
    @Override
    public void toggleProductStatus(Long id, Integer status) {
        if (id == null) {
            log.error("没有传入产品ID");
            throw new ServiceException("没有传入产品ID");
        }
        
        if (status == null || (status != ProductStatusEnum.OFF.getCode() && status != ProductStatusEnum.ON.getCode())) {
            log.error("状态参数错误，必须为1(下架)或2(上架)");
            throw new ServiceException("状态参数错误，必须为1(下架)或2(上架)");
        }
        
        Product product = productMapper.selectById(id);
        if (product == null) {
            log.error("产品不存在，ID: {}", id);
            throw new ServiceException("产品不存在");
        }
        
        // 如果是切换到下架状态，需要校验是否存在待提货的订单
        if (status == ProductStatusEnum.OFF.getCode()) {
            // 检查待提货订单
            QueryWrapper<ProductOrder> pendingPickupQuery = new QueryWrapper<>();
            pendingPickupQuery.eq("product_id", id)
                    .eq("status", ProductOrderStatusEnum.PENDING_PICKUP.getCode());
            
            Long pendingPickupCount = productOrderMapper.selectCount(pendingPickupQuery);
            if (pendingPickupCount > 0) {
                log.error("商品存在待提货的订单，不允许下架，商品ID: {}, 待提货订单数: {}", id, pendingPickupCount);
                throw new ServiceException("该商品存在" + pendingPickupCount + "个待提货的订单，不允许下架");
            }
            
            // 检查待支付订单
            QueryWrapper<ProductOrder> nonpaymentQuery = new QueryWrapper<>();
            nonpaymentQuery.eq("product_id", id)
                    .eq("status", ProductOrderStatusEnum.NONPAYMENT.getCode());
            
            Long nonpaymentCount = productOrderMapper.selectCount(nonpaymentQuery);
            if (nonpaymentCount > 0) {
                log.error("商品存在待支付的订单，不允许下架，商品ID: {}, 待支付订单数: {}", id, nonpaymentCount);
                throw new ServiceException("该商品存在" + nonpaymentCount + "个待支付的订单，不允许下架");
            }
        }
        
        // 更新状态
        Product updateProduct = new Product();
        updateProduct.setId(id);
        updateProduct.setStatus(status);
        updateProduct.setUpdateTime(LocalDateTime.now());
        
        productMapper.updateById(updateProduct);
        log.info("产品状态已切换，ID: {}，新状态: {}", id, status);
    }

    @Override
    public void updateProduct(Long id, ProductDTO productDTO) {
        // 校验价格
        validateProductPrice(productDTO);
        
        if(null==id)
        {
            log.error("没有传入产品ID");
            throw new ServiceException("没有传入产品ID");
        }
        productDTO.setId(id);
        productDTO.setUpdateTime(LocalDateTime.now());
        Product product = CopyTools.copy(productDTO, Product.class);

        productMapper.updateById(product);

        //       更新地址
        QueryWrapper<Location> locationQueryWrapper = new QueryWrapper<>();
        locationQueryWrapper.eq("location_product_id", id);

        Location newLocation = productDTO.getLocation();
        if(null!=newLocation)
        {
            newLocation.setId(null);
            newLocation.setUpdatedTime(LocalDateTime.now());
            locationMapper.update(newLocation,locationQueryWrapper);
        }
    }
    
    /**
     * 验证产品价格
     *
     * @param productDTO 产品DTO
     * @throws ServiceException 价格不符合规则时抛出异常
     */
    private void validateProductPrice(ProductDTO productDTO) {
        // 检查产品价格
        BigDecimal price = productDTO.getPrice();
        if (price == null) {
            throw new ServiceException("产品价格不能为空");
        }
        
        // 检查价格不为0且不小于0.01
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("产品价格不能小于或等于0");
        }
        
        if (price.compareTo(new BigDecimal("0.01")) < 0) {
            throw new ServiceException("产品价格最小为0.01");
        }
        
        // 检查价格小数位数不超过2位
        if (price.scale() > 2) {
            throw new ServiceException("产品价格小数位不能超过2位");
        }
    }
}
