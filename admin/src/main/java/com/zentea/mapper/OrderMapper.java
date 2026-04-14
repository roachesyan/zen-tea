package com.zentea.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zentea.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
