package com.cclg.dianping.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.constant.UserConstants;
import com.cclg.dianping.domain.User;
import com.cclg.dianping.dto.LoginFormDTO;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.dto.UserDTO;
import com.cclg.dianping.mapper.UserMapper;
import com.cclg.dianping.service.IUserService;
import com.cclg.dianping.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.cclg.dianping.utils.RedisConstants.LOGIN_CODE_KEY;
import static com.cclg.dianping.utils.RedisConstants.LOGIN_CODE_TTL;
import static com.cclg.dianping.utils.RedisConstants.LOGIN_USER_KEY;
import static com.cclg.dianping.utils.RedisConstants.LOGIN_USER_TTL;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone) {
        String code = RandomUtil.randomNumbers(6);
        String key = LOGIN_CODE_KEY + phone;
        stringRedisTemplate.opsForValue().set(
                key,
                code,
                LOGIN_CODE_TTL,
                TimeUnit.MINUTES
        );
        log.debug("发送短信验证码成功，手机号：{}，验证码：{}", phone, code);
        return Result.ok(code);
    }

    @Override
    public Result login(LoginFormDTO loginForm) {
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();
        String password = loginForm.getPassword();

        User user;

        if (StrUtil.isNotBlank(code)) {
            String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
            if (cacheCode == null || !cacheCode.equals(code)) {
                return Result.fail(UserConstants.CODE_INVALID);
            }
            user = query().eq("phone", phone).one();
            if (user == null) {
                user = createUserWithPhone(phone);
            }
        } else if (StrUtil.isNotBlank(password)) {
            user = query().eq("phone", phone).one();
            if (user == null) {
                return Result.fail("手机号或密码错误");
            }
            String encryptedPassword = DigestUtil.md5Hex(password);
            if (!encryptedPassword.equals(user.getPassword())) {
                return Result.fail("手机号或密码错误");
            }
        } else {
            return Result.fail("请提供验证码或密码");
        }

        String token = UUID.randomUUID().toString(true);

        Map<String, Object> userMap = BeanUtil.beanToMap(
                user,
                new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString())
        );

        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.HOURS);

        log.info(UserConstants.LOGIN_SUCCESS + "，用户ID：{}", user.getId());
        return Result.ok(token);
    }

    @Override
    public Result logout(String token) {
        if (StrUtil.isNotBlank(token)) {
            String tokenKey = LOGIN_USER_KEY + token;
            stringRedisTemplate.delete(tokenKey);
        }
        UserDTO user = UserHolder.getUser();
        if (user != null) {
            log.info(UserConstants.LOGOUT_SUCCESS + "，用户ID：{}", user.getId());
        }
        return Result.ok();
    }

    @Override
    public Result getCurrentUser() {
        UserDTO userDTO = UserHolder.getUser();
        if (userDTO == null) {
            return Result.fail(UserConstants.USER_NOT_LOGIN);
        }
        return Result.ok(userDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveUser(User user) {
        if (user == null) {
            return Result.fail(UserConstants.USER_INFO_NOT_NULL);
        }

        if (StrUtil.isBlank(user.getPhone())) {
            return Result.fail(UserConstants.USER_PHONE_NOT_NULL);
        }

        if (checkPhoneExist(user.getPhone(), null)) {
            return Result.fail(UserConstants.USER_PHONE_EXIST);
        }

        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(DigestUtil.md5Hex(user.getPassword()));
        }

        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        if (StrUtil.isBlank(user.getNickName())) {
            user.setNickName("user_" + RandomUtil.randomString(10));
        }

        boolean success = save(user);
        if (success) {
            log.info(UserConstants.USER_CREATE_SUCCESS, user.getId());
            return Result.ok(user.getId());
        }

        return Result.fail(UserConstants.USER_CREATE_FAIL);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateUser(User user) {
        if (user == null || user.getId() == null) {
            return Result.fail(UserConstants.USER_ID_NOT_NULL);
        }

        User existUser = getById(user.getId());
        if (existUser == null) {
            return Result.fail(UserConstants.USER_NOT_EXIST);
        }

        if (user.getPhone() != null && StrUtil.isBlank(user.getPhone())) {
            return Result.fail(UserConstants.USER_PHONE_NOT_NULL);
        }

        if (StrUtil.isNotBlank(user.getPhone()) && checkPhoneExist(user.getPhone(), user.getId())) {
            return Result.fail(UserConstants.USER_PHONE_EXIST);
        }

        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(DigestUtil.md5Hex(user.getPassword()));
        }

        user.setUpdateTime(LocalDateTime.now());

        boolean success = updateById(user);
        if (success) {
            log.info(UserConstants.USER_UPDATE_SUCCESS, user.getId());
            return Result.ok();
        }

        return Result.fail(UserConstants.USER_UPDATE_FAIL);
    }

    @Override
    public Result getUserById(Long id) {
        if (id == null) {
            return Result.fail(UserConstants.USER_ID_NOT_NULL);
        }

        User user = getById(id);
        if (user == null) {
            return Result.fail(UserConstants.USER_NOT_EXIST);
        }

        return Result.ok(user);
    }

    @Override
    public Result queryUserPage(Integer current, Integer size, String phone, String nickName) {
        if (current == null || current <= 0) {
            current = UserConstants.DEFAULT_PAGE_CURRENT;
        }

        if (size == null || size <= 0) {
            size = UserConstants.DEFAULT_PAGE_SIZE;
        } else if (size > UserConstants.MAX_PAGE_SIZE) {
            size = UserConstants.MAX_PAGE_SIZE;
        }

        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(phone)) {
            queryWrapper.like(User::getPhone, phone);
        }

        if (StrUtil.isNotBlank(nickName)) {
            queryWrapper.like(User::getNickName, nickName);
        }

        queryWrapper.orderByDesc(User::getUpdateTime);

        page(page, queryWrapper);

        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteUserById(Long id) {
        if (id == null) {
            return Result.fail(UserConstants.USER_ID_NOT_NULL);
        }

        User user = getById(id);
        if (user == null) {
            return Result.fail(UserConstants.USER_NOT_EXIST);
        }

        boolean success = removeById(id);
        if (success) {
            log.info(UserConstants.USER_DELETE_SUCCESS, id);
            return Result.ok();
        }

        return Result.fail(UserConstants.USER_DELETE_FAIL);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteUserByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail(UserConstants.USER_ID_LIST_NOT_NULL);
        }

        List<User> existUsers = listByIds(ids);
        Set<Long> existIds = existUsers.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        Optional<Long> nonExistId = ids.stream()
                .filter(id -> !existIds.contains(id))
                .findFirst();
        if (nonExistId.isPresent()) {
            return Result.fail(UserConstants.USER_NOT_EXIST + "，用户ID：" + nonExistId.get());
        }

        boolean success = removeByIds(ids);
        if (success) {
            log.info(UserConstants.USER_BATCH_DELETE_SUCCESS, ids.size());
            return Result.ok();
        }

        return Result.fail(UserConstants.USER_BATCH_DELETE_FAIL);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("user_" + RandomUtil.randomString(10));
        save(user);
        return user;
    }

    private boolean checkPhoneExist(String phone, Long excludeId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);

        if (excludeId != null) {
            queryWrapper.ne(User::getId, excludeId);
        }

        return count(queryWrapper) > 0;
    }
}
