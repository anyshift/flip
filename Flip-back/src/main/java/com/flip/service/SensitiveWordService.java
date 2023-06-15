package com.flip.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.flip.domain.Response;
import com.flip.entity.SensitiveWord;

import java.util.List;

public interface SensitiveWordService extends IService<SensitiveWord> {
    Response<SensitiveWord> addSensitiveWord(SensitiveWord sensitiveWord);

    Response<List<SensitiveWord>> getSensitiveWords();

    List<String> getSensitiveStringWords();

    Response<SensitiveWord> updateSensitiveWord(SensitiveWord sensitiveWord);
}
