package com.flip.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flip.entity.vo.BannedUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BannedUserMapper extends BaseMapper<BannedUser> {
    int insertBannedHistory(Long uid, String createTime, String deadline, String reason);
}
