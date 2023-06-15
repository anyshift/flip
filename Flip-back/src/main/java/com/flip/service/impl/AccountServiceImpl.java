package com.flip.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.flip.domain.Response;
import com.flip.entity.Role;
import com.flip.entity.User;
import com.flip.entity.dto.LoggedUser;
import com.flip.entity.vo.BannedUser;
import com.flip.mapper.BannedUserMapper;
import com.flip.mapper.UserMapper;
import com.flip.service.AccountService;
import com.flip.service.UserService;
import com.flip.utils.AvatarUtils;
import com.flip.utils.IpUtils;
import com.flip.utils.JwtUtils;
import com.flip.utils.RedisKeyUtils;
import com.flip.utils.elastic.ElasticUserUtils;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    @Value("${mail.from}")
    private String from;
    @Value("${mail.register.subject}")
    private String subject;

    @Value("${avatar.remote-prefix}")
    private String avatarRemotePrefix;

    @Value("${avatar.remote-suffix}")
    private String avatarRemoteSuffix;

    @Value("${upload.avatarPath}")
    private String avatarPath;

    @Value("${avatar.prefix}")
    private String avatarPrefix;

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private BannedUserMapper bannedUserMapper;

    @Resource
    private PasswordEncoder passwordEncoder;/* SecurityConfig 中注入的是 BcryptPasswordEncoder */

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JwtUtils jwtUtils;

    /**
     * 用户注册
     *
     * @param user           用户实体类
     * @param request        请求对象
     * @param captcha        图形验证码
     * @param captchaOwner   图形验证码所属者
     * @return 响应体
     */
    @Override
    @Transactional
    public Response<Object> register(User user,
                                     HttpServletRequest request,
                                     String captcha,
                                     String captchaOwner) {

        Response<Object> captchaCheckResult = checkCaptcha(captcha, captchaOwner);
        if (captchaCheckResult.getCode() != 200) return captchaCheckResult;

        Response<Object> usernameUniqueResult = checkUsernameUnique(user.getUsername());
        if (usernameUniqueResult.getCode() != 200) return usernameUniqueResult;

        Response<Object> emailUniqueResult = checkEmailUnique(user.getEmail());
        if (emailUniqueResult.getCode() != 200) return emailUniqueResult;

        /* 设置用户的注册IP */
        String ip = IpUtils.getIP(request);
        user.setRegisterIp(ip);

        /* 通过 BcryptPasswordEncode 加密算法加密前端传过来的用户密码 */
        String encodePassword = passwordEncoder.encode(user.getPassword());
        /* 原始密码替换为加密后的密码 */
        user.setPassword(encodePassword);

        /* 设置用户的盐值 */
        user.setSalt(IdUtil.simpleUUID().substring(0, 5));

        /* 设置默认头像地址 */


        /* 设置用户的角色 */
        Role role = new Role();
        Long totalUsers = userMapper.selectCount(null);
        if (ObjectUtil.isNull(totalUsers)) {
            role.setRid(1); //管理员
            user.setEmailVerified(true);
        } else {
            role.setRid(4); //未认证成员
            user.setEmailVerified(false);
        }
        user.setRole(role);

        user.setNickname(user.getUsername());

        /* 用户数据插入到数据库 */
        int insertUserResult = userMapper.insert(user);

        /* 用户角色插入到数据库 */
        int insertUserRoleResult = userMapper.insertUserRole(user.getUid(), user.getRole().getRid());

        /* 用户数据和角色数据插入数据库成功 */
        if (insertUserResult > 0 && insertUserRoleResult > 0) {
            Role realRole = userService.loadUserRoleByUid(String.valueOf(user.getUid()));
            user.setRole(realRole);

            String random = DigestUtil.md5Hex(user.getEmail() + user.getSalt());
            String avatar = AvatarUtils.createAvatar(avatarPath, avatarPrefix, avatarRemotePrefix, avatarRemoteSuffix, random, user.getUid());
            user.setAvatar(avatar);
            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
            userUpdateWrapper.eq("uid", user.getUid()).set("avatar", avatar);
            userMapper.update(null, userUpdateWrapper);

            /* 变更Redis中的用户数 */
            String userNumberKey = RedisKeyUtils.getUserNumberKey();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(userNumberKey))) {
                redisTemplate.opsForValue().increment(userNumberKey);
            } else redisTemplate.opsForValue().set(userNumberKey, userMapper.selectCount(null));

            try {
                ElasticUserUtils.insertUserToEs(elasticsearchClient, user);
            } catch (IOException e) {
                return Response.success(200, "注册成功");
            }
            return Response.success(200, "注册成功");
        }

        /* 用户数据和角色数据插入到数据库失败，返回注册错误信息 */
        return Response.error(400, "注册失败");
    }


    /**
     * 用户登录
     */
    @Override
    public Response<Object> login(User user, String code, String codeOwner) {

        Response<Object> captchaCheckResult = checkCaptcha(code, codeOwner);
        if (captchaCheckResult.getCode() != 200) return captchaCheckResult;

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user.getUsername().trim(), user.getPassword().trim());

        // authenticate 会调用 UserDetailsServiceImpl.loadUserByUsername()
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        LoggedUser loggedUser = (LoggedUser) authentication.getPrincipal();
        if (Boolean.TRUE.equals(loggedUser.getUser().getBanned())) {
            QueryWrapper<BannedUser> bannedUserQueryWrapper = new QueryWrapper<>();
            bannedUserQueryWrapper.eq("uid", loggedUser.getUser().getUid());
            BannedUser bannedUser = bannedUserMapper.selectOne(bannedUserQueryWrapper);
            if (ObjectUtil.isNotEmpty(bannedUser.getDeadline())) {
                if (bannedUser.getDeadline().startsWith("2099")) {
                    return Response.error(400, "登录失败，账号已被永久封禁");
                } else {
                    String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    if (now.compareTo(bannedUser.getDeadline()) < 0) {
                        return Response.error(400, "登录失败，账号已被封禁", Map.of("deadline", bannedUser.getDeadline()));
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

        log.info("{} logged in", uid);

        return Response.success(200, "登录成功", map);
    }

    @Override
    public Response<Object> refreshToken(String refreshToken) {
        if (jwtUtils.isTokenValid(refreshToken)) {
            Map<String, Object> map = new HashMap<>();
            Claims claims = jwtUtils.parseTokenToClaims(refreshToken);
            if ((claims.getExpiration().getTime() - DateUtil.date().getTime()) <= 48 * 60 * 60 * 1000L) {
                String newRefreshToken = jwtUtils.createRefreshToken(claims.getSubject());
                map.put("refreshToken", newRefreshToken);
                log.info("{}'s refreshToken recreated.", claims.getSubject());
            }
            String accessToken = jwtUtils.createAccessToken(claims.getSubject());
            map.put("token", accessToken);

            log.info("{}'s token refreshed.", claims.getSubject());

            return Response.success(200, "token refreshed", map);
        } else return Response.error(409, "请重新登录");
    }

    /**
     * 校验用户名唯一性
     *
     * @param username 用户名
     * @return 响应体
     */
    @Override
    public Response<Object> checkUsernameUnique(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        Long result = userMapper.selectCount(queryWrapper);
        if (result.intValue() == 0) {
            return Response.success(200, "校验通过");
        }
        return Response.success(400, "用户名已存在");
    }

    /**
     * 校验邮箱唯一性
     *
     * @param email 邮箱地址
     * @return 响应体
     */
    @Override
    public Response<Object> checkEmailUnique(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        Long result = userMapper.selectCount(queryWrapper);
        if (result.intValue() == 0) {
            return Response.success(200, "校验通过");
        }
        return Response.success(400, "邮箱已存在");
    }

    /**
     * 获取 Hutool 图形验证码
     *
     * @return 图形验证码响应体
     */
    @Override
    public Response<String> getCaptcha() {
        RandomGenerator randomGenerator = new RandomGenerator("abcdefghjkmnpqrstuvwxyz123456789", 4);
        CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(200, 100);
        circleCaptcha.setGenerator(randomGenerator);
        //CircleCaptcha captcha = new CircleCaptcha(200, 100, 4, 20);
        circleCaptcha.createCode();
        String code = circleCaptcha.getCode();
        ;
        String codeImageBase64 = circleCaptcha.getImageBase64Data();

        String uuid = IdUtil.simpleUUID();

        /* 验证码的拥有者，只有该拥有者可以从Redis中取出验证码 */
        String codeOwnerKey = RedisKeyUtils.getCaptchaKey(uuid);

        /* 以拥有者的UUID为key，以验证码值为value，超时时间为三分钟 */
        redisTemplate.opsForValue().set(codeOwnerKey, code, 3, TimeUnit.MINUTES);

        Map<String, String> map = new HashMap<>();
        map.put("codeImage", codeImageBase64);
        map.put("codeOwner", uuid);

        /* 响应给前端200状态码、验证码图片的base64码和验证码拥有者唯一ID */
        return Response.success(200, map);
    }

    /**
     * 校验图形验证码
     *
     * @param code      图形验证码
     * @param codeOwner 图形验证码所属者
     * @return 响应体
     */
    @Override
    public Response<Object> checkCaptcha(String code, String codeOwner) {
        if (StrUtil.isBlank(code)) {
            return Response.error(400, "请输入图形验证码");
        }

        String captchaKey = RedisKeyUtils.getCaptchaKey(codeOwner);
        String trueCode = (String) redisTemplate.opsForValue().get(captchaKey);
        if (StrUtil.isBlank(trueCode)) {
            return Response.error(400, "图形验证码已过期");
        }

        if (!code.equalsIgnoreCase(trueCode)) {
            return Response.error(400, "请输入正确的图形验证码");
        }

        return Response.success(200, "图形验证码校验通过");
    }

    @Override
    public Response<String> getEmailCode(String uid) {
        String emailCode = String.valueOf(RandomUtil.randomInt(111111, 999999));

        String emailCodeRequestTimesKey = RedisKeyUtils.getEmailCodeRequestTimesKey(uid);
        Integer requestTimes = (Integer) redisTemplate.opsForValue().get(emailCodeRequestTimesKey);
        if (ObjectUtil.isNotNull(requestTimes) && requestTimes >= 3) {
            return Response.error(400, "请求过于频繁，请一天后重试");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).select("email");
        User user = userMapper.selectOne(queryWrapper);

        Response<String> sentResult = sendEmailCode(user.getEmail(), emailCode);
        if (sentResult.getCode() == 200) {
            String emailCodeKey = RedisKeyUtils.getEmailCodeKey(uid);
            redisTemplate.opsForValue().set(emailCodeKey, emailCode, 15, TimeUnit.MINUTES);

            Boolean onceSent = redisTemplate.hasKey(emailCodeRequestTimesKey);
            if (Boolean.TRUE.equals(onceSent)) {
                redisTemplate.opsForValue().increment(emailCodeRequestTimesKey);
            } else {
                redisTemplate.opsForValue().set(emailCodeRequestTimesKey, 1, 1, TimeUnit.DAYS);
            }

            return Response.success(200, "已发送验证码");
        } else {
            return sentResult;
        }
    }

    /**
     * 发送验证码到邮箱地址
     *
     * @param to        目标邮箱
     * @param emailCode 验证码
     * @return 响应体
     */
    public Response<String> sendEmailCode(String to, String emailCode) {
        Context context = new Context();
        context.setVariable("emailCode", emailCode);
        String emailContent = templateEngine.process("mail-register-template", context);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(emailContent, true);
            mailSender.send(mimeMessage);
        return Response.success(200, "发送成功");
        } catch (MessagingException e) {
            return Response.success(500, "服务器异常");
        }
    }

    @Override
    @Transactional
    public Response<Object> activateEmail(User user, String emailCode) {
        Long uid = user.getUid();
        String emailCodeKey = RedisKeyUtils.getEmailCodeKey(String.valueOf(uid));
        String realEmailCode = (String) redisTemplate.opsForValue().get(emailCodeKey);

        if (ObjectUtil.isNull(realEmailCode)) {
            return Response.error(400, "验证码已过期");
        };
        if (!realEmailCode.equals(emailCode)) {
            return Response.error(400, "验证码错误");
        }

        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("uid", String.valueOf(uid)).set("email_verified", true);
        int updateResult = userMapper.update(null, userUpdateWrapper);
        if (updateResult > 0) {
            String loggedUserKey = RedisKeyUtils.getLoggedUserKey(String.valueOf(uid));
            LoggedUser loggedUser = (LoggedUser) redisTemplate.opsForValue().get(loggedUserKey);
            if (ObjectUtil.isNotNull(loggedUser)) {
                Role role = loggedUser.getUser().getRole();
                if (role.getRid() != 1 || role.getRid() != 2) {
                    role.setRid(3);
                    userMapper.updateUserRole(uid, 3);
                }
                loggedUser.getUser().setRole(role);
                loggedUser.getUser().setEmailVerified(true);
                redisTemplate.opsForValue().set(loggedUserKey, loggedUser);
            }

            try {
                user.setEmailVerified(true);
                ElasticUserUtils.updateUserEmailVerifiedInEs(elasticsearchClient, user);
            } catch (IOException e) {
                return Response.success(200, "邮箱已认证激活");
            }
            return Response.success(200, "邮箱已认证激活");
        } else return Response.error(400, "出现错误，请稍后重试");
    }
}
