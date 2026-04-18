package com.cclg.dianping.handler;

import com.cclg.dianping.constant.VoucherConstants;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠券处理器工厂
 * 管理所有优惠券处理器，根据类型获取对应的处理器
 *
 * @author system
 */
@Component
public class VoucherHandlerFactory {

    /**
     * 所有优惠券处理器
     */
    @Resource
    private List<VoucherHandler> voucherHandlers;

    /**
     * 处理器映射表，key为优惠券类型，value为对应的处理器
     */
    private final Map<Integer, VoucherHandler> handlerMap = new HashMap<>();

    /**
     * 初始化处理器映射表
     * 在Spring容器初始化后执行
     */
    @PostConstruct
    public void init() {
        for (VoucherHandler handler : voucherHandlers) {
            handlerMap.put(handler.getType(), handler);
        }
    }

    /**
     * 根据优惠券类型获取对应的处理器
     *
     * @param type 优惠券类型
     * @return 对应的处理器，如果不存在则返回null
     */
    public VoucherHandler getHandler(Integer type) {
        if (type == null) {
            // 默认返回普通券处理器
            return handlerMap.get(VoucherConstants.VOUCHER_TYPE_NORMAL);
        }
        return handlerMap.get(type);
    }

    /**
     * 检查是否支持指定的优惠券类型
     *
     * @param type 优惠券类型
     * @return true-支持，false-不支持
     */
    public boolean supports(Integer type) {
        if (type == null) {
            return true;
        }
        return handlerMap.containsKey(type);
    }
}
