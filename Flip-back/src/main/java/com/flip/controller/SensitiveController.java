package com.flip.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.flip.common.Response;
import com.flip.domain.entity.SensitiveWord;
import com.flip.service.SensitiveWordService;
import com.flip.utils.RedisKeyUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/sys-ctrl")
public class SensitiveController {

    private final String sensitiveObjsKey = RedisKeyUtils.getSensitiveObjsKey();

    @Resource
    private SensitiveWordService sensitiveWordService;

    @Resource
    private RedisTemplate<String, SensitiveWord> redisTemplate;

    @PostMapping("/sensitiveWord")
    public Response<Map<String, SensitiveWord>> addSensitiveWord(@RequestBody SensitiveWord sensitiveWord) {
        boolean saved = sensitiveWordService.save(sensitiveWord);
        if (saved) {
            redisTemplate.opsForList().rightPushIfPresent(sensitiveObjsKey, sensitiveWord);
            return Response.success("新增敏感词成功", Map.of("sensitiveWord", sensitiveWord));
        } else return Response.failed("新增敏感词失败");
    }

    @GetMapping("/sensitiveWord")
    public Response<Map<String, List<SensitiveWord>>> getSensitiveWords() {
        List<SensitiveWord> sensitiveWords = sensitiveWordService.getSensitiveWords();
        return Response.success("获取敏感词成功", Map.of("sensitiveWords", sensitiveWords));
    }

    @PutMapping("/sensitiveWord")
    public Response<Map<String, SensitiveWord>> updateSensitiveWord(@RequestBody SensitiveWord sensitiveWord) {
        QueryWrapper<SensitiveWord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", sensitiveWord.getId());
        if (!sensitiveWordService.exists(queryWrapper)) {
            return Response.failed("该敏感词不存在");
        }

        boolean updated = sensitiveWordService.updateById(sensitiveWord);
        if (updated) {
            redisTemplate.opsForList().set(sensitiveObjsKey, sensitiveWord.getId() - 1, sensitiveWord);
            return Response.success("更新敏感词成功", Map.of("sensitiveWord", sensitiveWord));
        } else return Response.failed("更新敏感词失败");
    }
}
