package com.flip.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@TableName("banned")
public class BannedUser {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 被封禁的用户UID
     */
    private Long uid;

    /**
     * 解封截止时间
     */
    private String deadline;

    /**
     * 封禁理由
     */
    private String reason;

}
