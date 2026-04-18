package com.cclg.dianping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cclg.dianping.domain.Follow;
import com.cclg.dianping.dto.Result;

/**
 * 关注服务接口
 * 定义关注相关的业务操作方法
 *
 * @author system
 */
public interface IFollowService extends IService<Follow> {

    /**
     * 关注用户
     * 业务逻辑：
     * 1. 校验被关注用户ID不能为空
     * 2. 校验不能关注自己
     * 3. 校验被关注用户是否存在
     * 4. 校验是否已经关注
     * 5. 创建关注关系
     *
     * @param followUserId 被关注用户ID
     * @return 操作结果
     */
    Result follow(Long followUserId);

    /**
     * 取消关注
     * 业务逻辑：
     * 1. 校验被关注用户ID不能为空
     * 2. 校验是否已关注
     * 3. 删除关注关系
     *
     * @param followUserId 被关注用户ID
     * @return 操作结果
     */
    Result unfollow(Long followUserId);

    /**
     * 查询是否关注了某个用户
     *
     * @param followUserId 被关注用户ID
     * @return 操作结果，返回是否关注
     */
    Result isFollow(Long followUserId);

    /**
     * 查询用户的关注数量
     *
     * @param userId 用户ID
     * @return 操作结果，返回关注数量
     */
    Result getFollowCount(Long userId);

    /**
     * 查询用户的粉丝数量
     *
     * @param userId 用户ID
     * @return 操作结果，返回粉丝数量
     */
    Result getFansCount(Long userId);

    /**
     * 查询共同关注
     * 查询当前登录用户和目标用户的共同关注列表
     *
     * @param targetUserId 目标用户ID
     * @return 操作结果，返回共同关注用户列表
     */
    Result getCommonFollow(Long targetUserId);

    /**
     * 分页查询用户的关注列表
     *
     * @param userId  用户ID
     * @param current 当前页码
     * @param size    每页大小
     * @return 操作结果，返回关注用户列表
     */
    Result getFollowList(Long userId, Integer current, Integer size);

    /**
     * 分页查询用户的粉丝列表
     *
     * @param userId  用户ID
     * @param current 当前页码
     * @param size    每页大小
     * @return 操作结果，返回粉丝用户列表
     */
    Result getFansList(Long userId, Integer current, Integer size);
}
