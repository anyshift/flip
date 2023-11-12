package com.flip.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flip.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    int insertUserRole(@Param("uid") long uid, @Param("rid") int rid);

    void updateUserRole(@Param("uid") long uid, @Param("rid") int rid);

}
