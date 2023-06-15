package com.flip.entity;

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
public class Role {
    @TableId(type = IdType.AUTO)
    private Integer rid;

    private String name;

    private String alias;

    private Integer index; //排序号，越小越靠前
}
