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

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号（Query参数） |

**请求示例：**
```
POST /user/code?phone=13800138000
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
  "errorMsg": "手机号格式错误",
  "data": null,
  "total": null
}
```

---

### 2. 用户登录

**接口路径：** `POST /user/login`

**请求参数：** (RequestBody - JSON)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |
| code | String | 否 | 验证码（验证码登录时必填） |
| password | String | 否 | 密码（密码登录时必填） |

**请求示例：**
```json
{
  "phone": "13800138000",
  "code": "123456"
}
```

或

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
  "data": "token_string",
  "total": null
}
```

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

## 五、常量说明

### 商铺相关常量

| 常量名 | 值 | 说明 |
|--------|-----|------|
| DEFAULT_PAGE_CURRENT | 1 | 默认页码 |
| DEFAULT_PAGE_SIZE | 10 | 默认每页大小 |
| MAX_PAGE_SIZE | 100 | 最大每页大小 |
