package com.cclg.dianping.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.domain.SeckillVoucher;
import com.cclg.dianping.mapper.SeckillVoucherMapper;
import com.cclg.dianping.service.ISeckillVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 秒杀券服务实现类
 * 继承ServiceImpl，提供基础的增删改查功能
 * 供VoucherService内部调用，处理秒杀券相关操作
 *
 * @author system
 */
@Slf4j
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {

}
