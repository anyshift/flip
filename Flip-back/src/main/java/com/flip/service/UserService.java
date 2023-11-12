package com.flip.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flip.domain.dto.LoggedUser;
import com.flip.domain.entity.Role;
import com.flip.domain.entity.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

public interface UserService extends IService<User>{

    User loadUserByUsername(String username);

    User loadUserByUid(Long uid);

    Role loadUserRoleByUid(String uid);

    List<String> loadRoleAuthoritiesByRid(int rid);

    Boolean checkNicknameUnique(String nickname, LoggedUser loggedUser);

    Boolean correctPassword(BCryptPasswordEncoder passwordEncoder, LoggedUser loggedUser, String password);

    void updateUserRole(Long uid, Integer rid);
}
