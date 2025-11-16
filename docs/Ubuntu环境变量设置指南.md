# Ubuntu 环境变量设置指南

本文档介绍如何在 Ubuntu 系统上持久化设置 `WECHAT_APPID` 和 `WECHAT_SECRET` 环境变量，确保这些变量在系统重启后仍然有效。

---

## 目录

- [一、方法一：使用 ~/.bashrc（推荐用于用户环境）](#一方法一使用-bashrc推荐用于用户环境)
- [二、方法二：使用 ~/.profile](#二方法二使用-profile)
- [三、方法三：使用 /etc/environment（系统级环境变量）](#三方法三使用-etcenvironment系统级环境变量)
- [四、方法四：使用 /etc/profile.d/（系统级，推荐）](#四方法四使用-etcprofiled系统级推荐)
- [五、方法五：使用 Systemd 服务文件（推荐用于服务）](#五方法五使用-systemd-服务文件推荐用于服务)
- [六、验证环境变量](#六验证环境变量)
- [七、常见问题](#七常见问题)
- [八、推荐方案](#八推荐方案)

---

## 一、方法一：使用 ~/.bashrc（推荐用于用户环境）

### 适用场景

- 只对当前用户有效
- 适用于交互式 shell（SSH 登录、终端）
- 每次打开新的终端窗口时自动加载

### 设置步骤

#### 1. 编辑 ~/.bashrc 文件

```bash
# 使用 nano 编辑器（推荐新手）
nano ~/.bashrc

# 或使用 vim 编辑器
vim ~/.bashrc
```

#### 2. 在文件末尾添加环境变量

在文件末尾添加以下内容：

```bash
# 微信小程序配置
export WECHAT_APPID=wx1234567890abcdef
export WECHAT_SECRET=abcdef1234567890abcdef1234567890ab
```

**注意**：请将 `wx1234567890abcdef` 和 `abcdef1234567890abcdef1234567890ab` 替换为你的实际 AppID 和 AppSecret。

#### 3. 保存并退出

**nano 编辑器：**
- 按 `Ctrl + O` 保存
- 按 `Enter` 确认文件名
- 按 `Ctrl + X` 退出

**vim 编辑器：**
- 按 `i` 进入插入模式
- 编辑完成后按 `Esc` 退出插入模式
- 输入 `:wq` 保存并退出

#### 4. 使配置生效

```bash
# 重新加载 ~/.bashrc
source ~/.bashrc

# 或重新打开终端窗口
```

#### 5. 验证环境变量

```bash
echo $WECHAT_APPID
echo $WECHAT_SECRET
```

---

## 二、方法二：使用 ~/.profile

### 适用场景

- 只对当前用户有效
- 适用于所有 shell（bash、sh、dash 等）
- 登录时自动加载

### 设置步骤

#### 1. 编辑 ~/.profile 文件

```bash
nano ~/.profile
# 或
vim ~/.profile
```

#### 2. 在文件末尾添加环境变量

```bash
# 微信小程序配置
export WECHAT_APPID=wx1234567890abcdef
export WECHAT_SECRET=abcdef1234567890abcdef1234567890ab
```

#### 3. 保存并退出

参考方法一的保存步骤。

#### 4. 使配置生效

```bash
# 重新加载 ~/.profile
source ~/.profile

# 或重新登录
```

#### 5. 验证环境变量

```bash
echo $WECHAT_APPID
echo $WECHAT_SECRET
```

---

## 三、方法三：使用 /etc/environment（系统级环境变量）

### 适用场景

- 对所有用户有效
- 系统级环境变量
- 不需要 export 关键字
- **需要 root 权限**

### 设置步骤

#### 1. 编辑 /etc/environment 文件

```bash
sudo nano /etc/environment
# 或
sudo vim /etc/environment
```

#### 2. 添加环境变量

文件内容格式（每行一个变量，不需要 export）：

```
PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
WECHAT_APPID=wx1234567890abcdef
WECHAT_SECRET=abcdef1234567890abcdef1234567890ab
```

**注意**：
- 不需要使用 `export` 关键字
- 值不需要用引号（除非值中包含空格）
- 每行一个变量

#### 3. 保存并退出

参考方法一的保存步骤。

#### 4. 使配置生效

```bash
# 重新登录或重启系统
# 或使用以下命令（需要 root 权限）
sudo systemctl restart systemd-logind
```

#### 5. 验证环境变量

```bash
echo $WECHAT_APPID
echo $WECHAT_SECRET
```

---

## 四、方法四：使用 /etc/profile.d/（系统级，推荐）

### 适用场景

- 对所有用户有效
- 系统级环境变量
- 便于管理多个环境变量文件
- **需要 root 权限**

### 设置步骤

#### 1. 创建环境变量文件

```bash
# 创建环境变量文件
sudo nano /etc/profile.d/wechat-env.sh
# 或
sudo vim /etc/profile.d/wechat-env.sh
```

#### 2. 添加环境变量

在文件中添加以下内容：

```bash
#!/bin/bash
# 微信小程序配置
export WECHAT_APPID=wx1234567890abcdef
export WECHAT_SECRET=abcdef1234567890abcdef1234567890ab
```

#### 3. 设置文件权限

```bash
# 设置可执行权限
sudo chmod +x /etc/profile.d/wechat-env.sh
```

#### 4. 使配置生效

```bash
# 重新加载配置
source /etc/profile

# 或重新登录
```

#### 5. 验证环境变量

```bash
echo $WECHAT_APPID
echo $WECHAT_SECRET
```

---

## 五、方法五：使用 Systemd 服务文件（推荐用于服务）

### 适用场景

- 只对特定服务有效
- 适用于通过 systemd 管理的服务
- 不影响其他进程
- **推荐用于生产环境**

### 设置步骤

#### 1. 编辑 Systemd 服务文件

```bash
sudo nano /etc/systemd/system/test-api.service
# 或
sudo vim /etc/systemd/system/test-api.service
```

#### 2. 在 [Service] 部分添加环境变量

服务文件示例：

```ini
[Unit]
Description=Test API Application
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/test-api

# 设置微信环境变量（必须）
Environment="WECHAT_APPID=wx1234567890abcdef"
Environment="WECHAT_SECRET=abcdef1234567890abcdef1234567890ab"

ExecStart=/usr/bin/java -Xms512m -Xmx1024m -XX:+UseG1GC \
    -jar /opt/test-api/test-api-0.0.1-SNAPSHOT.jar \
    --spring.profiles.active=prod

ExecStop=/bin/kill -15 $MAINPID
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

#### 3. 重新加载 Systemd 配置

```bash
# 重新加载 systemd 配置
sudo systemctl daemon-reload
```

#### 4. 重启服务

```bash
# 重启服务使环境变量生效
sudo systemctl restart test-api
```

#### 5. 验证环境变量

```bash
# 查看服务环境变量
sudo systemctl show test-api | grep Environment

# 查看服务状态
sudo systemctl status test-api
```

---

## 六、验证环境变量

### 验证方法

#### 1. 使用 echo 命令

```bash
# 查看单个环境变量
echo $WECHAT_APPID
echo $WECHAT_SECRET

# 查看所有微信相关环境变量
env | grep WECHAT
```

#### 2. 使用 printenv 命令

```bash
# 查看单个环境变量
printenv WECHAT_APPID
printenv WECHAT_SECRET

# 查看所有环境变量
printenv | grep WECHAT
```

#### 3. 在 Java 应用中验证

启动应用后，查看日志输出。如果配置正确，会在日志中看到：

```
当前使用的微信配置 - AppID: wx1234567890abcdef, AppSecret: abcdef1234567890abcdef1234567890ab
```

#### 4. 在启动脚本中验证

在启动脚本中添加验证：

```bash
#!/bin/bash

# 检查环境变量
if [ -z "$WECHAT_APPID" ] || [ -z "$WECHAT_SECRET" ]; then
    echo "Error: WECHAT_APPID or WECHAT_SECRET environment variable is not set"
    exit 1
fi

echo "WECHAT_APPID: ${WECHAT_APPID:0:10}..."  # 只显示前10个字符
# 继续启动应用...
```

---

## 七、常见问题

### Q1: 设置环境变量后，应用仍然读取不到？

**原因**：
- 环境变量设置后，已运行的进程不会自动获取新的环境变量
- 不同的设置方法适用于不同的场景

**解决方案**：

1. **如果使用 ~/.bashrc 或 ~/.profile**：
   ```bash
   # 重新加载配置
   source ~/.bashrc
   # 或
   source ~/.profile
   
   # 重新启动应用
   ```

2. **如果使用 /etc/environment**：
   ```bash
   # 需要重新登录或重启系统
   sudo reboot
   ```

3. **如果使用 Systemd 服务**：
   ```bash
   # 重新加载并重启服务
   sudo systemctl daemon-reload
   sudo systemctl restart test-api
   ```

4. **检查环境变量是否在正确的上下文中生效**：
   - 交互式 shell：使用 ~/.bashrc
   - 登录 shell：使用 ~/.profile
   - 系统服务：使用 Systemd 服务文件

---

### Q2: 如何删除环境变量？

#### 删除用户级环境变量

**从 ~/.bashrc 删除：**
```bash
# 编辑文件
nano ~/.bashrc

# 删除或注释掉相关行
# export WECHAT_APPID=...
# export WECHAT_SECRET=...

# 重新加载
source ~/.bashrc
```

**从 ~/.profile 删除：**
```bash
# 编辑文件
nano ~/.profile

# 删除或注释掉相关行
# 重新加载
source ~/.profile
```

#### 删除系统级环境变量

**从 /etc/environment 删除：**
```bash
# 编辑文件（需要 root 权限）
sudo nano /etc/environment

# 删除相关行
# 重新登录
```

**从 /etc/profile.d/ 删除：**
```bash
# 删除文件
sudo rm /etc/profile.d/wechat-env.sh

# 重新加载
source /etc/profile
```

**从 Systemd 服务文件删除：**
```bash
# 编辑服务文件
sudo nano /etc/systemd/system/test-api.service

# 删除 Environment 行
# 重新加载并重启
sudo systemctl daemon-reload
sudo systemctl restart test-api
```

---

### Q3: 用户变量和系统变量有什么区别？

| 特性 | 用户变量 | 系统变量 |
|------|---------|---------|
| **作用范围** | 只对当前用户有效 | 对所有用户有效 |
| **设置位置** | ~/.bashrc, ~/.profile | /etc/environment, /etc/profile.d/ |
| **权限要求** | 普通用户 | root 权限 |
| **推荐场景** | 开发环境、个人使用 | 生产环境、多用户系统 |

---

### Q4: 环境变量设置后多久生效？

| 设置方法 | 生效时间 |
|---------|---------|
| ~/.bashrc | 重新加载配置或打开新终端 |
| ~/.profile | 重新加载配置或重新登录 |
| /etc/environment | 重新登录或重启系统 |
| /etc/profile.d/ | 重新加载配置或重新登录 |
| Systemd 服务文件 | 重新加载并重启服务 |

---

### Q5: 如何查看所有环境变量？

```bash
# 查看所有环境变量
env

# 或
printenv

# 查看包含特定关键字的环境变量
env | grep WECHAT

# 查看系统级环境变量
sudo cat /etc/environment

# 查看用户级环境变量
cat ~/.bashrc | grep export
```

---

### Q6: 环境变量值包含特殊字符怎么办？

如果环境变量值包含空格、引号等特殊字符，需要使用引号：

```bash
# 值包含空格
export WECHAT_APPID="wx 1234567890abcdef"

# 值包含引号
export WECHAT_APPID='wx"1234567890abcdef"'

# 值包含 $ 符号（需要转义）
export WECHAT_APPID='wx\$1234567890abcdef'
```

---

### Q7: 如何在不同用户之间共享环境变量？

**方法1：使用系统级环境变量文件**

```bash
# 在 /etc/profile.d/ 创建共享文件
sudo nano /etc/profile.d/shared-env.sh

# 添加环境变量
export WECHAT_APPID=wx1234567890abcdef
export WECHAT_SECRET=abcdef1234567890abcdef1234567890ab

# 设置权限
sudo chmod +x /etc/profile.d/shared-env.sh
```

**方法2：使用 /etc/environment**

```bash
# 编辑系统环境变量文件
sudo nano /etc/environment

# 添加环境变量（不需要 export）
WECHAT_APPID=wx1234567890abcdef
WECHAT_SECRET=abcdef1234567890abcdef1234567890ab
```

---

## 八、推荐方案

### 开发环境

**推荐使用：~/.bashrc**

- 只对当前用户有效
- 每次打开终端自动加载
- 便于修改和测试

**设置步骤：**
```bash
# 编辑 ~/.bashrc
nano ~/.bashrc

# 添加环境变量
export WECHAT_APPID=wx1234567890abcdef
export WECHAT_SECRET=abcdef1234567890abcdef1234567890ab

# 重新加载
source ~/.bashrc
```

---

### 生产环境（使用启动脚本）

**推荐使用：/etc/profile.d/ 或 /etc/environment**

- 对所有用户有效
- 系统重启后仍然有效
- 便于统一管理

**设置步骤：**
```bash
# 创建环境变量文件
sudo nano /etc/profile.d/wechat-env.sh

# 添加环境变量
export WECHAT_APPID=wx1234567890abcdef
export WECHAT_SECRET=abcdef1234567890abcdef1234567890ab

# 设置权限
sudo chmod +x /etc/profile.d/wechat-env.sh

# 重新加载
source /etc/profile
```

---

### 生产环境（使用 Systemd 服务）

**推荐使用：Systemd 服务文件**

- 只对特定服务有效
- 不影响其他进程
- 便于服务管理

**设置步骤：**
```bash
# 编辑服务文件
sudo nano /etc/systemd/system/test-api.service

# 在 [Service] 部分添加
Environment="WECHAT_APPID=wx1234567890abcdef"
Environment="WECHAT_SECRET=abcdef1234567890abcdef1234567890ab"

# 重新加载并重启
sudo systemctl daemon-reload
sudo systemctl restart test-api
```

---

## 快速设置命令

### 用户级环境变量（~/.bashrc）

```bash
# 一键设置（替换为实际值）
echo 'export WECHAT_APPID=wx1234567890abcdef' >> ~/.bashrc
echo 'export WECHAT_SECRET=abcdef1234567890abcdef1234567890ab' >> ~/.bashrc
source ~/.bashrc
```

### 系统级环境变量（/etc/profile.d/）

```bash
# 一键设置（需要 root 权限，替换为实际值）
sudo bash -c 'echo "export WECHAT_APPID=wx1234567890abcdef" > /etc/profile.d/wechat-env.sh'
sudo bash -c 'echo "export WECHAT_SECRET=abcdef1234567890abcdef1234567890ab" >> /etc/profile.d/wechat-env.sh'
sudo chmod +x /etc/profile.d/wechat-env.sh
source /etc/profile
```

---

## 相关文档

- [云服务器运维文档](./云服务器运维文档.md)
- [部署指南](../部署指南.md)
- [Windows 环境变量设置指南](./Windows环境变量设置指南.md)
- [微信配置说明](./微信配置说明.md)

---

**最后更新**: 2024-01-01

