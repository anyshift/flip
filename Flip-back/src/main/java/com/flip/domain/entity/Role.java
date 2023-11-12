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
public class Role {

    @TableId(type = IdType.AUTO)
    private Integer rid;

    /**
     * 角色名
     */
    private String name;

    /**
     * 角色别名
     */
    private String alias;

    /**
     * 角色排序索引，越小越靠前
     */
    private Integer index;

}
