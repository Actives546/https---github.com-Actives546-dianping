package com.cclg.dianping.controller;

import com.cclg.dianping.constant.FollowConstants;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.service.IFollowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 关注管理控制器
 * 提供关注、取消关注、粉丝统计、共同关注等接口
 *
 * @author system
 */
@Slf4j
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;

    /**
     * 关注用户
     *
     * @param followUserId 被关注用户ID
     * @return 操作结果
     */
    @PostMapping("/{followUserId}")
    public Result follow(@PathVariable("followUserId") Long followUserId) {
        log.info(FollowConstants.LOG_FOLLOW, followUserId);
        return followService.follow(followUserId);
    }

    /**
     * 取消关注
     *
     * @param followUserId 被关注用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{followUserId}")
    public Result unfollow(@PathVariable("followUserId") Long followUserId) {
        log.info(FollowConstants.LOG_UNFOLLOW, followUserId);
        return followService.unfollow(followUserId);
    }

    /**
     * 查询是否关注了某个用户
     *
     * @param followUserId 被关注用户ID
     * @return 操作结果，返回是否关注
     */
    @GetMapping("/or/not/{followUserId}")
    public Result isFollow(@PathVariable("followUserId") Long followUserId) {
        log.info(FollowConstants.LOG_IS_FOLLOW, followUserId);
        return followService.isFollow(followUserId);
    }

    /**
     * 查询用户的关注数量
     *
     * @param userId 用户ID
     * @return 操作结果，返回关注数量
     */
    @GetMapping("/count/follow/{userId}")
    public Result getFollowCount(@PathVariable("userId") Long userId) {
        log.info(FollowConstants.LOG_FOLLOW_COUNT, userId);
        return followService.getFollowCount(userId);
    }

    /**
     * 查询用户的粉丝数量
     *
     * @param userId 用户ID
     * @return 操作结果，返回粉丝数量
     */
    @GetMapping("/count/fans/{userId}")
    public Result getFansCount(@PathVariable("userId") Long userId) {
        log.info(FollowConstants.LOG_FANS_COUNT, userId);
        return followService.getFansCount(userId);
    }

    /**
     * 查询共同关注
     * 查询当前登录用户和目标用户的共同关注列表
     *
     * @param targetUserId 目标用户ID
     * @return 操作结果，返回共同关注用户列表
     */
    @GetMapping("/common/{targetUserId}")
    public Result getCommonFollow(@PathVariable("targetUserId") Long targetUserId) {
        log.info(FollowConstants.LOG_COMMON_FOLLOW, targetUserId);
        return followService.getCommonFollow(targetUserId);
    }

    /**
     * 分页查询用户的关注列表
     *
     * @param userId  用户ID
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10，最大100
     * @return 操作结果，返回关注用户列表
     */
    @GetMapping("/list/follow/{userId}")
    public Result getFollowList(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info(FollowConstants.LOG_FOLLOW_LIST, userId, current, size);
        return followService.getFollowList(userId, current, size);
    }

    /**
     * 分页查询用户的粉丝列表
     *
     * @param userId  用户ID
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10，最大100
     * @return 操作结果，返回粉丝用户列表
     */
    @GetMapping("/list/fans/{userId}")
    public Result getFansList(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info(FollowConstants.LOG_FANS_LIST, userId, current, size);
        return followService.getFansList(userId, current, size);
    }
}
