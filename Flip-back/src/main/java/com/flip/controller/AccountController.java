package com.flip.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.flip.common.Response;
import com.flip.domain.dto.LoggedUser;
import com.flip.domain.entity.BannedUser;
import com.flip.domain.entity.Role;
import com.flip.domain.entity.User;
import com.flip.domain.enums.ResponseCode;
import com.flip.service.BannedUserService;
import com.flip.service.UserService;
import com.flip.service.impl.AccountServiceImpl;
import com.flip.utils.JwtUtils;
import com.flip.utils.RedisKeyUtils;
import com.flip.utils.elastic.ElasticUserUtils;
import com.flip.validation.VG;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 账号 登录、注册、验证 控制器
 */
@Slf4j
@Validated(VG.class)
@RestController
public class AccountController {

    @Value("${jwt.auto-refresh-ttl}")
    private Long autoRefreshTTL;

    @Resource
    private AccountServiceImpl accountService;

    @Resource
    private UserService userService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private BannedUserService bannedUserService;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    /**
     * 用户注册
     * @param user 用户实体类
     * @param request 请求对象
     * @param code 图形验证码
     * @param codeOwner 图形验证码所属者
     * @return result
     */
    @PostMapping("/register")
    public Response<Object> register(@RequestBody @Validated(VG.class) User user, HttpServletRequest request,
                                     @RequestParam String code, @RequestParam String codeOwner) {
        Response<Object> captchaCheckResult = this.checkCaptcha(code, codeOwner);
        if (captchaCheckResult.getCode() != 200) return captchaCheckResult;

        Boolean unique = accountService.checkUsernameUnique(user.getUsername());
        if (!unique) return Response.failed("用户名已存在");

        Response<Object> emailUniqueResult = accountService.checkEmailUnique(user.getEmail());
        if (emailUniqueResult.getCode() != 200) return emailUniqueResult;

        Boolean registered = accountService.register(user, request, code, codeOwner);
        if (registered) {
            return Response.success("注册成功");
        }
        return Response.failed("注册失败");
    }

    /**
     * 用户登录
     * @param user 用户
     * @param code 图形验证码
     * @param codeOwner 图形验证码所属者
     * @return result
     */
    @PostMapping("/login")
    public Response<Object> login(@RequestBody User user, @RequestParam String code, @RequestParam String codeOwner) {
        Response<Object> captchaCheckResult = this.checkCaptcha(code, codeOwner);
        if (captchaCheckResult.getCode() != 200) return captchaCheckResult;

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user.getUsername().trim(), user.getPassword().trim());

        // authenticate 会调用 UserDetailsServiceImpl.loadUserByUsername()
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        LoggedUser loggedUser = (LoggedUser) authentication.getPrincipal();
        if (Boolean.TRUE.equals(loggedUser.getUser().getBanned())) {
            QueryWrapper<BannedUser> bannedUserQueryWrapper = new QueryWrapper<>();
            bannedUserQueryWrapper.eq("uid", loggedUser.getUser().getUid());

            BannedUser bannedUser = bannedUserService.getOne(bannedUserQueryWrapper);
            if (ObjectUtil.isNotEmpty(bannedUser.getDeadline())) {
                if (bannedUser.getDeadline().startsWith("2099")) {
                    return Response.failed("登录失败，账号已被永久封禁");
                } else {
                    String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    if (now.compareTo(bannedUser.getDeadline()) < 0) {
                        return Response.failed("登录失败，账号已被封禁", Map.of("deadline", bannedUser.getDeadline()));
                    }
                }
            }
        }

        String uid = String.valueOf(loggedUser.getUser().getUid());
        String accessToken = jwtUtils.createAccessToken(uid);
        String refreshToken = jwtUtils.createRefreshToken(uid);

        redisTemplate.opsForValue().set(RedisKeyUtils.getLoggedUserKey(uid), loggedUser);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Map<String, Object> map = new HashMap<>();
        map.put("token", accessToken);
        map.put("refreshToken", refreshToken);

        log.info("{} logged in", loggedUser.getUser().getUsername());

        return Response.success(200, "登录成功", map);
    }

    @GetMapping("/activate")
    public Response<String> getActivateEmailCode(@RequestParam String uid) {
        String emailCode = String.valueOf(RandomUtil.randomInt(111111, 999999));

        String emailCodeRequestTimesKey = RedisKeyUtils.getEmailCodeRequestTimesKey(uid);
        Integer requestTimes = (Integer) redisTemplate.opsForValue().get(emailCodeRequestTimesKey);
        if (ObjectUtil.isNotNull(requestTimes) && requestTimes >= 3) {
            return Response.failed("请求过于频繁，请一天后重试");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).select("email");
        User user = userService.getOne(queryWrapper);

        Boolean sent = accountService.sendEmailCode(user.getEmail(), emailCode);
        if (sent) {
            String emailCodeKey = RedisKeyUtils.getEmailCodeKey(uid);
            redisTemplate.opsForValue().set(emailCodeKey, emailCode, 15, TimeUnit.MINUTES);

            Boolean onceSent = redisTemplate.hasKey(emailCodeRequestTimesKey);
            if (Boolean.TRUE.equals(onceSent)) {
                redisTemplate.opsForValue().increment(emailCodeRequestTimesKey);
            } else {
                redisTemplate.opsForValue().set(emailCodeRequestTimesKey, 1, 1, TimeUnit.DAYS);
            }

            return Response.success("已发送验证码");
        } else {
            return Response.failed("获取验证码失败");
        }
    }

    /**
     * 激活邮箱
     * @param user 用户
     * @param emailCode 邮箱验证码
     * @return result
     */
    @PutMapping("/activate")
    public Response<Object> activateEmail(@RequestBody User user, @RequestParam String emailCode) {
        Long uid = user.getUid();
        String emailCodeKey = RedisKeyUtils.getEmailCodeKey(String.valueOf(uid));
        String realEmailCode = (String) redisTemplate.opsForValue().get(emailCodeKey);

        if (ObjectUtil.isNull(realEmailCode)) {
            return Response.failed("验证码已过期");
        };
        if (!realEmailCode.equals(emailCode)) {
            return Response.failed("验证码错误");
        }

        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("uid", String.valueOf(uid)).set("email_verified", true);
        boolean updated = userService.update(userUpdateWrapper);
        if (updated) {
            String loggedUserKey = RedisKeyUtils.getLoggedUserKey(String.valueOf(uid));
            LoggedUser loggedUser = (LoggedUser) redisTemplate.opsForValue().get(loggedUserKey);
            if (ObjectUtil.isNotNull(loggedUser)) {
                Role role = loggedUser.getUser().getRole();
                if (role.getRid() != 1 && role.getRid() != 2) {
                    role.setRid(3);
                    userService.updateUserRole(uid, 3);
                }
                loggedUser.getUser().setRole(role);
                loggedUser.getUser().setEmailVerified(true);
                redisTemplate.opsForValue().set(loggedUserKey, loggedUser);
            }

            try {
                user.setEmailVerified(true);
                ElasticUserUtils.updateUserEmailVerifiedInEs(elasticsearchClient, user);
            } catch (IOException e) {
                return Response.success("邮箱已认证激活");
            }
            return Response.success("邮箱已认证激活");
        } else return Response.failed("出现错误，请稍后重试");
    }

    /**
     * 刷新访问凭证
     * @param refreshToken 刷新凭证
     * @return result
     */
    @PostMapping("/refresh")
    public Response<Map<String, Object>> refreshToken(@NotBlank(message = "refreshToken不能为空", groups = {VG.First.class}) @RequestParam String refreshToken) {
        if (jwtUtils.isTokenValid(refreshToken)) {
            Map<String, Object> map = new HashMap<>();
            Claims claims = jwtUtils.parseTokenToClaims(refreshToken);
            if ((claims.getExpiration().getTime() - DateUtil.date().getTime()) <= autoRefreshTTL) {
                String newRefreshToken = jwtUtils.createRefreshToken(claims.getSubject());
                map.put("refreshToken", newRefreshToken);
                log.info("{}'s refreshToken recreated.", claims.getSubject());
            }
            String accessToken = jwtUtils.createAccessToken(claims.getSubject());
            map.put("token", accessToken);

            log.info("{}'s token refreshed.", claims.getSubject());

            return Response.success("凭证已刷新", map);
        } else return Response.failed(ResponseCode.AUTHENTICATION_EXPIRED.getCode(), "请重新登录");
    }

    /**
     * 校验用户名唯一性
     * @param username 用户名
     * @return 响应体
     */
    @GetMapping("/_checkUsernameUnique")
    public Response<Object> checkUsernameUnique(@RequestParam String username) {
        if (accountService.checkUsernameUnique(username)) {
            return Response.success("校验通过");
        }
        return Response.failed("用户名已存在");
    }

    /**
     * 获取 Hutool 图片验证码
     * @return 验证码响应体
     */
    @GetMapping("/_captcha")
    public Response<Map<String, String>> getCaptcha() {
        RandomGenerator randomGenerator = new RandomGenerator("abcdefghjkmnpqrstuvwxyz123456789", 4);
        CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(200, 100);
        circleCaptcha.setGenerator(randomGenerator);

        circleCaptcha.createCode();
        String code = circleCaptcha.getCode();

        String codeImageBase64 = circleCaptcha.getImageBase64Data();

        String uuid = IdUtil.simpleUUID();

        /* 验证码的拥有者，只有该拥有者可以从Redis中取出验证码 */
        String codeOwnerKey = RedisKeyUtils.getCaptchaKey(uuid);

        /* 以拥有者的UUID为key，以验证码值为value，超时时间为三分钟 */
        redisTemplate.opsForValue().set(codeOwnerKey, code, 3, TimeUnit.MINUTES);

        Map<String, String> map = new HashMap<>();
        map.put("codeImage", codeImageBase64);
        map.put("codeOwner", uuid);

        return Response.success("获取验证码成功", map);
    }

    /**
     * 校验图形验证码输入是否正确
     * @param code 图形验证码
     * @param codeOwner 图形验证码所属者
     * @return 响应体
     */
    @GetMapping("/_checkCaptcha")
    public Response<Object> checkCaptcha(@RequestParam String code, @RequestParam String codeOwner) {
        if (StrUtil.isBlank(code)) {
            return Response.failed("请输入图形验证码");
        }

        String captchaKey = RedisKeyUtils.getCaptchaKey(codeOwner);
        String trueCode = (String) redisTemplate.opsForValue().get(captchaKey);
        if (StrUtil.isBlank(trueCode)) {
            return Response.failed("图形验证码已过期");
        }

        if (!code.equalsIgnoreCase(trueCode)) {
            return Response.failed("请输入正确的图形验证码");
        }

        return Response.success("图形验证码校验通过");
    }
}
