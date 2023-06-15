package com.flip.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flip.domain.Response;
import com.flip.entity.SensitiveWord;
import com.flip.mapper.SensitiveWordMapper;
import com.flip.service.SensitiveWordService;
import com.flip.utils.RedisKeyUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord> implements SensitiveWordService {

    private final String sensitiveObjsKey = RedisKeyUtils.getSensitiveObjsKey();

    private final String sensitiveWordsKey = RedisKeyUtils.getSensitiveWordsKey();

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    @Resource
    private RedisTemplate<String, SensitiveWord> redisTemplate;

    @Resource
    private RedisTemplate<String, String> stringRedisTemplate;

    @Override
    public Response<SensitiveWord> addSensitiveWord(SensitiveWord sensitiveWord) {
        int insertResult = sensitiveWordMapper.insert(sensitiveWord);
        if (insertResult > 0) {
            redisTemplate.opsForList().rightPushIfPresent(sensitiveObjsKey, sensitiveWord);
            return Response.success(200, "新增敏感词成功", Map.of("sensitiveWord", sensitiveWord));
        } else return Response.error(400, "新增敏感词失败");
    }

    @Override
    public Response<List<SensitiveWord>> getSensitiveWords() {
        List<SensitiveWord> sensitiveWords;

        Boolean hasKey = redisTemplate.hasKey(sensitiveObjsKey);
        if (Boolean.TRUE.equals(hasKey)) {
            sensitiveWords = redisTemplate.opsForList().range(sensitiveObjsKey, 0, -1);
            if (ObjectUtil.isNotNull(sensitiveWords)) {
                return Response.success(200, Map.of("sensitiveWords", sensitiveWords));
            }
        }

        sensitiveWords = sensitiveWordMapper.selectList(null);
        sensitiveWords.forEach(sensitiveWord -> {
            redisTemplate.opsForList().rightPush(sensitiveObjsKey, sensitiveWord);
        });

        return Response.success(200, Map.of("sensitiveWords", sensitiveWords));
    }

    public List<String> getSensitiveStringWords() {
        List<String> sensitiveWords;
        Boolean hasKey = stringRedisTemplate.hasKey(sensitiveWordsKey);
        if (Boolean.TRUE.equals(hasKey)) {
            sensitiveWords = stringRedisTemplate.opsForList().range(sensitiveWordsKey, 0, -1);
            if (ObjectUtil.isNotNull(sensitiveWords)) {
                return sensitiveWords;
            }
        }

        List<SensitiveWord> tempSensitiveWords = getSensitiveWords().getData().get("sensitiveWords");
        tempSensitiveWords.forEach(sensitiveWord -> {
            stringRedisTemplate.opsForList().rightPush(sensitiveWordsKey, sensitiveWord.getWord());
        });
        sensitiveWords = stringRedisTemplate.opsForList().range(sensitiveWordsKey, 0, -1);

        return sensitiveWords;
    }

    @Override
    public Response<SensitiveWord> updateSensitiveWord(SensitiveWord sensitiveWord) {
        QueryWrapper<SensitiveWord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", sensitiveWord.getId());
        if (!sensitiveWordMapper.exists(queryWrapper)) {
            return Response.error(400, "该敏感词不存在");
        }

        int updateResult = sensitiveWordMapper.updateById(sensitiveWord);
        if (updateResult > 0) {
            redisTemplate.opsForList().set(sensitiveObjsKey, sensitiveWord.getId() - 1, sensitiveWord);
            return Response.success(200, "更新敏感词成功", Map.of("sensitiveWord", sensitiveWord));
        } else return Response.error(400, "更新敏感词失败");
    }
}
