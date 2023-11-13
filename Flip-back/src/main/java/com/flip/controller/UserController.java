package com.flip.controller;

import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.flip.common.Response;
import com.flip.domain.dto.LoggedUser;
import com.flip.domain.entity.Post;
import com.flip.domain.entity.Role;
import com.flip.domain.entity.User;
import com.flip.service.PostService;
import com.flip.service.UserService;
import com.flip.utils.LoggedUserUtils;
import com.flip.utils.RedisKeyUtils;
import com.flip.utils.elastic.ElasticUserUtils;
import com.flip.validation.VG;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 修改用户个人信息控制器
 */
@Slf4j
@Validated(VG.class)
@RestController
public class UserController {

    /**
     * 上传的头像文件存储的地址
     */
    @Value("${upload.avatarPath}")
    private String avatarPath;

    /**
     * 上传的头像映射的虚拟路径地址
     */
    @Value("${upload.avatarMapperPath}")
    private String avatarMapperPath;

    /**
     * 头像访问前缀
     */
    @Value("${avatar.prefix}")
    private String avatarPrefix;

    @Resource
    private UserService userService;

    @Resource
    private PostService postService;

    @Resource
    private RedisTemplate<String, LoggedUser> redisTemplate;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @GetMapping("/profile")
    public Response<Object> getProfile() {
        User user = LoggedUserUtils.getLoggedUser().getUser();
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
        return Response.success("获取个人信息成功", map);
    }

    @GetMapping("/userProfile")
    public Response<Object> getUserProfile(@RequestParam String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        String select = "id, uid, username, nickname, avatar, email, register_ip AS registerIp, create_time AS createTime";
        queryWrapper.eq("username", username).select(select);

        User user = userService.getOne(queryWrapper);

        if (ObjectUtil.isNotNull(user)) {
            Role role = userService.loadUserRoleByUid(String.valueOf(user.getUid()));
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
            return Response.success("获取用户信息成功", map);
        }

        return Response.failed("没有找到该用户");
    }

    @GetMapping("/userPosts")
    public Response<Object> getUserPosts(@RequestParam String username) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("username", username).select("uid");

        User user = userService.getOne(userQueryWrapper);
        if (ObjectUtil.isNotNull(user)) {
            QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
            postQueryWrapper.eq("uid", user.getUid())
                    .select("id, uid, title, create_time, priority, status, type, reply_number, view_number")
                    .orderByDesc("create_time");

            List<Post> posts = postService.list(postQueryWrapper);

            if (ObjectUtil.isNotNull(posts)) {
                return Response.success("获取用户已发布帖子成功", Map.of("posts", posts));
            }

            return Response.failed("获取用户已发布帖子失败");
        }

        return Response.failed("无此用户，查询帖子失败");
    }

    @GetMapping("/updateNickname")
    public Response<String> updateNickname(
            @NotBlank(message = "昵称不能为空", groups = {VG.First.class})
            @Pattern(regexp = "^[\\u4E00-\\u9FA5A-Za-z\\d]+$", message = "昵称不能包含特殊字符", groups = {VG.Second.class})
            @Length(min = 2, message = "昵称至少两位字符", groups = {VG.Third.class})
            @Length(max = 10, message = "昵称至多十位字符", groups = {VG.Fourth.class})
            @RequestParam String nickname) {

        LoggedUser loggedUser = LoggedUserUtils.getLoggedUser();

        if (Objects.equals(loggedUser.getUser().getNickname(), nickname)) {
            return Response.success("昵称无变化");
        }

        if (!userService.checkNicknameUnique(nickname, loggedUser)) {
            return Response.failed("昵称重复不可用");
        }

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", loggedUser.getUser().getUid()).set("nickname", nickname);
        boolean updated = userService.update(loggedUser.getUser(), updateWrapper);
        if (updated) {
            loggedUser.getUser().setNickname(nickname);
            String loggedUserKey = RedisKeyUtils.getLoggedUserKey(String.valueOf(loggedUser.getUser().getUid()));
            redisTemplate.opsForValue().set(loggedUserKey, loggedUser);

            try {
                ElasticUserUtils.updateUserNicknameInEs(elasticsearchClient, loggedUser.getUser());
            } catch (IOException e) {
                return Response.success("昵称已变更");
            }
            return Response.success("昵称已变更");
        }

        return Response.failed("发生未知错误");
    }

    @PostMapping("/updatePassword")
    public Response<String> updatePassword(
            @NotBlank(message = "当前密码不能为空", groups = {VG.First.class})
            @Length(min = 8, message = "当前密码至少8位字符", groups = {VG.Second.class})
            @Pattern(regexp = "(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[\\W_]).{8,}",
                    message = "当前密码需包含大小写字母、数字和特殊符号", groups = {VG.Third.class})
            @RequestParam String currentPassword,

            @NotBlank(message = "新密码不能为空", groups = {VG.Fourth.class})
            @Length(min = 8, message = "新密码至少8位字符", groups = {VG.Fifth.class})
            @Pattern(regexp = "(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[\\W_]).{8,}",
                    message = "新密码需包含大小写字母、数字和特殊符号", groups = {VG.Sixth.class})
            @RequestParam String newPassword,

            @NotBlank(message = "请再次输入新密码", groups = {VG.Seventh.class})
            @RequestParam String confirmNewPassword) {

        if (!newPassword.equals(confirmNewPassword)) return Response.failed("两次输入的密码不匹配");

        LoggedUser loggedUser = LoggedUserUtils.getLoggedUser();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (!userService.correctPassword(passwordEncoder, loggedUser, currentPassword)) {
            return Response.failed("当前密码不匹配");
        }

        // 加密新密码
        String encodeNewPassword = passwordEncoder.encode(newPassword);

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", loggedUser.getUser().getUid()).set("password", encodeNewPassword);
        boolean updated = userService.update(loggedUser.getUser(), updateWrapper);
        if (updated) {
            loggedUser.getUser().setPassword(encodeNewPassword);
            String loggedUserKey = RedisKeyUtils.getLoggedUserKey(String.valueOf(loggedUser.getUser().getUid()));
            redisTemplate.opsForValue().set(loggedUserKey, loggedUser);
            return Response.success("密码修改成功");
        }
        return Response.failed("发生未知错误");
    }

    @PostMapping("/updateAvatar")
    public Response<Object> updateAvatar(@RequestParam("file") MultipartFile avatar) {
        LoggedUser loggedUser = LoggedUserUtils.getLoggedUser();
        String uid = String.valueOf(loggedUser.getUser().getUid());
        String avatarType = Objects.requireNonNull(avatar.getContentType()).substring(6);
        String avatarName = System.currentTimeMillis() + "." + avatarType;

        File path = new File(avatarPath + loggedUser.getUser().getUid());
        if (!path.exists()) {
            boolean ready = path.mkdirs();
            if (!ready) return Response.failed("发生未知错误");
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
        boolean updated = userService.update(loggedUser.getUser(), updateWrapper);
        if (updated) {
            loggedUser.getUser().setAvatar(totalAvatarPath);
            redisTemplate.opsForValue().set(RedisKeyUtils.getLoggedUserKey(uid), loggedUser);

            try {
                ElasticUserUtils.updateUserAvatarInEs(elasticsearchClient, loggedUser.getUser());
            } catch (IOException e) {
                return Response.success("更新头像成功", Map.of("newAvatar", totalAvatarPath));
            }
            return Response.success("更新头像成功", Map.of("newAvatar", totalAvatarPath));
        }

        return Response.failed("设置头像失败");
    }
}
