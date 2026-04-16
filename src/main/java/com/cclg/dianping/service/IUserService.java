package com.cclg.dianping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cclg.dianping.domain.User;
import com.cclg.dianping.dto.LoginFormDTO;
import com.cclg.dianping.dto.Result;

public interface IUserService extends IService<User> {
    
    Result sendCode(String phone);
    
    Result login(LoginFormDTO loginForm);
}
