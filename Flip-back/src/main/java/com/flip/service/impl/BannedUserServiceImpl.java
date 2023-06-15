package com.flip.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flip.domain.Response;
import com.flip.entity.User;
import com.flip.entity.vo.BannedUser;
import com.flip.mapper.BannedUserMapper;
import com.flip.mapper.UserMapper;
import com.flip.service.BannedUserService;
import com.flip.utils.elastic.ElasticUserUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BannedUserServiceImpl extends ServiceImpl<BannedUserMapper, BannedUser> implements BannedUserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private BannedUserMapper bannedUserMapper;

    @Override
    @Transactional
    public Response<Object> banUser(BannedUser bannedUser) {
        int insertResult = bannedUserMapper.insert(bannedUser);
        if (insertResult > 0) {
            Long uid = bannedUser.getUid();
            String reason = bannedUser.getReason();
            String deadline = bannedUser.getDeadline();
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            bannedUserMapper.insertBannedHistory(uid, now, deadline, reason);

            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
            userUpdateWrapper.eq("uid", uid).set("banned", true);
            userMapper.update(null, userUpdateWrapper);

            User user = new User();
            user.setUid(uid);
            user.setBanned(true);
            try {
                ElasticUserUtils.updateUserBannedStatusInEs(elasticsearchClient, user);
            } catch (IOException e) {
                return Response.success(200, "封禁成功");
            }
            return Response.success(200, "封禁成功");
        } else return Response.error(400, "封禁失败");
    }

    @Override
    public Response<Object> cancelBanUser(String uid) {
        QueryWrapper<BannedUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);

        int deleteResult = bannedUserMapper.delete(queryWrapper);
        if (deleteResult > 0) {
            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
            userUpdateWrapper.eq("uid", uid).set("banned", false);
            userMapper.update(null, userUpdateWrapper);

            User user = new User();
            user.setUid(Long.valueOf(uid));
            user.setBanned(false);
            try {
                ElasticUserUtils.updateUserBannedStatusInEs(elasticsearchClient, user);
            } catch (IOException e) {
                return Response.success(200, "解除封禁成功");
            }
            return Response.success(200, "解除封禁成功");
        } else return Response.error(400, "解除封禁失败");
    }
}
