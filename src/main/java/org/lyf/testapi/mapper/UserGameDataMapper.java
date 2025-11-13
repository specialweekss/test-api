package org.lyf.testapi.mapper;

import org.apache.ibatis.annotations.Param;
import org.lyf.testapi.entity.UserGameData;

/**
 * 用户游戏数据Mapper接口
 */
public interface UserGameDataMapper {
    /**
     * 根据用户ID查询游戏数据
     *
     * @param userId 用户ID
     * @return 用户游戏数据
     */
    UserGameData selectByUserId(@Param("userId") String userId);

    /**
     * 插入用户游戏数据
     *
     * @param userGameData 用户游戏数据
     * @return 影响行数
     */
    int insert(UserGameData userGameData);

    /**
     * 更新用户游戏数据
     *
     * @param userGameData 用户游戏数据
     * @return 影响行数
     */
    int updateByUserId(UserGameData userGameData);
}

