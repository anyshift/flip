package com.flip.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flip.domain.entity.BannedUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BannedUserMapper extends BaseMapper<BannedUser> {

    void insertBannedHistory(Long uid, String createTime, String deadline, String reason);

}
