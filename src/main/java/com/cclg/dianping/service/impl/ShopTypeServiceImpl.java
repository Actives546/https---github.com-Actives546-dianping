package com.cclg.dianping.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.constant.ShopConstants;
import com.cclg.dianping.constant.ShopTypeConstants;
import com.cclg.dianping.domain.Shop;
import com.cclg.dianping.domain.ShopType;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.mapper.ShopMapper;
import com.cclg.dianping.mapper.ShopTypeMapper;
import com.cclg.dianping.service.IShopTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * 商铺类型服务实现类
 * 实现商铺类型相关的业务逻辑
 *
 * @author system
 */
@Slf4j
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private ShopMapper shopMapper;

    /**
     * 新增商铺类型
     * 业务逻辑：
     * 1. 校验商铺类型信息不能为空
     * 2. 校验商铺类型名称不能为空
     * 3. 校验商铺类型名称是否已存在
     * 4. 设置创建时间和更新时间
     * 5. 设置默认排序值
     * 6. 保存商铺类型信息
     *
     * @param shopType 商铺类型信息
     * @return 操作结果，成功返回商铺类型ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveShopType(ShopType shopType) {
        // ========== 1. 校验商铺类型信息不能为空 ==========
        if (shopType == null) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_INFO_NOT_NULL);
        }

        // ========== 2. 校验商铺类型名称不能为空 ==========
        if (StrUtil.isBlank(shopType.getName())) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NAME_NOT_NULL);
        }

        // ========== 3. 校验商铺类型名称是否已存在 ==========
        if (checkShopTypeNameExist(shopType.getName(), null)) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NAME_EXIST);
        }

        // ========== 4. 设置创建时间和更新时间 ==========
        LocalDateTime now = LocalDateTime.now();
        shopType.setCreateTime(now);
        shopType.setUpdateTime(now);

        // ========== 5. 设置默认排序值 ==========
        if (shopType.getSort() == null) {
            shopType.setSort(0);
        }

        // ========== 6. 保存商铺类型信息 ==========
        boolean success = save(shopType);
        if (success) {
            log.info(ShopTypeConstants.SHOP_TYPE_CREATE_SUCCESS, shopType.getId());
            return Result.ok(shopType.getId());
        }

        return Result.fail(ShopTypeConstants.SHOP_TYPE_CREATE_FAIL);
    }

    /**
     * 更新商铺类型
     * 业务逻辑：
     * 1. 校验商铺类型ID不能为空
     * 2. 校验商铺类型是否存在
     * 3. 校验商铺类型名称不能是空串（如果传入了名称）
     * 4. 校验商铺类型名称是否已存在（排除自身）
     * 5. 设置更新时间
     * 6. 更新商铺类型信息
     *
     * @param shopType 商铺类型信息
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateShopType(ShopType shopType) {
        // ========== 1. 校验商铺类型ID不能为空 ==========
        if (shopType == null || shopType.getId() == null) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_ID_NOT_NULL);
        }

        // ========== 2. 校验商铺类型是否存在 ==========
        ShopType existShopType = getById(shopType.getId());
        if (existShopType == null) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NOT_EXIST);
        }

        // ========== 3. 校验商铺类型名称不能是空串 ==========
        // 如果传入了名称参数，校验不能是空串或空白字符串
        if (shopType.getName() != null && StrUtil.isBlank(shopType.getName())) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NAME_NOT_NULL);
        }

        // ========== 4. 校验商铺类型名称是否已存在（排除自身） ==========
        // 只有当传入了有效名称时才检查唯一性
        if (StrUtil.isNotBlank(shopType.getName()) && checkShopTypeNameExist(shopType.getName(), shopType.getId())) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NAME_EXIST);
        }

        // ========== 5. 设置更新时间 ==========
        shopType.setUpdateTime(LocalDateTime.now());

        // ========== 6. 更新商铺类型信息 ==========
        boolean success = updateById(shopType);
        if (success) {
            log.info(ShopTypeConstants.SHOP_TYPE_UPDATE_SUCCESS, shopType.getId());
            return Result.ok();
        }

        return Result.fail(ShopTypeConstants.SHOP_TYPE_UPDATE_FAIL);
    }

    /**
     * 根据ID查询商铺类型
     * 业务逻辑：
     * 1. 校验商铺类型ID不能为空
     * 2. 根据ID查询商铺类型
     * 3. 校验商铺类型是否存在
     *
     * @param id 商铺类型ID
     * @return 商铺类型信息
     */
    @Override
    public Result getShopTypeById(Long id) {
        // ========== 1. 校验商铺类型ID不能为空 ==========
        if (id == null) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_ID_NOT_NULL);
        }

        // ========== 2. 根据ID查询商铺类型 ==========
        ShopType shopType = getById(id);

        // ========== 3. 校验商铺类型是否存在 ==========
        if (shopType == null) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NOT_EXIST);
        }

        return Result.ok(shopType);
    }

    /**
     * 分页查询商铺类型
     * 支持按类型名称模糊筛选
     * 业务逻辑：
     * 1. 处理默认分页参数
     * 2. 限制最大分页数
     * 3. 构建查询条件（支持名称模糊搜索）
     * 4. 执行分页查询
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param name    类型名称（可选，模糊匹配）
     * @return 分页结果，包含数据列表和总记录数
     */
    @Override
    public Result queryShopTypePage(Integer current, Integer size, String name) {
        // ========== 1. 处理默认分页参数 ==========
        if (current == null || current <= 0) {
            current = ShopConstants.DEFAULT_PAGE_CURRENT;
        }

        // ========== 2. 限制最大分页数 ==========
        if (size == null || size <= 0) {
            size = ShopConstants.DEFAULT_PAGE_SIZE;
        } else if (size > ShopConstants.MAX_PAGE_SIZE) {
            size = ShopConstants.MAX_PAGE_SIZE;
        }

        // ========== 3. 构建查询条件 ==========
        Page<ShopType> page = new Page<>(current, size);
        LambdaQueryWrapper<ShopType> queryWrapper = new LambdaQueryWrapper<>();

        // 按类型名称模糊筛选（如果有传入name参数）
        if (StrUtil.isNotBlank(name)) {
            queryWrapper.like(ShopType::getName, name);
        }

        // 按排序值升序排序，再按更新时间降序排序
        queryWrapper.orderByAsc(ShopType::getSort);
        queryWrapper.orderByDesc(ShopType::getUpdateTime);

        // ========== 4. 执行分页查询 ==========
        page(page, queryWrapper);

        return Result.ok(page.getRecords(), page.getTotal());
    }

    /**
     * 根据ID删除商铺类型
     * 业务逻辑：
     * 1. 校验商铺类型ID不能为空
     * 2. 校验商铺类型是否存在
     * 3. 校验是否有关联的商铺
     * 4. 执行删除操作
     *
     * @param id 商铺类型ID
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteShopTypeById(Long id) {
        // ========== 1. 校验商铺类型ID不能为空 ==========
        if (id == null) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_ID_NOT_NULL);
        }

        // ========== 2. 校验商铺类型是否存在 ==========
        ShopType shopType = getById(id);
        if (shopType == null) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NOT_EXIST);
        }

        // ========== 3. 校验是否有关联的商铺 ==========
        if (hasAssociatedShop(id)) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_HAS_ASSOCIATED_SHOP);
        }

        // ========== 4. 执行删除操作 ==========
        boolean success = removeById(id);
        if (success) {
            log.info(ShopTypeConstants.SHOP_TYPE_DELETE_SUCCESS, id);
            return Result.ok();
        }

        return Result.fail(ShopTypeConstants.SHOP_TYPE_DELETE_FAIL);
    }

    /**
     * 批量删除商铺类型
     * 业务逻辑：
     * 1. 校验商铺类型ID列表不能为空
     * 2. 批量查询所有商铺类型，校验是否存在
     * 3. 批量查询关联的商铺，校验是否有关联
     * 4. 执行批量删除操作
     *
     * @param ids 商铺类型ID列表
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteShopTypeByIds(List<Long> ids) {
        // ========== 1. 校验商铺类型ID列表不能为空 ==========
        if (ids == null || ids.isEmpty()) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_ID_LIST_NOT_NULL);
        }

        // ========== 2. 批量查询所有商铺类型，校验是否存在 ==========
        // 使用listByIds一次性查询所有类型，替代循环单条查询
        List<ShopType> existShopTypes = listByIds(ids);
        Set<Long> existIds = existShopTypes.stream()
                .map(ShopType::getId)
                .collect(Collectors.toSet());

        // 找出不存在的ID
        Optional<Long> nonExistId = ids.stream()
                .filter(id -> !existIds.contains(id))
                .findFirst();
        if (nonExistId.isPresent()) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NOT_EXIST + "，商铺类型ID：" + nonExistId.get());
        }

        // ========== 3. 批量查询关联的商铺，校验是否有关联 ==========
        // 使用in查询一次性查询所有typeId的关联商铺
        Set<Long> associatedTypeIds = findAssociatedTypeIds(ids);

        // 找出有关联商铺的类型ID
        Optional<Long> associatedId = ids.stream()
                .filter(associatedTypeIds::contains)
                .findFirst();
        if (associatedId.isPresent()) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_HAS_ASSOCIATED_SHOP + "，商铺类型ID：" + associatedId.get());
        }

        // ========== 4. 执行批量删除操作 ==========
        boolean success = removeByIds(ids);
        if (success) {
            log.info(ShopTypeConstants.SHOP_TYPE_BATCH_DELETE_SUCCESS, ids.size());
            return Result.ok();
        }

        return Result.fail(ShopTypeConstants.SHOP_TYPE_BATCH_DELETE_FAIL);
    }

    /**
     * 查询所有商铺类型
     * 按排序值升序排列
     *
     * @return 所有商铺类型列表
     */
    @Override
    public Result queryAllShopTypes() {
        LambdaQueryWrapper<ShopType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(ShopType::getSort);
        List<ShopType> shopTypes = list(queryWrapper);
        return Result.ok(shopTypes);
    }

    /**
     * 检查商铺类型名称是否已存在
     *
     * @param shopTypeName 商铺类型名称
     * @param excludeId    需要排除的商铺类型ID（更新时使用，排除自身）
     * @return true-已存在，false-不存在
     */
    private boolean checkShopTypeNameExist(String shopTypeName, Long excludeId) {
        LambdaQueryWrapper<ShopType> queryWrapper = new LambdaQueryWrapper<>();
        // 等值查询商铺类型名称
        queryWrapper.eq(ShopType::getName, shopTypeName);

        // 更新时排除自身ID
        if (excludeId != null) {
            queryWrapper.ne(ShopType::getId, excludeId);
        }

        // 统计符合条件的记录数，大于0表示已存在
        return count(queryWrapper) > 0;
    }

    /**
     * 检查是否有商铺关联该类型
     * 适用于单条查询的场景
     *
     * @param typeId 商铺类型ID
     * @return true-有关联商铺，false-无关联商铺
     */
    private boolean hasAssociatedShop(Long typeId) {
        if (typeId == null) {
            return false;
        }
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Shop::getTypeId, typeId);
        // 统计符合条件的商铺数量，大于0表示有关联商铺
        return shopMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 批量查询关联商铺的类型ID
     * 使用in查询一次性查询所有typeId，替代循环单条查询
     *
     * @param typeIds 商铺类型ID列表
     * @return 有关联商铺的类型ID集合
     */
    private Set<Long> findAssociatedTypeIds(List<Long> typeIds) {
        if (typeIds == null || typeIds.isEmpty()) {
            return Set.of();
        }

        // 使用in查询一次性查询所有typeId
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Shop::getTypeId, typeIds);
        // 只查询typeId字段，提高查询效率
        queryWrapper.select(Shop::getTypeId);
        // 去重，只获取不同的typeId
        queryWrapper.groupBy(Shop::getTypeId);

        List<Shop> shops = shopMapper.selectList(queryWrapper);

        return shops.stream()
                .map(Shop::getTypeId)
                .collect(Collectors.toSet());
    }
}
