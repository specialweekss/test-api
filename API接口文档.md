# 用户游戏数据持久化 API 接口文档

## 基础信息

- **Base URL**: `http://your-domain.com` (开发环境: `http://localhost:8080`)
- **Content-Type**: `application/json`
- **字符编码**: UTF-8

## 统一响应格式

所有接口响应都遵循以下格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| code | number | 响应码，200表示成功，其他表示失败 |
| message | string | 响应消息 |
| data | object | 响应数据，具体结构见各接口说明 |

### 响应码说明

| 响应码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 参数错误 |
| 500 | 服务器内部错误 |

---

## 接口列表

### 1. 获取用户游戏数据

获取指定用户的游戏数据，如果用户不存在则自动创建默认数据。

**接口地址**: `/api/game/user-data`

**请求方法**: `GET`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | string | 是 | 用户唯一标识 |

**请求示例**:

```http
GET /api/game/user-data?userId=123456 HTTP/1.1
Host: localhost:8080
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": "123456",
    "playerInfo": {
      "playerLevel": 1,
      "money": 0,
      "clickRewardBase": 100,
      "clickMultiplier": 1.0,
      "upgradeCost": 10
    },
    "assistants": [
      {
        "id": 1,
        "unlocked": false,
        "level": 0
      },
      {
        "id": 2,
        "unlocked": false,
        "level": 0
      },
      {
        "id": 3,
        "unlocked": false,
        "level": 0
      },
      {
        "id": 4,
        "unlocked": false,
        "level": 0
      }
    ],
    "challenges": [
      {
        "id": 1,
        "completed": false
      },
      {
        "id": 2,
        "completed": false
      },
      {
        "id": 3,
        "completed": false
      },
      {
        "id": 4,
        "completed": false
      },
      {
        "id": 5,
        "completed": false
      }
    ],
    "lastUpdateTime": "2024-01-01 12:00:00"
  }
}
```

**响应数据结构**:

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | string | 用户ID |
| playerInfo | object | 玩家基础信息 |
| playerInfo.playerLevel | number | 玩家等级 (1-999) |
| playerInfo.money | number | 当前金钱 (>= 0) |
| playerInfo.clickRewardBase | number | 点击收益基础值 (>= 0) |
| playerInfo.clickMultiplier | number | 点击收益倍率 (>= 0) |
| playerInfo.upgradeCost | number | 升级所需金币 (>= 0) |
| assistants | array | 助理数据数组，固定4个元素 |
| assistants[].id | number | 助理ID (1-4) |
| assistants[].unlocked | boolean | 是否已解锁 |
| assistants[].level | number | 当前等级 (0表示未解锁，1-50表示已解锁的等级) |
| challenges | array | 挑战数据数组，固定5个元素 |
| challenges[].id | number | 挑战ID (1-5) |
| challenges[].completed | boolean | 是否已完成 |
| lastUpdateTime | string | 最后更新时间 (格式: yyyy-MM-dd HH:mm:ss) |

**错误响应示例**:

```json
{
  "code": 400,
  "message": "用户ID不能为空",
  "data": null
}
```

**注意事项**:
- 如果用户首次访问（数据库中不存在），系统会自动创建默认数据并插入数据库
- 默认数据：玩家等级1，金钱0，所有助理未解锁，所有挑战未完成

---

### 2. 保存用户游戏数据

保存用户的游戏数据到服务器。

**接口地址**: `/api/game/user-data`

**请求方法**: `POST`

**请求头**:

```
Content-Type: application/json
```

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | string | 是 | 用户唯一标识 |
| playerInfo | object | 是 | 玩家基础信息 |
| playerInfo.playerLevel | number | 是 | 玩家等级 (1-999) |
| playerInfo.money | number | 是 | 当前金钱 (>= 0) |
| playerInfo.clickRewardBase | number | 是 | 点击收益基础值 (>= 0) |
| playerInfo.clickMultiplier | number | 是 | 点击收益倍率 (>= 0) |
| playerInfo.upgradeCost | number | 是 | 升级所需金币 (>= 0) |
| assistants | array | 是 | 助理数据数组，必须包含4个元素 |
| assistants[].id | number | 是 | 助理ID (1-4) |
| assistants[].unlocked | boolean | 是 | 是否已解锁 |
| assistants[].level | number | 是 | 当前等级 (0-50) |
| challenges | array | 是 | 挑战数据数组，必须包含5个元素 |
| challenges[].id | number | 是 | 挑战ID (1-5) |
| challenges[].completed | boolean | 是 | 是否已完成 |

**请求示例**:

```http
POST /api/game/user-data HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "userId": "123456",
  "playerInfo": {
    "playerLevel": 5,
    "money": 10000,
    "clickRewardBase": 120,
    "clickMultiplier": 1.4,
    "upgradeCost": 50
  },
  "assistants": [
    {
      "id": 1,
      "unlocked": true,
      "level": 10
    },
    {
      "id": 2,
      "unlocked": true,
      "level": 5
    },
    {
      "id": 3,
      "unlocked": false,
      "level": 0
    },
    {
      "id": 4,
      "unlocked": false,
      "level": 0
    }
  ],
  "challenges": [
    {
      "id": 1,
      "completed": true
    },
    {
      "id": 2,
      "completed": false
    },
    {
      "id": 3,
      "completed": false
    },
    {
      "id": 4,
      "completed": false
    },
    {
      "id": 5,
      "completed": false
    }
  ]
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "lastUpdateTime": "2024-01-01 12:00:00"
  }
}
```

**响应数据结构**:

| 字段 | 类型 | 说明 |
|------|------|------|
| success | boolean | 是否保存成功 |
| lastUpdateTime | string | 最后更新时间 (格式: yyyy-MM-dd HH:mm:ss) |

**错误响应示例**:

```json
{
  "code": 400,
  "message": "玩家等级必须在1-999之间",
  "data": null
}
```

**常见错误信息**:

| 错误信息 | 说明 |
|----------|------|
| 用户ID不能为空 | userId 参数缺失或为空 |
| 玩家信息不能为空 | playerInfo 参数缺失 |
| 玩家等级必须在1-999之间 | playerLevel 超出范围 |
| 金钱不能为负数 | money 小于0 |
| 点击收益基础值不能为负数 | clickRewardBase 小于0 |
| 点击收益倍率不能为负数 | clickMultiplier 小于0 |
| 升级所需金币不能为负数 | upgradeCost 小于0 |
| 助理数据不能为空 | assistants 数组为空或null |
| 助理ID必须大于0 | assistant.id 为空或小于1 |
| 助理等级必须在0-50之间 | assistant.level 超出范围 |
| 挑战数据不能为空 | challenges 数组为空或null |
| 挑战ID必须大于0 | challenge.id 为空或小于1 |
| 请求参数不能为空 | 请求体为空 |

**注意事项**:
- 如果用户不存在，会自动创建新记录
- 如果用户已存在，会更新现有记录
- 所有数值字段都需要进行合法性校验
- assistants 和 challenges 数组可以为任意长度（但不能为空）

---

## 数据校验规则

### 玩家信息 (playerInfo)

| 字段 | 校验规则 |
|------|----------|
| playerLevel | 必填，范围: 1-999 |
| money | 必填，范围: >= 0 |
| clickRewardBase | 必填，范围: >= 0 |
| clickMultiplier | 必填，范围: >= 0 |
| upgradeCost | 必填，范围: >= 0 |

### 助理数据 (assistants)

| 字段 | 校验规则 |
|------|----------|
| 数组长度 | 不能为空，可以为任意长度 |
| id | 必填，范围: >= 1 |
| unlocked | 必填，boolean类型 |
| level | 必填，范围: 0-50 |

### 挑战数据 (challenges)

| 字段 | 校验规则 |
|------|----------|
| 数组长度 | 不能为空，可以为任意长度 |
| id | 必填，范围: >= 1 |
| completed | 必填，boolean类型 |

---

## 调用示例

### JavaScript/TypeScript (使用 fetch)

#### 获取用户数据

```typescript
async function getUserData(userId: string) {
  try {
    const response = await fetch(`http://localhost:8080/api/game/user-data?userId=${userId}`);
    const result = await response.json();
    
    if (result.code === 200) {
      console.log('获取成功:', result.data);
      return result.data;
    } else {
      console.error('获取失败:', result.message);
      return null;
    }
  } catch (error) {
    console.error('请求异常:', error);
    return null;
  }
}
```

#### 保存用户数据

```typescript
async function saveUserData(userId: string, gameData: any) {
  try {
    const response = await fetch('http://localhost:8080/api/game/user-data', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        userId: userId,
        ...gameData
      })
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
      console.log('保存成功:', result.data);
      return true;
    } else {
      console.error('保存失败:', result.message);
      return false;
    }
  } catch (error) {
    console.error('请求异常:', error);
    return false;
  }
}
```

### LayaAir (使用 Laya.HttpRequest)

#### 获取用户数据

```typescript
function getUserData(userId: string, callback: (data: any) => void) {
  const request = new Laya.HttpRequest();
  request.once(Laya.Event.COMPLETE, this, (data: string) => {
    const result = JSON.parse(data);
    if (result.code === 200) {
      callback(result.data);
    } else {
      console.error('获取失败:', result.message);
      callback(null);
    }
  });
  request.once(Laya.Event.ERROR, this, () => {
    console.error('请求失败');
    callback(null);
  });
  
  const url = `http://localhost:8080/api/game/user-data?userId=${userId}`;
  request.send(url, null, 'get', 'json');
}
```

#### 保存用户数据

```typescript
function saveUserData(userId: string, gameData: any, callback: (success: boolean) => void) {
  const request = new Laya.HttpRequest();
  request.once(Laya.Event.COMPLETE, this, (data: string) => {
    const result = JSON.parse(data);
    if (result.code === 200) {
      callback(true);
    } else {
      console.error('保存失败:', result.message);
      callback(false);
    }
  });
  request.once(Laya.Event.ERROR, this, () => {
    console.error('请求失败');
    callback(false);
  });
  
  const url = 'http://localhost:8080/api/game/user-data';
  const postData = JSON.stringify({
    userId: userId,
    ...gameData
  });
  request.send(url, postData, 'post', 'json', ['Content-Type: application/json']);
}
```

---

## 建议的调用时机

### 获取数据
- 游戏启动时
- 切换账号时
- 手动刷新时

### 保存数据
- 玩家升级后
- 金钱变化后（建议达到一定阈值，如每次增加1000以上）
- 助理解锁/升级后
- 挑战完成后
- 定期自动保存（建议每30秒）
- 游戏退出时

---

## 注意事项

1. **网络异常处理**: 建议实现重试机制和本地缓存，网络失败时使用本地缓存数据
2. **数据同步**: 保存失败时，建议将数据加入队列，稍后重试
3. **性能优化**: 避免频繁保存，使用防抖或节流机制
4. **用户ID**: 需要确定如何获取用户的唯一标识（微信小游戏使用 openid，其他平台使用平台提供的用户标识）
5. **时区**: 时间字段使用服务器时区（Asia/Shanghai），格式为 `yyyy-MM-dd HH:mm:ss`

---

## 联系方式

如有问题，请联系后端开发人员。

