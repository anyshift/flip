package com.flip.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BannedHistory {
    private Integer id;

    private Long uid;

    private String createTime;

    private String deadline;

    private String reason;
}
