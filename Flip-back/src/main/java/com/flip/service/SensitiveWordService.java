package com.flip.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.flip.domain.entity.SensitiveWord;

import java.util.List;

public interface SensitiveWordService extends IService<SensitiveWord> {

    List<SensitiveWord> getSensitiveWords();

    List<String> getSensitiveStringWords();

}
