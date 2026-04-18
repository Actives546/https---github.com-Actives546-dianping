# API 接口文档

## 通用响应结构

所有接口的响应都使用统一的 `Result` 对象：

```json
{
  "success": true,
  "errorMsg": null,
  "data": {},
  "total": 0
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| success | Boolean | 操作是否成功 |
| errorMsg | String | 错误信息（success=false时） |
| data | Object | 返回数据 |
| total | Long | 总记录数（分页查询时） |

---

## 一、用户管理接口

### 1. 发送手机验证码

**接口路径：** `POST /user/code`

**接口说明：** 发送短信验证码到指定手机号，验证码有效期为2分钟。验证码会保存到Redis中，返回值包含生成的验证码（方便开发调试）。

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号（Query参数），必须是11位有效手机号格式 |

**请求示例：**
```
POST /user/code?phone=13800138000
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": "123456",
  "total": null
}
```
> 注：data为生成的6位数字验证码

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "手机号格式错误",
  "data": null,
  "total": null
}
```

---

### 2. 用户登录

**接口路径：** `POST /user/login`

**接口说明：** 用户登录，支持两种方式：
1. 验证码登录：手机号 + 验证码
2. 密码登录：手机号 + 密码

登录成功后返回Token，后续请求需要在Header中携带 `authorization: {token}` 进行身份认证。

**请求参数：** (RequestBody - JSON)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号，必须是11位有效手机号格式 |
| code | String | 否 | 验证码（验证码登录时必填，6位数字） |
| password | String | 否 | 密码（密码登录时必填） |

**请求示例1 - 验证码登录：**
```json
{
  "phone": "13800138000",
  "code": "123456"
}
```

**请求示例2 - 密码登录：**
```json
{
  "phone": "13800138000",
  "password": "123456"
}
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": "550e8400e29b41d4a716446655440000",
  "total": null
}
```
> 注：data为登录Token，有效期30小时

**错误返回示例1 - 验证码错误：**
```json
{
  "success": false,
  "errorMsg": "验证码错误或已过期",
  "data": null,
  "total": null
}
```

**错误返回示例2 - 密码错误：**
```json
{
  "success": false,
  "errorMsg": "手机号或密码错误",
  "data": null,
  "total": null
}
```

---

### 3. 用户退出登录

**接口路径：** `POST /user/logout`

**接口说明：** 用户退出登录，清除Redis中的Token缓存。需要在Header中携带有效的authorization Token。

**请求参数：** 无

**请求头：**
```
authorization: {token}
```

**请求示例：**
```
POST /user/logout
Header: authorization: 550e8400e29b41d4a716446655440000
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

---

### 4. 获取当前登录用户信息

**接口路径：** `GET /user/me`

**接口说明：** 获取当前登录用户的基本信息（脱敏）。返回用户的ID、昵称和头像。需要登录状态。

**请求参数：** 无

**请求头：**
```
authorization: {token}
```

**请求示例：**
```
GET /user/me
Header: authorization: 550e8400e29b41d4a716446655440000
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "id": 1,
    "nickName": "user_abc123def4",
    "icon": ""
  },
  "total": null
}
```

**返回字段说明：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 用户ID |
| nickName | String | 用户昵称 |
| icon | String | 用户头像URL |

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "用户未登录",
  "data": null,
  "total": null
}
```

---

### 5. 新增用户

**接口路径：** `POST /user`

**接口说明：** 管理员新增用户。需要登录状态。手机号必须唯一，不能重复。

**请求参数：** (RequestBody - JSON)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号，必须唯一 |
| password | String | 否 | 密码（建议加密存储） |
| nickName | String | 否 | 昵称，默认自动生成 `user_` + 随机字符串 |
| icon | String | 否 | 头像URL，默认空字符串 |

**请求示例：**
```json
{
  "phone": "13900139000",
  "password": "e10adc3949ba59abbe56e057f20f883e",
  "nickName": "张三",
  "icon": "https://example.com/avatar.png"
}
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": 2,
  "total": null
}
```
> 注：data为新增用户的ID

**错误返回示例1 - 手机号已存在：**
```json
{
  "success": false,
  "errorMsg": "手机号已存在",
  "data": null,
  "total": null
}
```

**错误返回示例2 - 手机号为空：**
```json
{
  "success": false,
  "errorMsg": "手机号不能为空",
  "data": null,
  "total": null
}
```

---

### 6. 更新用户信息

**接口路径：** `PUT /user`

**接口说明：** 更新用户信息。需要登录状态。更新手机号时需要确保新手机号未被其他用户使用。

**请求参数：** (RequestBody - JSON)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |
| phone | String | 否 | 手机号（修改时需要确保唯一） |
| password | String | 否 | 密码 |
| nickName | String | 否 | 昵称 |
| icon | String | 否 | 头像URL |

**请求示例：**
```json
{
  "id": 1,
  "nickName": "新昵称",
  "icon": "https://example.com/new-avatar.png"
}
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

**错误返回示例1 - 用户不存在：**
```json
{
  "success": false,
  "errorMsg": "用户不存在",
  "data": null,
  "total": null
}
```

**错误返回示例2 - 手机号已被使用：**
```json
{
  "success": false,
  "errorMsg": "手机号已存在",
  "data": null,
  "total": null
}
```

---

### 7. 根据ID查询用户信息

**接口路径：** `GET /user/{id}`

**接口说明：** 根据用户ID查询用户详细信息。需要登录状态。

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID（Path参数） |

**请求示例：**
```
GET /user/1
Header: authorization: {token}
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "id": 1,
    "phone": "13800138000",
    "password": "e10adc3949ba59abbe56e057f20f883e",
    "nickName": "张三",
    "icon": "https://example.com/avatar.png",
    "createTime": "2024-01-01T12:00:00",
    "updateTime": "2024-01-15T08:30:00"
  },
  "total": null
}
```

**返回字段说明：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 用户ID |
| phone | String | 手机号 |
| password | String | 密码（加密存储） |
| nickName | String | 昵称 |
| icon | String | 头像URL |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "用户不存在",
  "data": null,
  "total": null
}
```

---

### 8. 分页查询用户列表

**接口路径：** `GET /user/page`

**接口说明：** 分页查询用户列表，支持按手机号和昵称模糊搜索。需要登录状态。

**请求参数：** (Query参数)

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| current | Integer | 否 | 1 | 当前页码，从1开始 |
| size | Integer | 否 | 10 | 每页大小，默认10，最大100 |
| phone | String | 否 | null | 手机号（模糊匹配） |
| nickName | String | 否 | null | 昵称（模糊匹配） |

**请求示例：**
```
GET /user/page?current=1&size=10&phone=138&nickName=张
Header: authorization: {token}
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": [
    {
      "id": 1,
      "phone": "13800138000",
      "password": "e10adc3949ba59abbe56e057f20f883e",
      "nickName": "张三",
      "icon": "https://example.com/avatar.png",
      "createTime": "2024-01-01T12:00:00",
      "updateTime": "2024-01-15T08:30:00"
    },
    {
      "id": 2,
      "phone": "13800138001",
      "password": null,
      "nickName": "user_abc123def4",
      "icon": "",
      "createTime": "2024-01-02T10:00:00",
      "updateTime": "2024-01-02T10:00:00"
    }
  ],
  "total": 25
}
```
> 注：total为总记录数，用于分页计算

---

### 9. 根据ID删除用户

**接口路径：** `DELETE /user/{id}`

**接口说明：** 根据用户ID删除单个用户。需要登录状态。删除前会校验用户是否存在。

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID（Path参数） |

**请求示例：**
```
DELETE /user/1
Header: authorization: {token}
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "用户不存在，用户ID：999",
  "data": null,
  "total": null
}
```

---

### 10. 批量删除用户

**接口路径：** `DELETE /user/batch`

**接口说明：** 批量删除多个用户。需要登录状态。会校验所有ID是否都存在，如果有不存在的ID会返回错误。

**请求参数：** (RequestBody - JSON)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| - | List<Long> | 是 | 用户ID列表（JSON数组） |

**请求示例：**
```json
[1, 2, 3]
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

**错误返回示例：**
```json
{
  "success": false,
  "errorMsg": "用户不存在，用户ID：999",
  "data": null,
  "total": null
}
```

---

## 二、商铺管理接口

### 1. 新增商铺

**接口路径：** `POST /shop`

**请求参数：** (RequestBody - JSON)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 商铺名称 |
| typeId | Long | 否 | 商铺类型ID |
| images | String | 否 | 商铺图片，多个图片以','隔开 |
| area | String | 否 | 商圈，例如陆家嘴 |
| address | String | 否 | 地址 |
| x | Double | 否 | 经度 |
| y | Double | 否 | 纬度 |
| avgPrice | Long | 否 | 均价，取整数 |
| openHours | String | 否 | 营业时间，例如 10:00-22:00 |

**请求示例：**
```json
{
  "name": "测试商铺",
  "typeId": 1,
  "images": "image1.jpg,image2.jpg",
  "area": "陆家嘴",
  "address": "上海市浦东新区",
  "x": 121.5,
  "y": 31.2,
  "avgPrice": 100,
  "openHours": "10:00-22:00"
}
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": 1,
  "total": null
}
```
> 注：data为新增商铺的ID

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "商铺名称不能为空",
  "data": null,
  "total": null
}
```

---

### 2. 更新商铺信息

**接口路径：** `PUT /shop`

**请求参数：** (RequestBody - JSON)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商铺ID |
| name | String | 否 | 商铺名称 |
| typeId | Long | 否 | 商铺类型ID |
| images | String | 否 | 商铺图片 |
| area | String | 否 | 商圈 |
| address | String | 否 | 地址 |
| x | Double | 否 | 经度 |
| y | Double | 否 | 纬度 |
| avgPrice | Long | 否 | 均价 |
| openHours | String | 否 | 营业时间 |

**请求示例：**
```json
{
  "id": 1,
  "name": "更新后的商铺名称",
  "avgPrice": 150
}
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "商铺不存在",
  "data": null,
  "total": null
}
```

---

### 3. 根据ID查询商铺信息

**接口路径：** `GET /shop/{id}`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商铺ID（Path参数） |

**请求示例：**
```
GET /shop/1
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "id": 1,
    "name": "测试商铺",
    "typeId": 1,
    "images": "image1.jpg,image2.jpg",
    "area": "陆家嘴",
    "address": "上海市浦东新区",
    "x": 121.5,
    "y": 31.2,
    "avgPrice": 100,
    "sold": 0,
    "comments": 0,
    "score": 0,
    "openHours": "10:00-22:00",
    "createTime": "2024-01-01T12:00:00",
    "updateTime": "2024-01-01T12:00:00",
    "distance": null,
    "shopType": {
      "id": 1,
      "name": "美食",
      "icon": "food.png",
      "sort": 1
    }
  },
  "total": null
}
```

**返回字段说明：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 商铺ID |
| name | String | 商铺名称 |
| typeId | Long | 商铺类型ID |
| images | String | 商铺图片 |
| area | String | 商圈 |
| address | String | 地址 |
| x | Double | 经度 |
| y | Double | 纬度 |
| avgPrice | Long | 均价 |
| sold | Integer | 销量 |
| comments | Integer | 评论数量 |
| score | Integer | 评分（1~5分，乘10保存） |
| openHours | String | 营业时间 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| distance | Double | 距离（非数据库字段） |
| shopType | ShopType | 关联的商铺类型信息 |

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "商铺不存在",
  "data": null,
  "total": null
}
```

---

### 4. 分页查询商铺信息

**接口路径：** `GET /shop/page`

**请求参数：** (Query参数)

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| current | Integer | 否 | 1 | 当前页码 |
| size | Integer | 否 | 10 | 每页大小（最大100） |
| name | String | 否 | null | 商铺名称（模糊匹配） |

**请求示例：**
```
GET /shop/page?current=1&size=10&name=美食
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": [
    {
      "id": 1,
      "name": "美食商铺",
      "typeId": 1,
      "images": "image1.jpg",
      "area": "陆家嘴",
      "address": "上海市浦东新区",
      "x": 121.5,
      "y": 31.2,
      "avgPrice": 100,
      "sold": 0,
      "comments": 0,
      "score": 0,
      "openHours": "10:00-22:00",
      "createTime": "2024-01-01T12:00:00",
      "updateTime": "2024-01-01T12:00:00",
      "distance": null,
      "shopType": {
        "id": 1,
        "name": "美食",
        "icon": "food.png",
        "sort": 1
      }
    }
  ],
  "total": 1
}
```

---

### 5. 根据ID删除商铺

**接口路径：** `DELETE /shop/{id}`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商铺ID（Path参数） |

**请求示例：**
```
DELETE /shop/1
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "商铺不存在",
  "data": null,
  "total": null
}
```

---

### 6. 批量删除商铺

**接口路径：** `DELETE /shop/batch`

**请求参数：** (RequestBody - JSON)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<Long> | 是 | 商铺ID列表 |

**请求示例：**
```json
[1, 2, 3]
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "商铺不存在，商铺ID：5",
  "data": null,
  "total": null
}
```

---

## 三、商铺类型管理接口

### 1. 新增商铺类型

**接口路径：** `POST /shop-type`

**请求参数：** (RequestBody - JSON)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 类型名称 |
| icon | String | 否 | 图标 |
| sort | Integer | 否 | 顺序（默认0） |

**请求示例：**
```json
{
  "name": "美食",
  "icon": "food.png",
  "sort": 1
}
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": 1,
  "total": null
}
```
> 注：data为新增商铺类型的ID

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "商铺类型名称不能为空",
  "data": null,
  "total": null
}
```

---

### 2. 更新商铺类型

**接口路径：** `PUT /shop-type`

**请求参数：** (RequestBody - JSON)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 类型ID |
| name | String | 否 | 类型名称 |
| icon | String | 否 | 图标 |
| sort | Integer | 否 | 顺序 |

**请求示例：**
```json
{
  "id": 1,
  "name": "美食小吃",
  "sort": 2
}
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "商铺类型不存在",
  "data": null,
  "total": null
}
```

---

### 3. 根据ID查询商铺类型

**接口路径：** `GET /shop-type/{id}`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 类型ID（Path参数） |

**请求示例：**
```
GET /shop-type/1
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "id": 1,
    "name": "美食",
    "icon": "food.png",
    "sort": 1
  },
  "total": null
}
```

**返回字段说明：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 类型ID |
| name | String | 类型名称 |
| icon | String | 图标 |
| sort | Integer | 顺序 |

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "商铺类型不存在",
  "data": null,
  "total": null
}
```

---

### 4. 查询所有商铺类型

**接口路径：** `GET /shop-type/list`

**请求参数：** 无

**请求示例：**
```
GET /shop-type/list
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": [
    {
      "id": 1,
      "name": "美食",
      "icon": "food.png",
      "sort": 1
    },
    {
      "id": 2,
      "name": "娱乐",
      "icon": "entertainment.png",
      "sort": 2
    }
  ],
  "total": null
}
```

---

### 5. 分页查询商铺类型

**接口路径：** `GET /shop-type/page`

**请求参数：** (Query参数)

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| current | Integer | 否 | 1 | 当前页码 |
| size | Integer | 否 | 10 | 每页大小（最大100） |
| name | String | 否 | null | 类型名称（模糊匹配） |

**请求示例：**
```
GET /shop-type/page?current=1&size=10&name=美食
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": [
    {
      "id": 1,
      "name": "美食",
      "icon": "food.png",
      "sort": 1
    }
  ],
  "total": 1
}
```

---

### 6. 根据ID删除商铺类型

**接口路径：** `DELETE /shop-type/{id}`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 类型ID（Path参数） |

**请求示例：**
```
DELETE /shop-type/1
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "商铺类型不存在",
  "data": null,
  "total": null
}
```

---

### 7. 批量删除商铺类型

**接口路径：** `DELETE /shop-type/batch`

**请求参数：** (RequestBody - JSON)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<Long> | 是 | 类型ID列表 |

**请求示例：**
```json
[1, 2, 3]
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "商铺类型不存在，商铺类型ID：5",
  "data": null,
  "total": null
}
```

---

## 四、数据实体说明

### Shop（商铺）实体

| 字段名 | 类型 | 数据库字段 | 说明 |
|--------|------|------------|------|
| id | Long | id | 主键，自增 |
| name | String | name | 商铺名称 |
| typeId | Long | type_id | 商铺类型ID |
| images | String | images | 商铺图片，多个图片以','隔开 |
| area | String | area | 商圈 |
| address | String | address | 地址 |
| x | Double | x | 经度 |
| y | Double | y | 纬度 |
| avgPrice | Long | avg_price | 均价 |
| sold | Integer | sold | 销量 |
| comments | Integer | comments | 评论数量 |
| score | Integer | score | 评分（1~5分，乘10保存） |
| openHours | String | open_hours | 营业时间 |
| createTime | LocalDateTime | create_time | 创建时间 |
| updateTime | LocalDateTime | update_time | 更新时间 |
| distance | Double | - | 距离（非数据库字段） |
| shopType | ShopType | - | 关联的商铺类型（非数据库字段） |

### ShopType（商铺类型）实体

| 字段名 | 类型 | 数据库字段 | 说明 |
|--------|------|------------|------|
| id | Long | id | 主键，自增 |
| name | String | name | 类型名称 |
| icon | String | icon | 图标 |
| sort | Integer | sort | 顺序 |
| createTime | LocalDateTime | create_time | 创建时间（JSON忽略） |
| updateTime | LocalDateTime | update_time | 更新时间（JSON忽略） |

---

## 四、优惠券管理接口

优惠券分为两种类型，通过 `type` 字段区分：
- `type=0`：普通券
- `type=1`：秒杀券（需要额外传入 `stock`、`beginTime`、`endTime`）

### 1. 新增优惠券

**接口路径：** `POST /voucher`

**接口说明：** 新增优惠券，根据type字段区分普通券和秒杀券。
- 普通券：只需传入基础信息
- 秒杀券：需额外传入库存、开始时间、结束时间

**请求参数：** (RequestBody - JSON)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| shopId | Long | 是 | 关联的商铺ID |
| title | String | 是 | 优惠券标题 |
| subTitle | String | 否 | 副标题 |
| rules | String | 否 | 使用规则 |
| payValue | Long | 是 | 支付金额（必须大于0） |
| actualValue | Long | 是 | 抵扣金额（必须大于0） |
| type | Integer | 否 | 优惠券类型，0-普通券（默认），1-秒杀券 |
| status | Integer | 否 | 状态，1-正常，2-过期 |
| stock | Integer | 秒杀券必填 | 库存（秒杀券专用，必须大于等于0） |
| beginTime | LocalDateTime | 秒杀券必填 | 开始时间（秒杀券专用） |
| endTime | LocalDateTime | 秒杀券必填 | 结束时间（秒杀券专用，必须晚于开始时间） |

**请求示例1 - 新增普通券：**
```json
{
  "shopId": 1,
  "title": "满100减20优惠券",
  "subTitle": "全场通用",
  "rules": "满100元可用，不可与其他优惠叠加",
  "payValue": 80,
  "actualValue": 100,
  "type": 0,
  "status": 1
}
```

**请求示例2 - 新增秒杀券：**
```json
{
  "shopId": 1,
  "title": "1元秒杀100元代金券",
  "subTitle": "限时秒杀",
  "rules": "每人限抢1张",
  "payValue": 1,
  "actualValue": 100,
  "type": 1,
  "status": 1,
  "stock": 100,
  "beginTime": "2024-01-20T10:00:00",
  "endTime": "2024-01-20T22:00:00"
}
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": 1,
  "total": null
}
```
> 注：data为新增优惠券的ID

**错误返回示例1 - 商铺不存在：**
```json
{
  "success": false,
  "errorMsg": "商铺不存在",
  "data": null,
  "total": null
}
```

**错误返回示例2 - 秒杀券缺少库存：**
```json
{
  "success": false,
  "errorMsg": "秒杀券库存不能为空",
  "data": null,
  "total": null
}
```

---

### 2. 更新优惠券信息

**接口路径：** `PUT /voucher`

**接口说明：** 更新优惠券信息。如果是秒杀券，可同时更新库存、开始时间、结束时间。

**请求参数：** (RequestBody - JSON)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 优惠券ID |
| shopId | Long | 否 | 关联的商铺ID |
| title | String | 否 | 优惠券标题 |
| subTitle | String | 否 | 副标题 |
| rules | String | 否 | 使用规则 |
| payValue | Long | 否 | 支付金额 |
| actualValue | Long | 否 | 抵扣金额 |
| type | Integer | 否 | 优惠券类型 |
| status | Integer | 否 | 状态 |
| stock | Integer | 否 | 库存（秒杀券可更新） |
| beginTime | LocalDateTime | 否 | 开始时间（秒杀券可更新） |
| endTime | LocalDateTime | 否 | 结束时间（秒杀券可更新） |

**请求示例：**
```json
{
  "id": 1,
  "title": "更新后的优惠券标题",
  "stock": 200,
  "endTime": "2024-01-25T23:59:59"
}
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "优惠券不存在",
  "data": null,
  "total": null
}
```

---

### 3. 根据ID查询优惠券信息

**接口路径：** `GET /voucher/{id}`

**接口说明：** 根据ID查询优惠券详情。如果是秒杀券，会自动关联查询秒杀券信息（库存、开始时间、结束时间）。

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 优惠券ID（Path参数） |

**请求示例：**
```
GET /voucher/1
```

**返回示例1 - 普通券：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "id": 1,
    "shopId": 1,
    "title": "满100减20优惠券",
    "subTitle": "全场通用",
    "rules": "满100元可用",
    "payValue": 80,
    "actualValue": 100,
    "type": 0,
    "status": 1,
    "stock": null,
    "beginTime": null,
    "endTime": null,
    "createTime": "2024-01-15T10:00:00",
    "updateTime": "2024-01-15T10:00:00"
  },
  "total": null
}
```

**返回示例2 - 秒杀券：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": {
    "id": 2,
    "shopId": 1,
    "title": "1元秒杀100元代金券",
    "subTitle": "限时秒杀",
    "rules": "每人限抢1张",
    "payValue": 1,
    "actualValue": 100,
    "type": 1,
    "status": 1,
    "stock": 100,
    "beginTime": "2024-01-20T10:00:00",
    "endTime": "2024-01-20T22:00:00",
    "createTime": "2024-01-15T10:00:00",
    "updateTime": "2024-01-15T10:00:00"
  },
  "total": null
}
```

**返回字段说明：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 优惠券ID |
| shopId | Long | 关联的商铺ID |
| title | String | 优惠券标题 |
| subTitle | String | 副标题 |
| rules | String | 使用规则 |
| payValue | Long | 支付金额 |
| actualValue | Long | 抵扣金额 |
| type | Integer | 类型：0-普通券，1-秒杀券 |
| status | Integer | 状态：1-正常，2-过期 |
| stock | Integer | 库存（秒杀券专用，普通券为null） |
| beginTime | LocalDateTime | 开始时间（秒杀券专用，普通券为null） |
| endTime | LocalDateTime | 结束时间（秒杀券专用，普通券为null） |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "优惠券不存在",
  "data": null,
  "total": null
}
```

---

### 4. 分页查询优惠券信息

**接口路径：** `GET /voucher/page`

**接口说明：** 分页查询优惠券列表，支持按商铺ID和优惠券类型筛选。
- 不传 `type`：查询所有类型的优惠券
- `type=0`：只查询普通券
- `type=1`：只查询秒杀券

**请求参数：** (Query参数)

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| current | Integer | 否 | 1 | 当前页码，从1开始 |
| size | Integer | 否 | 10 | 每页大小，默认10，最大100 |
| shopId | Long | 否 | null | 商铺ID（按商铺筛选） |
| type | Integer | 否 | null | 优惠券类型：0-普通券，1-秒杀券 |

**请求示例1 - 查询所有优惠券：**
```
GET /voucher/page?current=1&size=10
```

**请求示例2 - 按商铺筛选：**
```
GET /voucher/page?current=1&size=10&shopId=1
```

**请求示例3 - 查询秒杀券：**
```
GET /voucher/page?current=1&size=10&type=1
```

**请求示例4 - 按商铺+类型筛选：**
```
GET /voucher/page?current=1&size=10&shopId=1&type=1
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": [
    {
      "id": 1,
      "shopId": 1,
      "title": "满100减20优惠券",
      "subTitle": "全场通用",
      "rules": "满100元可用",
      "payValue": 80,
      "actualValue": 100,
      "type": 0,
      "status": 1,
      "stock": null,
      "beginTime": null,
      "endTime": null,
      "createTime": "2024-01-15T10:00:00",
      "updateTime": "2024-01-15T10:00:00"
    },
    {
      "id": 2,
      "shopId": 1,
      "title": "1元秒杀100元代金券",
      "subTitle": "限时秒杀",
      "rules": "每人限抢1张",
      "payValue": 1,
      "actualValue": 100,
      "type": 1,
      "status": 1,
      "stock": 100,
      "beginTime": "2024-01-20T10:00:00",
      "endTime": "2024-01-20T22:00:00",
      "createTime": "2024-01-15T10:00:00",
      "updateTime": "2024-01-15T10:00:00"
    }
  ],
  "total": 25
}
```
> 注：total为总记录数，用于分页计算

---

### 5. 根据商铺ID查询优惠券列表

**接口路径：** `GET /voucher/shop/{shopId}`

**接口说明：** 查询指定商铺下的所有优惠券（包含普通券和秒杀券）。

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| shopId | Long | 是 | 商铺ID（Path参数） |

**请求示例：**
```
GET /voucher/shop/1
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": [
    {
      "id": 1,
      "shopId": 1,
      "title": "满100减20优惠券",
      "type": 0,
      "status": 1,
      "stock": null,
      "beginTime": null,
      "endTime": null,
      "createTime": "2024-01-15T10:00:00",
      "updateTime": "2024-01-15T10:00:00"
    },
    {
      "id": 2,
      "shopId": 1,
      "title": "1元秒杀100元代金券",
      "type": 1,
      "status": 1,
      "stock": 100,
      "beginTime": "2024-01-20T10:00:00",
      "endTime": "2024-01-20T22:00:00",
      "createTime": "2024-01-15T10:00:00",
      "updateTime": "2024-01-15T10:00:00"
    }
  ],
  "total": null
}
```

---

### 6. 根据ID删除优惠券

**接口路径：** `DELETE /voucher/{id}`

**接口说明：** 根据ID删除优惠券。如果是秒杀券，会自动删除关联的秒杀券信息。

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 优惠券ID（Path参数） |

**请求示例：**
```
DELETE /voucher/1
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

**错误返回：**
```json
{
  "success": false,
  "errorMsg": "优惠券不存在",
  "data": null,
  "total": null
}
```

---

### 7. 批量删除优惠券

**接口路径：** `DELETE /voucher/batch`

**接口说明：** 批量删除多个优惠券。会校验所有ID是否都存在，如果有不存在的ID会返回错误。删除时会自动删除关联的秒杀券信息。

**请求参数：** (RequestBody - JSON)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| - | List<Long> | 是 | 优惠券ID列表（JSON数组） |

**请求示例：**
```json
[1, 2, 3]
```

**返回示例：**
```json
{
  "success": true,
  "errorMsg": null,
  "data": null,
  "total": null
}
```

**错误返回示例：**
```json
{
  "success": false,
  "errorMsg": "优惠券不存在，优惠券ID：999",
  "data": null,
  "total": null
}
```

---

## 五、数据实体说明

### Shop（商铺）实体

| 字段名 | 类型 | 数据库字段 | 说明 |
|--------|------|------------|------|
| id | Long | id | 主键，自增 |
| name | String | name | 商铺名称 |
| typeId | Long | type_id | 商铺类型ID |
| images | String | images | 商铺图片，多个图片以','隔开 |
| area | String | area | 商圈 |
| address | String | address | 地址 |
| x | Double | x | 经度 |
| y | Double | y | 纬度 |
| avgPrice | Long | avg_price | 均价 |
| sold | Integer | sold | 销量 |
| comments | Integer | comments | 评论数量 |
| score | Integer | score | 评分（1~5分，乘10保存） |
| openHours | String | open_hours | 营业时间 |
| createTime | LocalDateTime | create_time | 创建时间 |
| updateTime | LocalDateTime | update_time | 更新时间 |
| distance | Double | - | 距离（非数据库字段） |
| shopType | ShopType | - | 关联的商铺类型（非数据库字段） |

### ShopType（商铺类型）实体

| 字段名 | 类型 | 数据库字段 | 说明 |
|--------|------|------------|------|
| id | Long | id | 主键，自增 |
| name | String | name | 类型名称 |
| icon | String | icon | 图标 |
| sort | Integer | sort | 顺序 |
| createTime | LocalDateTime | create_time | 创建时间（JSON忽略） |
| updateTime | LocalDateTime | update_time | 更新时间（JSON忽略） |

### Voucher（优惠券）实体

| 字段名 | 类型 | 数据库字段 | 说明 |
|--------|------|------------|------|
| id | Long | id | 主键，自增 |
| shopId | Long | shop_id | 关联的商铺ID |
| title | String | title | 优惠券标题 |
| subTitle | String | sub_title | 副标题 |
| rules | String | rules | 使用规则 |
| payValue | Long | pay_value | 支付金额 |
| actualValue | Long | actual_value | 抵扣金额 |
| type | Integer | type | 类型：0-普通券，1-秒杀券 |
| status | Integer | status | 状态：1-正常，2-过期 |
| createTime | LocalDateTime | create_time | 创建时间 |
| updateTime | LocalDateTime | update_time | 更新时间 |
| stock | Integer | - | 库存（非数据库字段，秒杀券专用，从SeckillVoucher关联查询） |
| beginTime | LocalDateTime | - | 开始时间（非数据库字段，秒杀券专用，从SeckillVoucher关联查询） |
| endTime | LocalDateTime | - | 结束时间（非数据库字段，秒杀券专用，从SeckillVoucher关联查询） |

### SeckillVoucher（秒杀券）实体

| 字段名 | 类型 | 数据库字段 | 说明 |
|--------|------|------------|------|
| voucherId | Long | voucher_id | 主键，关联的优惠券ID |
| stock | Integer | stock | 库存 |
| beginTime | LocalDateTime | begin_time | 开始时间 |
| endTime | LocalDateTime | end_time | 结束时间 |
| createTime | LocalDateTime | create_time | 创建时间 |
| updateTime | LocalDateTime | update_time | 更新时间 |

---

## 六、常量说明

### 商铺相关常量

| 常量名 | 值 | 说明 |
|--------|-----|------|
| DEFAULT_PAGE_CURRENT | 1 | 默认页码 |
| DEFAULT_PAGE_SIZE | 10 | 默认每页大小 |
| MAX_PAGE_SIZE | 100 | 最大每页大小 |

### 优惠券相关常量

| 常量名 | 值 | 说明 |
|--------|-----|------|
| VOUCHER_TYPE_NORMAL | 0 | 普通券类型 |
| VOUCHER_TYPE_SECKILL | 1 | 秒杀券类型 |
| VOUCHER_STATUS_NORMAL | 1 | 优惠券状态-正常 |
| VOUCHER_STATUS_EXPIRED | 2 | 优惠券状态-过期 |
