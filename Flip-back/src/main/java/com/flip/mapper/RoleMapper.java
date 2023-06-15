package com.flip.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flip.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    Role loadUserRoleByUid(@Param("uid") String uid); //根据用户UID获取用户角色信息
}
