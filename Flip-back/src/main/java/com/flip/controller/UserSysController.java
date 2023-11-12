package com.flip.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.flip.common.Response;
import com.flip.domain.entity.*;
import com.flip.service.BannedUserService;
import com.flip.service.UserService;
import com.flip.utils.elastic.ElasticUserUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sys-ctrl")
public class UserSysController {
    @Resource
    private UserService userService;

    @Resource
    BannedUserService bannedUserService;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @GetMapping("/users")
    public Response<Map<String, List<User>>> getAllUser() {
        List<User> users = userService.list();
        users.forEach(user -> {
            Role role = userService.loadUserRoleByUid(String.valueOf(user.getUid()));
            Authority authority = new Authority();
            authority.setAuthorities(userService.loadRoleAuthoritiesByRid(role.getRid()));

            user.setPassword(null);
            user.setRole(role);
            user.setAuthority(authority);
        });

        return Response.success("获取所有用户成功", Map.of("users", users));
    }

    @PostMapping("/banUser")
    public Response<Object> banUser(@RequestBody BannedUser bannedUser) {
        boolean saved = bannedUserService.save(bannedUser);
        if (saved) {
            Long uid = bannedUser.getUid();
            String reason = bannedUser.getReason();
            String deadline = bannedUser.getDeadline();
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            bannedUserService.insertBannedHistory(uid, now, deadline, reason);

            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
            userUpdateWrapper.eq("uid", uid).set("banned", true);
            userService.update(null, userUpdateWrapper);

            User user = new User();
            user.setUid(uid);
            user.setBanned(true);
            try {
                ElasticUserUtils.updateUserBannedStatusInEs(elasticsearchClient, user);
            } catch (IOException e) {
                return Response.success("封禁成功");
            }
            return Response.success("封禁成功");
        } else return Response.failed("封禁失败");
    }

    @DeleteMapping("/banUser")
    public Response<Object> cancelBanUser(@RequestParam String uid) {
        QueryWrapper<BannedUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);

        boolean removed = bannedUserService.remove(queryWrapper);
        if (removed) {
            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
            userUpdateWrapper.eq("uid", uid).set("banned", false);
            userService.update(null, userUpdateWrapper);

            User user = new User();
            user.setUid(Long.valueOf(uid));
            user.setBanned(false);
            try {
                ElasticUserUtils.updateUserBannedStatusInEs(elasticsearchClient, user);
            } catch (IOException e) {
                return Response.success("解除封禁成功");
            }
            return Response.success("解除封禁成功");
        } else return Response.failed("解除封禁失败");
    }
}
