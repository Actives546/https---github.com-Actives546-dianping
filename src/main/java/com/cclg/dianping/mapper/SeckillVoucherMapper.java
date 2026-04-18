package com.cclg.dianping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cclg.dianping.domain.SeckillVoucher;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀券Mapper接口
 * 继承BaseMapper，提供基本的增删改查功能
 *
 * @author system
 */
@Mapper
public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucher> {

}
