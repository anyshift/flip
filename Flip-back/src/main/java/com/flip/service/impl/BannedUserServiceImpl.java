package com.flip.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flip.domain.entity.BannedUser;
import com.flip.mapper.BannedUserMapper;
import com.flip.service.BannedUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class BannedUserServiceImpl extends ServiceImpl<BannedUserMapper, BannedUser> implements BannedUserService {

    @Resource
    private BannedUserMapper bannedUserMapper;

    @Override
    public void insertBannedHistory(Long uid, String createTime, String deadline, String reason) {
        bannedUserMapper.insertBannedHistory(uid, createTime, deadline, reason);
    }
}
