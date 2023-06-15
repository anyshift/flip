package com.flip.controller.admin;

import com.flip.domain.Response;
import com.flip.entity.User;
import com.flip.entity.vo.BannedUser;
import com.flip.service.BannedUserService;
import com.flip.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys-ctrl")
public class UserSysController {
    @Resource
    private UserService userService;

    @Resource
    BannedUserService bannedUserService;

    @GetMapping("/users")
    public Response<List<User>> getAllUser() {
        return userService.getAllUser();
    }

    @PostMapping("/banUser")
    public Response<Object> banUser(@RequestBody BannedUser bannedUser) {
        return bannedUserService.banUser(bannedUser);
    }

    @DeleteMapping("/banUser")
    public Response<Object> cancelBanUser(@RequestParam String uid) {
        return bannedUserService.cancelBanUser(uid);
    }
}
