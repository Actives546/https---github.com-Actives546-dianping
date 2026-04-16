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
        // 生成6位随机数字验证码
        String code = RandomUtil.randomNumbers(6);
        // 将验证码存入Redis，key为login:code:{phone}
        stringRedisTemplate.opsForValue().set(
                LOGIN_CODE_KEY + phone,
                code,
                LOGIN_CODE_TTL,
                TimeUnit.MINUTES
        );
        // 日志记录发送的验证码
        log.debug("发送短信验证码成功，验证码：{}", code);
        // 返回验证码给前端
        return Result.ok(code);
    }

    @Override
    public Result login(LoginFormDTO loginForm) {
        // 从登录表单中获取手机号
        String phone = loginForm.getPhone();
        // 从Redis中获取该手机号对应的验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        // 从登录表单中获取用户输入的验证码
        String code = loginForm.getCode();
        // 校验验证码是否正确
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 验证码错误，返回错误信息
            return Result.fail("验证码错误");
        }
        // 根据手机号查询用户信息
        User user = query().eq("phone", phone).one();
        // 判断用户是否存在
        if (user == null) {
            // 用户不存在，创建新用户
            user = createUserWithPhone(phone);
        }
        // 生成唯一的UUID作为登录token
        String token = UUID.randomUUID().toString(true);
        // 将用户对象转换为Map，以便存入Redis的Hash结构
        Map<String, Object> userMap = BeanUtil.beanToMap(user, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 拼接token在Redis中的key，格式为login:token:{token}
        String tokenKey = LOGIN_USER_KEY + token;
        // 将用户信息存入Redis的Hash结构
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 设置token的过期时间
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 返回token给前端
        return Result.ok(token);
    }

    /**
     * 根据手机号创建新用户
     * @param phone 手机号
     * @return 创建的用户对象
     */
    private User createUserWithPhone(String phone) {
        // 创建新用户对象
        User user = new User();
        // 设置用户手机号
        user.setPhone(phone);
        // 设置随机昵称，格式为user_ + 10位随机字符串
        user.setNickName("user_" + RandomUtil.randomString(10));
        // 将用户保存到数据库
        save(user);
        // 返回创建的用户对象
        return user;
    }
}
