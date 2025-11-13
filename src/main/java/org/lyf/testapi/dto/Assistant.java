package org.lyf.testapi.dto;

import lombok.Data;

/**
 * 助理数据DTO
 */
@Data
public class Assistant {
    /**
     * 助理ID (1-4)
     */
    private Integer id;

    /**
     * 是否已解锁
     */
    private Boolean unlocked;

    /**
     * 当前等级 (0表示未解锁，1-50表示已解锁的等级)
     */
    private Integer level;
}

