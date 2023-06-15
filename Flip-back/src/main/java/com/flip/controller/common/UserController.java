package com.flip.controller.common;

import com.flip.domain.Response;
import com.flip.service.UserService;
import com.flip.validation.VG;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 修改用户个人信息控制器
 */
@Slf4j
@Validated(VG.class)
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public Response<Object> getProfile() {
        return userService.getProfile();
    }

    @GetMapping("/userProfile")
    public Response<Object> getUserProfile(@RequestParam String username) {
        return userService.getUserProfile(username);
    }

    @GetMapping("/userPosts")
    public Response<Object> getUserPosts(@RequestParam String username) {
        return userService.getUserPosts(username);
    }

    @GetMapping("/updateNickname")
    public Response<String> updateNickname(
            @NotBlank(message = "昵称不能为空", groups = {VG.First.class})
            @Pattern(regexp = "^[\\u4E00-\\u9FA5A-Za-z\\d]+$", message = "昵称不能包含特殊字符", groups = {VG.Second.class})
            @Length(min = 2, message = "昵称至少两位字符", groups = {VG.Third.class})
            @Length(max = 10, message = "昵称至多十位字符", groups = {VG.Fourth.class})
            @RequestParam String nickname) {
        return userService.updateNickname(nickname);
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

        if (!newPassword.equals(confirmNewPassword)) return Response.error(400, "两次输入的密码不匹配");
        return userService.updatePassword(currentPassword, newPassword);
    }

    @PostMapping("/updateAvatar")
    public Response<Object> updateAvatar(@RequestParam("file") MultipartFile avatar) {
        if (avatar.isEmpty()) {
            return Response.error(400, "上传失败");
        }
        return userService.updateAvatar(avatar);
    }
}
