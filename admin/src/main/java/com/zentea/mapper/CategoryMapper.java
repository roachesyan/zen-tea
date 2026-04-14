package com.zentea.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zentea.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
