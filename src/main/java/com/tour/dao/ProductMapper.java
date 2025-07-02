package com.tour.dao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tour.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
/**
 * @description productMapper
 * @author kuril
 * @date 2025-05-08
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select(
    "<script>select t0.* from product t0 " +
    //add here if need left join
    "where 1=1" +
    "<when test='name!=null and name!=&apos;&apos; '> and t0.name=#{name}</when> " +
    "<when test='description!=null and description!=&apos;&apos; '> and t0.description=#{description}</when> " +
    "<when test='price!=null and price!=&apos;&apos; '> and t0.price=#{price}</when> " +
    "<when test='stock!=null and stock!=&apos;&apos; '> and t0.stock=#{stock}</when> " +
    "<when test='coverImage!=null and coverImage!=&apos;&apos; '> and t0.cover_image=#{coverImage}</when> " +
    "<when test='createdBy!=null and createdBy!=&apos;&apos; '> and t0.created_by=#{createdBy}</when> " +
    "<when test='updatedBy!=null and updatedBy!=&apos;&apos; '> and t0.updated_by=#{updatedBy}</when> " +
    "<when test='createdTime!=null and createdTime!=&apos;&apos; '> and t0.created_time=#{createdTime}</when> " +
    "<when test='updatedTime!=null and updatedTime!=&apos;&apos; '> and t0.updated_time=#{updatedTime}</when> " +
    //add here if need page limit
    //" limit ${page},${limit} " +
    " </script>")
    List<Product> pageAll(Product queryParamDTO, int page, int limit);

    @Select("<script>select count(1) from product t0 " +
    //add here if need left join
    "where 1=1" +
    "<when test='name!=null and name!=&apos;&apos; '> and t0.name=#{name}</when> " +
    "<when test='description!=null and description!=&apos;&apos; '> and t0.description=#{description}</when> " +
    "<when test='price!=null and price!=&apos;&apos; '> and t0.price=#{price}</when> " +
    "<when test='stock!=null and stock!=&apos;&apos; '> and t0.stock=#{stock}</when> " +
    "<when test='coverImage!=null and coverImage!=&apos;&apos; '> and t0.cover_image=#{coverImage}</when> " +
    "<when test='createdBy!=null and createdBy!=&apos;&apos; '> and t0.created_by=#{createdBy}</when> " +
    "<when test='updatedBy!=null and updatedBy!=&apos;&apos; '> and t0.updated_by=#{updatedBy}</when> " +
    "<when test='createdTime!=null and createdTime!=&apos;&apos; '> and t0.created_time=#{createdTime}</when> " +
    "<when test='updatedTime!=null and updatedTime!=&apos;&apos; '> and t0.updated_time=#{updatedTime}</when> " +
     " </script>")
    int countAll(Product queryParamDTO);

}