package com.flip.service;

import com.flip.domain.Response;
import com.flip.entity.vo.BannedUser;

public interface BannedUserService {
    Response<Object> banUser(BannedUser bannedUser);

    Response<Object> cancelBanUser(String uid);
}
