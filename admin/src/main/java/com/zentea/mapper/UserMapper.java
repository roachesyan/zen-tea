package com.zentea.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zentea.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
