# 用户游戏数据持久化 API 使用说明

## 项目结构

```
src/main/java/org/lyf/testapi/
├── controller/          # 控制器层
│   └── GameController.java
├── service/             # 服务层
│   └── UserGameDataService.java
├── mapper/              # MyBatis Mapper 接口
│   └── UserGameDataMapper.java
├── entity/              # 实体类
│   └── UserGameData.java
└── dto/                 # 数据传输对象
    ├── PlayerInfo.java
    ├── Assistant.java
    ├── Challenge.java
    ├── UserGameDataRequest.java
    ├── UserGameDataResponse.java
    └── Result.java
```

## 数据库初始化

执行 `src/main/resources/sql/init.sql` 中的 SQL 脚本创建数据表。

## API 接口

### 1. 获取用户游戏数据

**接口地址**：`GET /api/game/user-data`

**请求参数**：
- `userId` (string, 必填): 用户唯一标识

**请求示例**：
```
GET http://localhost:8080/api/game/user-data?userId=123456
```

**响应示例**：
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

### 2. 保存用户游戏数据

**接口地址**：`POST /api/game/user-data`

**请求头**：
```
Content-Type: application/json
```

**请求体示例**：
```json
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

**响应示例**：
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

**错误响应示例**：
```json
{
  "code": 400,
  "message": "参数错误",
  "data": null
}
```

## 数据校验规则

### 玩家信息校验
- `playerLevel`: 1-999
- `money`: >= 0
- `clickRewardBase`: >= 0
- `clickMultiplier`: >= 0
- `upgradeCost`: >= 0

### 助理数据校验
- 必须包含 4 个助理
- `id`: 1-4
- `level`: 0-50

### 挑战数据校验
- 必须包含 5 个挑战
- `id`: 1-5

## 配置说明

### 数据库配置
在 `application.properties` 中配置数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=12345678
```

### MyBatis 配置
已自动配置，无需额外设置。

## 运行项目

1. 确保 MySQL 数据库已启动
2. 执行 `src/main/resources/sql/init.sql` 创建数据表
3. 运行 `TestApiApplication.main()` 启动项目
4. 默认端口：8080

## 技术栈

- Spring Boot 2.7.18
- MyBatis 2.2.2
- MySQL 8.0
- Lombok
- Jackson (JSON 处理)

