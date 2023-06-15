package com.flip.controller.admin;

import com.flip.domain.Response;
import com.flip.mapper.PostMapper;
import com.flip.mapper.UserMapper;
import com.flip.utils.RedisKeyUtils;
import com.flip.utils.SystemUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class StatusController {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PostMapper postMapper;

    @GetMapping("/systemInfo")
    public Response<Object> getSystemInfo() {
        Map<String, String> memory = SystemUtils.getMemory();
        Map<String, Object> processor = SystemUtils.getProcessor();
        Map<String, Integer> forum = getForumInfo().getData();

        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("memory", memory);
        systemInfo.put("cpu", processor);
        systemInfo.put("forum", forum);
        return Response.success(200, systemInfo);
    }

    @GetMapping("/forumInfo")
    public Response<Integer> getForumInfo() {
        String userNumberKey = RedisKeyUtils.getUserNumberKey();
        String postNumberKey = RedisKeyUtils.getPostNumberKey();
        if (Boolean.FALSE.equals(redisTemplate.hasKey(userNumberKey))) {
            Long userNumber = userMapper.selectCount(null);
            redisTemplate.opsForValue().set(userNumberKey, userNumber);
        }
        if (Boolean.FALSE.equals(redisTemplate.hasKey(postNumberKey))) {
            Long postNumber = postMapper.selectCount(null);
            redisTemplate.opsForValue().set(postNumberKey, postNumber);
        }

        Map<String, Integer> map = new HashMap<>();
        map.put("userNumber", (Integer) redisTemplate.opsForValue().get(userNumberKey));
        map.put("postNumber", (Integer) redisTemplate.opsForValue().get(postNumberKey));
        return Response.success(200, map);
    }
}
