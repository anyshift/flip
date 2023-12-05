package com.flip.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flip.domain.dto.LoggedUser;
import com.flip.domain.entity.Authority;
import com.flip.domain.entity.Role;
import com.flip.domain.entity.User;
import com.flip.mapper.AuthorityMapper;
import com.flip.mapper.RoleMapper;
import com.flip.mapper.UserMapper;
import com.flip.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    private final RoleMapper roleMapper;

    private final AuthorityMapper authorityMapper;

    @Override
    public void updateUserRole(Long uid, Integer rid) {
        userMapper.updateUserRole(uid, rid);
    }

    @Override
    public User loadUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNull(user)) {
            throw new UsernameNotFoundException("请检查用户名是否输入正确");
        }
        Role role = loadUserRoleByUid(String.valueOf(user.getUid()));
        user.setRole(role);

        List<String> Authorities = loadRoleAuthoritiesByRid(user.getRole().getRid());
        Authority authority = new Authority();
        authority.setAuthorities(Authorities);
        user.setAuthority(authority);

        return user;
    }

    @Override
    public User loadUserByUid(Long uid) {
        return userMapper.selectById(uid);
    }

    @Override
    public Role loadUserRoleByUid(String uid) {
        return roleMapper.loadUserRoleByUid(uid);
    }

    @Override
    public List<String> loadRoleAuthoritiesByRid(int rid) {
        return authorityMapper.loadRoleAuthoritiesByRid(rid);
    }

    @Override
    public Boolean checkNicknameUnique(String nickname, LoggedUser loggedUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("nickname", nickname).or().eq("username", nickname);
        Long result = userMapper.selectCount(queryWrapper);
        if (result.intValue() == 0) {
            return true;
        }

        // 用户名与昵称不相同时，如果新昵称与用户名相同，则新昵称也可用。（相当于重置为默认昵称）
        return loggedUser.getUser().getUsername().equals(nickname);
    }

    @Override
    public Boolean correctPassword(BCryptPasswordEncoder passwordEncoder, LoggedUser loggedUser, String password) {
        return passwordEncoder.matches(password, loggedUser.getPassword());
    }

}
