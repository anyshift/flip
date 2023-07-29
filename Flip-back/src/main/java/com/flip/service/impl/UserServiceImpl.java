package com.flip.service.impl;

import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flip.domain.Response;
import com.flip.entity.Authority;
import com.flip.entity.Post;
import com.flip.entity.Role;
import com.flip.entity.User;
import com.flip.entity.dto.LoggedUser;
import com.flip.mapper.AuthorityMapper;
import com.flip.mapper.PostMapper;
import com.flip.mapper.RoleMapper;
import com.flip.mapper.UserMapper;
import com.flip.service.UserService;
import com.flip.utils.RedisKeyUtils;
import com.flip.utils.elastic.ElasticUserUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Validated
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${upload.avatarPath}")
    private String avatarPath; /*上传的头像存储的地址*/

    @Value("${upload.avatarMapperPath}")
    private String avatarMapperPath; /*上传的头像映射的虚拟路径地址*/

    @Value("${avatar.prefix}")
    private String avatarPrefix;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private AuthorityMapper authorityMapper;

    @Resource
    private PostMapper postMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取已登录用户的个人信息
     * @return 个人信息
     */
    @Override
    public Response<Object> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoggedUser loggedUser = (LoggedUser) authentication.getPrincipal();
        User user = loggedUser.getUser();

        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("uid", user.getUid());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname());
        map.put("avatar", user.getAvatar());
        map.put("email", user.getEmail());
        map.put("emailVerified", user.getEmailVerified());
        map.put("registerIp", user.getRegisterIp());
        map.put("createTime", user.getCreateTime());
        map.put("role", user.getRole().getName());
        return Response.success(200, map);
    }

    @Override
    public Response<List<User>> getAllUser() {
        List<User> users = userMapper.selectList(null);
        users.forEach(user -> {
            Role role = loadUserRoleByUid(String.valueOf(user.getUid()));
            Authority authority = new Authority();
            authority.setAuthorities(loadRoleAuthoritiesByRid(role.getRid()));

            user.setPassword(null);
            user.setRole(role);
            user.setAuthority(authority);
        });

        return Response.success(200, Map.of("users", users));
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
    public Role loadUserRoleByUid(String uid) {
        return roleMapper.loadUserRoleByUid(uid);
    }

    @Override
    public List<String> loadRoleAuthoritiesByRid(int rid) {
        return authorityMapper.loadRoleAuthoritiesByRid(rid);
    }

    @Override
    public Response<String> checkNicknameUnique(String nickname, LoggedUser loggedUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("nickname", nickname).or().eq("username", nickname);
        Long result = userMapper.selectCount(queryWrapper);
        if (result.intValue() == 0) {
            return Response.success(200, "昵称可用");
        }

        // 用户名与昵称不相同时，如果新昵称与用户名相同，则新昵称也可用。（相当于重置为默认昵称）
        if (loggedUser.getUser().getUsername().equals(nickname)) {
            return Response.success(200, "昵称可用");
        }

        return Response.error(400, "该昵称不可用");
    }

    @Override
    public Response<String> updateNickname(String nickname) {
        LoggedUser loggedUser = (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (Objects.equals(loggedUser.getUser().getNickname(), nickname)) {
            return Response.success(200, "昵称无变化");
        }

        Response<String> checkNicknameUniqueResult = checkNicknameUnique(nickname, loggedUser);
        if (checkNicknameUniqueResult.getCode() != 200) {
            return checkNicknameUniqueResult;
        }

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", loggedUser.getUser().getUid()).set("nickname", nickname);
        int update = userMapper.update(loggedUser.getUser(), updateWrapper);
        if (update > 0) {
            loggedUser.getUser().setNickname(nickname);
            String loggedUserKey = RedisKeyUtils.getLoggedUserKey(String.valueOf(loggedUser.getUser().getUid()));
            redisTemplate.opsForValue().set(loggedUserKey, loggedUser);

            try {
                ElasticUserUtils.updateUserNicknameInEs(elasticsearchClient, loggedUser.getUser());
            } catch (IOException e) {
                return Response.success(200, "昵称已变更");
            }
            return Response.success(200, "昵称已变更");
        }

        return Response.error(400, "发生未知错误");
    }

    @Override
    public Response<String> checkPassword(BCryptPasswordEncoder passwordEncoder, LoggedUser loggedUser, String password) {
        boolean matches = passwordEncoder.matches(password, loggedUser.getPassword());
        if (matches) {
            return Response.success(200, "密码匹配");
        } else return Response.error(400, "当前密码错误");
    }

    @Override
    public Response<String> updatePassword(String currentPassword, String newPassword) {
        LoggedUser loggedUser = (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        Response<String> checkPasswordResult = checkPassword(passwordEncoder, loggedUser, currentPassword);
        if (checkPasswordResult.getCode() != 200) {
            return checkPasswordResult;
        }

        // 检查新密码是否和数据库中的密码一致，一致就停止往下执行
        if (checkPassword(passwordEncoder, loggedUser, newPassword).getCode() == 200) {
            return Response.success(200, "密码无变化");
        }

        // 加密新密码
        String encodeNewPassword = passwordEncoder.encode(newPassword);

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", loggedUser.getUser().getUid()).set("password", encodeNewPassword);
        int update = userMapper.update(loggedUser.getUser(), updateWrapper);
        if (update > 0) {
            loggedUser.getUser().setPassword(encodeNewPassword);
            String loggedUserKey = RedisKeyUtils.getLoggedUserKey(String.valueOf(loggedUser.getUser().getUid()));
            redisTemplate.opsForValue().set(loggedUserKey, loggedUser);
            return Response.success(200, "密码修改成功");
        }
        return Response.error(400, "发生未知错误");
    }

    @Override
    public Response<Object> updateAvatar(MultipartFile avatar) {
        LoggedUser loggedUser = (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String uid = String.valueOf(loggedUser.getUser().getUid());
        String avatarType = Objects.requireNonNull(avatar.getContentType()).substring(6);
        String avatarName = System.currentTimeMillis() + "." + avatarType;

        File path = new File(avatarPath + loggedUser.getUser().getUid());
        if (!path.exists()) {
            boolean ready = path.mkdirs();
            if (!ready) return Response.error(400, "发生未知错误");
        }

        File realPath = new File(path.getAbsolutePath() + File.separator + avatarName);

        try {
            avatar.transferTo(realPath);
        } catch (IOException e) {
            throw new RuntimeException("转移头像文件时出错");
        }

        String totalAvatarPath = avatarPrefix + avatarMapperPath + uid + "/" + avatarName;

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", loggedUser.getUser().getUid()).set("avatar", totalAvatarPath);
        int update = userMapper.update(loggedUser.getUser(), updateWrapper);
        if (update > 0) {
            loggedUser.getUser().setAvatar(totalAvatarPath);
            redisTemplate.opsForValue().set(RedisKeyUtils.getLoggedUserKey(uid), loggedUser);

            try {
                ElasticUserUtils.updateUserAvatarInEs(elasticsearchClient, loggedUser.getUser());
            } catch (IOException e) {
                return Response.success(200, "更新头像成功", Map.of("newAvatar", totalAvatarPath));
            }
            return Response.success(200, "更新头像成功", Map.of("newAvatar", totalAvatarPath));
        }

        return Response.success(400, "设置头像失败");
    }

    @Override
    public Response<Object> getUserPosts(String username) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("username", username).select("uid");

        User user = userMapper.selectOne(userQueryWrapper);
        if (ObjectUtil.isNotNull(user)) {
            QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
            postQueryWrapper.eq("uid", user.getUid())
                    .select("id, uid, title, create_time, priority, status, type, reply_number, view_number")
                    .orderByDesc("create_time");

            List<Post> posts = postMapper.selectList(postQueryWrapper);

            if (ObjectUtil.isNotNull(posts)) {
                return Response.success(200, Map.of("posts", posts));
            }

            return Response.error(400, "查询主题失败");
        }

        return Response.error(400, "无此用户，查询主题失败");
    }

    @Override
    public Response<Object> getUserProfile(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        String select = "id, uid, username, nickname, avatar, email, register_ip AS registerIp, create_time AS createTime";
        queryWrapper.eq("username", username).select(select);

        User user = userMapper.selectOne(queryWrapper);

        if (ObjectUtil.isNotNull(user)) {
            Role role = loadUserRoleByUid(String.valueOf(user.getUid()));

            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("uid", user.getUid());
            map.put("username", user.getUsername());
            map.put("nickname", user.getNickname());
            map.put("avatar", user.getAvatar());
            map.put("email", user.getEmail());
            map.put("emailVerified", user.getEmailVerified());
            map.put("registerIp", user.getRegisterIp());
            map.put("createTime", user.getCreateTime());
            map.put("role", role);

            return Response.success(200, map);
        }

        return Response.error(400, "没有找到该用户");
    }
}
