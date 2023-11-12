package com.flip.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flip.domain.entity.BannedUser;

public interface BannedUserService extends IService<BannedUser> {

    void insertBannedHistory(Long uid, String createTime, String deadline, String reason);

}
