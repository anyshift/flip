package com.flip.entity.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@TableName("banned")
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BannedUser {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long uid;

    private String deadline;

    private String reason;
}
