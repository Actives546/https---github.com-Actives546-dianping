-- 秒杀券下单Lua脚本
-- 实现Redis层面的预扣减库存和一人一单判断
-- 参数：
--   KEYS[1]: 秒杀券ID
--   ARGV[1]: 用户ID
-- 返回值：
--   0: 成功
--   1: 库存不足
--   2: 重复下单

local voucherId = ARGV[1]
local userId = ARGV[2]

-- 构建Redis Key
local stockKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order:' .. voucherId

-- 1. 判断库存是否充足
local stock = redis.call('get', stockKey)
if stock == nil or tonumber(stock) <= 0 then
    -- 库存不足
    return 1
end

-- 2. 判断用户是否已经下单
if redis.call('sismember', orderKey, userId) == 1 then
    -- 重复下单
    return 2
end

-- 3. 扣减库存
redis.call('incrby', stockKey, -1)

-- 4. 记录用户已下单
redis.call('sadd', orderKey, userId)

-- 5. 成功
return 0
