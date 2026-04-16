package com.cclg.dianping.controller;

import com.cclg.dianping.dto.LoginFormDTO;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import static com.cclg.dianping.utils.RegexUtils.isPhoneInvalid;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    /**
     * 发送手机验证码
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        if (isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        return userService.sendCode(phone, session);
    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        String phone = loginForm.getPhone();
        if (isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        return userService.login(loginForm, session);
    }

}
