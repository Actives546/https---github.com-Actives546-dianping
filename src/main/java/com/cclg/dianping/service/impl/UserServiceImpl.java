package com.cclg.dianping.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.domain.User;
import com.cclg.dianping.dto.LoginFormDTO;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.mapper.UserMapper;
import com.cclg.dianping.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        // ========== 1. 生成验证码 ==========
        String code = RandomUtil.randomNumbers(6);

        // ========== 2. 保存验证码到Redis ==========
        String key = LOGIN_CODE_KEY + phone;
        stringRedisTemplate.opsForValue().set(
                key,
                code,
                LOGIN_CODE_TTL,
                TimeUnit.MINUTES
        );

        // ========== 3. 记录日志并返回 ==========
        log.debug("发送短信验证码成功，手机号：{}，验证码：{}", phone, code);
        return Result.ok(code);
    }

    @Override
    public Result login(LoginFormDTO loginForm) {
        // ========== 1. 获取参数 ==========
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();

        // ========== 2. 校验验证码 ==========
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.fail("验证码错误");
        }

        // ========== 3. 查询或创建用户 ==========
        User user = query().eq("phone", phone).one();
        if (user == null) {
            user = createUserWithPhone(phone);
        }

        // ========== 4. 生成并保存Token ==========
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
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // ========== 5. 返回Token ==========
        return Result.ok(token);
    }

    /**
     * 根据手机号创建新用户
     *
     * @param phone 手机号
     * @return 创建的用户对象
     */
    private User createUserWithPhone(String phone) {
        // ========== 1. 创建用户对象 ==========
        User user = new User();

        // ========== 2. 设置用户属性 ==========
        user.setPhone(phone);
        user.setNickName("user_" + RandomUtil.randomString(10));

        // ========== 3. 保存并返回 ==========
        save(user);
        return user;
    }
}
