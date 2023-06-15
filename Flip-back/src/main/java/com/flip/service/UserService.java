package com.flip.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flip.domain.Response;
import com.flip.entity.Role;
import com.flip.entity.User;
import com.flip.entity.dto.LoggedUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService extends IService<User>{

    User loadUserByUsername(String username);

    Role loadUserRoleByUid(String uid);

    List<String> loadRoleAuthoritiesByRid(int rid);

    Response<String> checkNicknameUnique(String nickname, LoggedUser loggedUser);

    Response<String> updateNickname(String nickname);

    Response<String> checkPassword(BCryptPasswordEncoder passwordEncoder, LoggedUser loggedUser, String password);

    Response<String> updatePassword(String currentPassword, String newPassword);

    Response<Object> updateAvatar(MultipartFile avatar);

    Response<Object> getUserPosts(String username);

    Response<Object> getUserProfile(String username);

    Response<Object> getProfile();

    Response<List<User>> getAllUser();
}
