# 客户端 Token 失效处理指南

## 问题说明

当 token 失效时（2小时过期），服务器会返回 401 错误。客户端需要实现自动重新登录机制，以提升用户体验。

## Token 失效场景

1. **Token 过期**：token 有效期 2 小时，过期后需要重新获取
2. **Token 无效**：token 不存在或已被清除
3. **服务重启**：服务器重启后，内存中的 token 会丢失

## 解决方案

### 方案1：统一请求拦截器 + 自动重新登录（推荐）

在客户端实现统一的 HTTP 请求拦截器，自动处理 401 错误并重新登录。

#### LayaAir 示例代码

```typescript
// HttpUtil.ts - 统一的HTTP请求工具类
export class HttpUtil {
    private static token: string = null;
    private static baseUrl: string = "https://your-server.com";
    
    /**
     * 设置token
     */
    public static setToken(token: string): void {
        this.token = token;
        // 保存到本地存储
        Laya.LocalStorage.setItem("user_token", token);
    }
    
    /**
     * 获取token
     */
    public static getToken(): string {
        if (!this.token) {
            this.token = Laya.LocalStorage.getItem("user_token");
        }
        return this.token;
    }
    
    /**
     * 清除token
     */
    public static clearToken(): void {
        this.token = null;
        Laya.LocalStorage.removeItem("user_token");
    }
    
    /**
     * 微信登录
     */
    private static async wxLogin(): Promise<string> {
        return new Promise((resolve, reject) => {
            // 调用微信登录
            wx.login({
                success: (res) => {
                    if (res.code) {
                        // 调用服务器登录接口
                        this.request({
                            url: "/api/game/wx-login",
                            method: "POST",
                            data: { code: res.code },
                            skipAuth: true // 跳过token验证
                        }).then((result: any) => {
                            if (result.code === 200 && result.data) {
                                const token = result.data.token;
                                this.setToken(token);
                                console.log("自动重新登录成功, token:", token);
                                resolve(token);
                            } else {
                                console.error("自动重新登录失败:", result.message);
                                reject(new Error(result.message || "登录失败"));
                            }
                        }).catch((error) => {
                            console.error("自动重新登录异常:", error);
                            reject(error);
                        });
                    } else {
                        reject(new Error("获取微信code失败"));
                    }
                },
                fail: (error) => {
                    reject(error);
                }
            });
        });
    }
    
    /**
     * 统一请求方法
     */
    public static request(options: {
        url: string;
        method?: string;
        data?: any;
        skipAuth?: boolean; // 是否跳过token验证（用于登录接口）
    }): Promise<any> {
        return new Promise((resolve, reject) => {
            const token = this.getToken();
            
            // 构建请求头
            const headers: any = {
                "Content-Type": "application/json"
            };
            
            // 添加token（登录接口除外）
            if (!options.skipAuth && token) {
                headers["X-Token"] = token;
            }
            
            // 发送请求
            Laya.HttpRequest.prototype.send = function(url: string, data?: any, method?: string, responseType?: string, headers?: any): void {
                // LayaAir的HttpRequest实现
            };
            
            const xhr = new Laya.HttpRequest();
            xhr.once(Laya.Event.COMPLETE, (result: any) => {
                try {
                    const response = JSON.parse(result);
                    
                    // 处理401错误 - token失效
                    if (response.code === 401 && !options.skipAuth) {
                        console.warn("Token失效，尝试自动重新登录...");
                        
                        // 清除旧token
                        this.clearToken();
                        
                        // 自动重新登录
                        this.wxLogin().then((newToken) => {
                            // 重新发送原请求
                            console.log("重新发送请求:", options.url);
                            options.skipAuth = false; // 恢复token验证
                            this.request(options).then(resolve).catch(reject);
                        }).catch((error) => {
                            console.error("自动重新登录失败，需要用户手动登录");
                            // 可以触发登录界面显示
                            reject({
                                code: 401,
                                message: "登录已过期，请重新登录",
                                needLogin: true
                            });
                        });
                    } else {
                        resolve(response);
                    }
                } catch (error) {
                    reject(error);
                }
            });
            
            xhr.once(Laya.Event.ERROR, (error) => {
                reject(error);
            });
            
            const url = this.baseUrl + options.url;
            const method = options.method || "GET";
            const data = options.data ? JSON.stringify(options.data) : null;
            
            xhr.send(url, data, method, "json", headers);
        });
    }
}
```

#### 使用示例

```typescript
// 获取用户游戏数据
HttpUtil.request({
    url: "/api/game/user-data",
    method: "GET"
}).then((result) => {
    if (result.code === 200) {
        console.log("获取数据成功:", result.data);
    }
}).catch((error) => {
    if (error.needLogin) {
        // 显示登录界面
        this.showLoginDialog();
    } else {
        console.error("请求失败:", error);
    }
});

// 保存用户游戏数据
HttpUtil.request({
    url: "/api/game/user-data",
    method: "POST",
    data: {
        playerInfo: {...},
        assistants: [...],
        challenges: [...]
    }
}).then((result) => {
    if (result.code === 200) {
        console.log("保存成功");
    }
});
```

### 方案2：在业务代码中处理 401

如果不想使用统一拦截器，可以在每个请求的回调中处理：

```typescript
function getUserData() {
    const token = HttpUtil.getToken();
    
    // 发送请求
    request({
        url: "/api/game/user-data",
        headers: { "X-Token": token }
    }).then((result) => {
        if (result.code === 200) {
            // 处理成功
        }
    }).catch((error) => {
        if (error.code === 401) {
            // Token失效，重新登录
            reLogin().then(() => {
                // 重新获取数据
                getUserData();
            });
        }
    });
}

function reLogin() {
    return new Promise((resolve, reject) => {
        wx.login({
            success: (res) => {
                // 调用登录接口
                login(res.code).then((token) => {
                    HttpUtil.setToken(token);
                    resolve(token);
                }).catch(reject);
            }
        });
    });
}
```

## 最佳实践

1. **统一处理**：使用统一的请求拦截器处理 token 失效
2. **自动重试**：token 失效后自动重新登录并重试原请求
3. **用户提示**：如果自动登录失败，提示用户手动登录
4. **Token 存储**：将 token 保存到本地存储，避免每次启动都重新登录

## 注意事项

1. **防止循环**：确保重新登录接口本身不触发 token 验证，避免死循环
2. **并发请求**：多个请求同时返回 401 时，只触发一次重新登录
3. **用户体验**：自动重新登录应该是静默的，不打断用户操作
4. **错误处理**：如果自动登录失败，应该给用户明确的提示

## 处理流程

```
客户端请求 → 服务器验证token → 
  ├─ token有效 → 继续处理请求 ✅
  └─ token失效 → 返回401错误 → 
      └─ 客户端自动重新登录 → 获取新token → 重试请求 ✅
```

