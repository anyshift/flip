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
public class BannedHistory {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 封禁用户的UID
     */
    private Long uid;

    /**
     * 封禁起始时间
     */
    private String createTime;

    /**
     * 解封截止时间
     */
    private String deadline;

    /**
     * 封禁理由
     */
    private String reason;

}
