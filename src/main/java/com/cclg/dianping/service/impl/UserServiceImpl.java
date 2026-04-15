package com.cclg.dianping.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.domain.User;
import com.cclg.dianping.mapper.UserMapper;
import com.cclg.dianping.service.IUserService;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
