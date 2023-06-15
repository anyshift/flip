package com.flip.controller.admin;

import com.flip.domain.Response;
import com.flip.entity.SensitiveWord;
import com.flip.service.SensitiveWordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sys-ctrl")
public class SensitiveController {
    @Resource
    private SensitiveWordService sensitiveWordService;

    @PostMapping("/sensitiveWord")
    public Response<SensitiveWord> addSensitiveWord(@RequestBody SensitiveWord sensitiveWord) {
        return sensitiveWordService.addSensitiveWord(sensitiveWord);
    }

    @GetMapping("/sensitiveWord")
    public Response<List<SensitiveWord>> getSensitiveWords() {
        return sensitiveWordService.getSensitiveWords();
    }

    @PutMapping("/sensitiveWord")
    public Response<SensitiveWord> updateSensitiveWord(@RequestBody SensitiveWord sensitiveWord) {
        return sensitiveWordService.updateSensitiveWord(sensitiveWord);
    }
}
