package com.flip.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWord {

    @TableId(type = IdType.AUTO)
    private Integer Id;

    /**
     * 敏感词
     */
    private String word;

}
