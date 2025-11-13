package org.lyf.testapi.dto;

import lombok.Data;

/**
 * 挑战数据DTO
 */
@Data
public class Challenge {
    /**
     * 挑战ID (1-5)
     */
    private Integer id;

    /**
     * 是否已完成
     */
    private Boolean completed;
}

