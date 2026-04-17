package com.cclg.dianping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cclg.dianping.domain.User;
import com.cclg.dianping.dto.LoginFormDTO;
import com.cclg.dianping.dto.Result;

import java.util.List;

public interface IUserService extends IService<User> {
    
    Result sendCode(String phone);
    
    Result login(LoginFormDTO loginForm);
    
    Result logout(String token);
    
    Result getCurrentUser();
    
    Result saveUser(User user);
    
    Result updateUser(User user);
    
    Result getUserById(Long id);
    
    Result queryUserPage(Integer current, Integer size, String phone, String nickName);
    
    Result deleteUserById(Long id);
    
    Result deleteUserByIds(List<Long> ids);
}
