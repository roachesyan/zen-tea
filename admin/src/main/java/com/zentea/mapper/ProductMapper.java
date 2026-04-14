package com.zentea.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zentea.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
