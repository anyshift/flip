package com.flip.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flip.entity.Authority;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuthorityMapper extends BaseMapper<Authority> {
    List<String> loadRoleAuthoritiesByRid(@Param("rid") int rid); //根据角色ID获取该角色所有权限信息
}
