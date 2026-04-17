package com.cclg.dianping.controller;

import com.cclg.dianping.constant.UserConstants;
import com.cclg.dianping.domain.User;
import com.cclg.dianping.dto.LoginFormDTO;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.cclg.dianping.utils.RegexUtils.isPhoneInvalid;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone) {
        if (isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        return userService.sendCode(phone);
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm){
        log.info(UserConstants.LOG_LOGIN, loginForm.getPhone());
        String phone = loginForm.getPhone();
        if (isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        return userService.login(loginForm);
    }

    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        log.info(UserConstants.LOG_LOGOUT);
        String token = request.getHeader("authorization");
        return userService.logout(token);
    }

    @GetMapping("/me")
    public Result getCurrentUser() {
        log.info(UserConstants.LOG_GET_CURRENT_USER);
        return userService.getCurrentUser();
    }

    @PostMapping
    public Result saveUser(@RequestBody User user) {
        log.info(UserConstants.LOG_SAVE_USER, user.getPhone());
        return userService.saveUser(user);
    }

    @PutMapping
    public Result updateUser(@RequestBody User user) {
        log.info(UserConstants.LOG_UPDATE_USER, user.getId());
        return userService.updateUser(user);
    }

    @GetMapping("/{id}")
    public Result getUserById(@PathVariable("id") Long id) {
        log.info(UserConstants.LOG_GET_USER, id);
        return userService.getUserById(id);
    }

    @GetMapping("/page")
    public Result queryUserPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "nickName", required = false) String nickName) {
        log.info(UserConstants.LOG_PAGE_QUERY, current, size, phone, nickName);
        return userService.queryUserPage(current, size, phone, nickName);
    }

    @DeleteMapping("/{id}")
    public Result deleteUserById(@PathVariable("id") Long id) {
        log.info(UserConstants.LOG_DELETE_USER, id);
        return userService.deleteUserById(id);
    }

    @DeleteMapping("/batch")
    public Result deleteUserByIds(@RequestBody List<Long> ids) {
        log.info(UserConstants.LOG_BATCH_DELETE_USER, ids.size());
        return userService.deleteUserByIds(ids);
    }

}
