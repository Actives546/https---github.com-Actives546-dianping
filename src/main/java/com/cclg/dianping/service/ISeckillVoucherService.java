package com.cclg.dianping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cclg.dianping.domain.SeckillVoucher;

/**
 * 秒杀券服务接口
 * 继承IService，提供基础的增删改查功能
 * 供VoucherService内部调用，处理秒杀券相关操作
 *
 * @author system
 */
public interface ISeckillVoucherService extends IService<SeckillVoucher> {

}
