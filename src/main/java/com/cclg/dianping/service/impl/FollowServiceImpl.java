package com.cclg.dianping.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.constant.FollowConstants;
import com.cclg.dianping.domain.Follow;
import com.cclg.dianping.domain.User;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.dto.UserDTO;
import com.cclg.dianping.mapper.FollowMapper;
import com.cclg.dianping.service.IFollowService;
import com.cclg.dianping.service.IUserService;
import com.cclg.dianping.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关注服务实现类
 * 实现关注相关的业务逻辑
 *
 * @author system
 */
@Slf4j
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private IUserService userService;

    /**
     * 关注用户
     *
     * @param followUserId 被关注用户ID
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result follow(Long followUserId) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.fail(FollowConstants.USER_NOT_EXIST);
        }

        Long userId = currentUser.getId();

        if (followUserId == null) {
            return Result.fail(FollowConstants.FOLLOW_USER_ID_NOT_NULL);
        }

        if (userId.equals(followUserId)) {
            return Result.fail(FollowConstants.CANNOT_FOLLOW_SELF);
        }

        User targetUser = userService.getById(followUserId);
        if (targetUser == null) {
            return Result.fail(FollowConstants.USER_NOT_EXIST);
        }

        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, followUserId);
        Follow existFollow = getOne(queryWrapper);

        if (existFollow != null) {
            return Result.fail(FollowConstants.ALREADY_FOLLOWED);
        }

        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setFollowUserId(followUserId);
        follow.setCreateTime(LocalDateTime.now());

        boolean success = save(follow);
        if (success) {
            log.info(FollowConstants.FOLLOW_CREATE_SUCCESS, userId, followUserId);
            return Result.ok(FollowConstants.FOLLOW_SUCCESS);
        }

        return Result.fail(FollowConstants.FOLLOW_FAIL);
    }

    /**
     * 取消关注
     *
     * @param followUserId 被关注用户ID
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result unfollow(Long followUserId) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.fail(FollowConstants.USER_NOT_EXIST);
        }

        Long userId = currentUser.getId();

        if (followUserId == null) {
            return Result.fail(FollowConstants.FOLLOW_USER_ID_NOT_NULL);
        }

        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, followUserId);
        Follow existFollow = getOne(queryWrapper);

        if (existFollow == null) {
            return Result.fail(FollowConstants.NOT_FOLLOWED);
        }

        boolean success = remove(queryWrapper);
        if (success) {
            log.info(FollowConstants.FOLLOW_DELETE_SUCCESS, userId, followUserId);
            return Result.ok(FollowConstants.UNFOLLOW_SUCCESS);
        }

        return Result.fail(FollowConstants.UNFOLLOW_FAIL);
    }

    /**
     * 查询是否关注了某个用户
     *
     * @param followUserId 被关注用户ID
     * @return 操作结果，返回是否关注
     */
    @Override
    public Result isFollow(Long followUserId) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.ok(false);
        }

        if (followUserId == null) {
            return Result.ok(false);
        }

        Long userId = currentUser.getId();

        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, followUserId);
        Follow existFollow = getOne(queryWrapper);

        return Result.ok(existFollow != null);
    }

    /**
     * 查询用户的关注数量
     *
     * @param userId 用户ID
     * @return 操作结果，返回关注数量
     */
    @Override
    public Result getFollowCount(Long userId) {
        if (userId == null) {
            return Result.ok(0);
        }

        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, userId);
        long count = count(queryWrapper);

        return Result.ok(count);
    }

    /**
     * 查询用户的粉丝数量
     *
     * @param userId 用户ID
     * @return 操作结果，返回粉丝数量
     */
    @Override
    public Result getFansCount(Long userId) {
        if (userId == null) {
            return Result.ok(0);
        }

        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getFollowUserId, userId);
        long count = count(queryWrapper);

        return Result.ok(count);
    }

    /**
     * 查询共同关注
     * 查询当前登录用户和目标用户的共同关注列表
     *
     * @param targetUserId 目标用户ID
     * @return 操作结果，返回共同关注用户列表
     */
    @Override
    public Result getCommonFollow(Long targetUserId) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.ok(Collections.emptyList());
        }

        if (targetUserId == null) {
            return Result.ok(Collections.emptyList());
        }

        Long userId = currentUser.getId();

        List<Long> commonFollowIds = baseMapper.selectCommonFollowUserIds(userId, targetUserId);

        if (CollUtil.isEmpty(commonFollowIds)) {
            return Result.ok(Collections.emptyList());
        }

        List<User> users = userService.listByIds(commonFollowIds);
        List<Map<String, Object>> userInfoList = users.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("nickName", user.getNickName());
                    map.put("icon", user.getIcon());
                    return map;
                })
                .collect(Collectors.toList());

        return Result.ok(userInfoList);
    }

    /**
     * 分页查询用户的关注列表
     *
     * @param userId  用户ID
     * @param current 当前页码
     * @param size    每页大小
     * @return 操作结果，返回关注用户列表
     */
    @Override
    public Result getFollowList(Long userId, Integer current, Integer size) {
        if (userId == null) {
            return Result.ok(Collections.emptyList(), 0L);
        }

        if (current == null || current <= 0) {
            current = FollowConstants.DEFAULT_PAGE_CURRENT;
        }

        if (size == null || size <= 0) {
            size = FollowConstants.DEFAULT_PAGE_SIZE;
        } else if (size > FollowConstants.MAX_PAGE_SIZE) {
            size = FollowConstants.MAX_PAGE_SIZE;
        }

        Page<Follow> page = new Page<>(current, size);
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, userId)
                .orderByDesc(Follow::getCreateTime);

        page(page, queryWrapper);

        List<Follow> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return Result.ok(Collections.emptyList(), page.getTotal());
        }

        List<Long> followUserIds = records.stream()
                .map(Follow::getFollowUserId)
                .collect(Collectors.toList());

        List<User> users = userService.listByIds(followUserIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<Map<String, Object>> userInfoList = records.stream()
                .map(follow -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", follow.getFollowUserId());
                    User user = userMap.get(follow.getFollowUserId());
                    if (user != null) {
                        map.put("nickName", user.getNickName());
                        map.put("icon", user.getIcon());
                    }
                    map.put("followTime", follow.getCreateTime());
                    return map;
                })
                .collect(Collectors.toList());

        return Result.ok(userInfoList, page.getTotal());
    }

    /**
     * 分页查询用户的粉丝列表
     *
     * @param userId  用户ID
     * @param current 当前页码
     * @param size    每页大小
     * @return 操作结果，返回粉丝用户列表
     */
    @Override
    public Result getFansList(Long userId, Integer current, Integer size) {
        if (userId == null) {
            return Result.ok(Collections.emptyList(), 0L);
        }

        if (current == null || current <= 0) {
            current = FollowConstants.DEFAULT_PAGE_CURRENT;
        }

        if (size == null || size <= 0) {
            size = FollowConstants.DEFAULT_PAGE_SIZE;
        } else if (size > FollowConstants.MAX_PAGE_SIZE) {
            size = FollowConstants.MAX_PAGE_SIZE;
        }

        Page<Follow> page = new Page<>(current, size);
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getFollowUserId, userId)
                .orderByDesc(Follow::getCreateTime);

        page(page, queryWrapper);

        List<Follow> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return Result.ok(Collections.emptyList(), page.getTotal());
        }

        List<Long> fansUserIds = records.stream()
                .map(Follow::getUserId)
                .collect(Collectors.toList());

        List<User> users = userService.listByIds(fansUserIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<Map<String, Object>> userInfoList = records.stream()
                .map(follow -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", follow.getUserId());
                    User user = userMap.get(follow.getUserId());
                    if (user != null) {
                        map.put("nickName", user.getNickName());
                        map.put("icon", user.getIcon());
                    }
                    map.put("followTime", follow.getCreateTime());
                    return map;
                })
                .collect(Collectors.toList());

        return Result.ok(userInfoList, page.getTotal());
    }
}
