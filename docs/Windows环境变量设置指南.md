# Windows 环境变量设置指南

## 方法一：通过系统设置界面（推荐，永久生效）

### 步骤1：打开环境变量设置

**Windows 10/11:**
1. 右键点击 **"此电脑"** 或 **"我的电脑"**
2. 选择 **"属性"**
3. 点击 **"高级系统设置"**
4. 在 **"系统属性"** 窗口中，点击 **"环境变量"** 按钮

**或者使用快捷键：**
- 按 `Win + R` 打开运行对话框
- 输入 `sysdm.cpl` 并回车
- 点击 **"高级"** 选项卡
- 点击 **"环境变量"** 按钮

### 步骤2：添加用户环境变量（推荐）

在 **"用户变量"** 区域（上半部分）：

1. 点击 **"新建"** 按钮
2. 变量名：`WECHAT_APPID`
3. 变量值：`wx1234567890abcdef`（替换为你的实际AppID）
4. 点击 **"确定"**

5. 再次点击 **"新建"** 按钮
6. 变量名：`WECHAT_SECRET`
7. 变量值：`abcdef1234567890abcdef1234567890ab`（替换为你的实际AppSecret）
8. 点击 **"确定"**

9. 点击 **"确定"** 关闭所有窗口

### 步骤3：验证环境变量

**方法1：使用命令提示符（CMD）**
```cmd
echo %WECHAT_APPID%
echo %WECHAT_SECRET%
```

**方法2：使用 PowerShell**
```powershell
$env:WECHAT_APPID
$env:WECHAT_SECRET
```

**方法3：使用系统信息**
```cmd
set | findstr WECHAT
```

### 步骤4：重启应用或重新打开终端

**重要**：设置环境变量后，需要：
- 关闭所有已打开的 CMD、PowerShell 或 IDE 窗口
- 重新打开终端或 IDE
- 重新启动应用

环境变量才会生效。

---

## 方法二：通过命令提示符（临时生效）

### 设置环境变量（当前会话有效）

打开 **命令提示符（CMD）** 或 **PowerShell**：

**CMD:**
```cmd
set WECHAT_APPID=wx1234567890abcdef
set WECHAT_SECRET=abcdef1234567890abcdef1234567890ab
```

**PowerShell:**
```powershell
$env:WECHAT_APPID="wx1234567890abcdef"
$env:WECHAT_SECRET="abcdef1234567890abcdef1234567890ab"
```

**注意**：这种方式只在当前终端会话中有效，关闭终端后失效。

---

## 方法三：通过 PowerShell 永久设置（推荐用于脚本）

### 使用 PowerShell 设置用户环境变量

打开 **PowerShell（管理员权限）**：

```powershell
# 设置 WECHAT_APPID
[System.Environment]::SetEnvironmentVariable("WECHAT_APPID", "wx1234567890abcdef", "User")

# 设置 WECHAT_SECRET
[System.Environment]::SetEnvironmentVariable("WECHAT_SECRET", "abcdef1234567890abcdef1234567890ab", "User")
```

**验证：**
```powershell
[System.Environment]::GetEnvironmentVariable("WECHAT_APPID", "User")
[System.Environment]::GetEnvironmentVariable("WECHAT_SECRET", "User")
```

**注意**：设置后需要重新打开终端或重启应用才能生效。

---

## 方法四：通过注册表（高级用户）

### 使用注册表编辑器

1. 按 `Win + R`，输入 `regedit` 并回车
2. 导航到：`HKEY_CURRENT_USER\Environment`
3. 右键点击空白处，选择 **"新建"** → **"字符串值"**
4. 名称：`WECHAT_APPID`，值：`wx1234567890abcdef`
5. 再次创建：名称：`WECHAT_SECRET`，值：`abcdef1234567890abcdef1234567890ab`
6. 关闭注册表编辑器
7. **重启计算机**或注销重新登录

**警告**：修改注册表有风险，请谨慎操作。

---

## 方法五：使用批处理脚本（.bat）

### 创建设置脚本

创建 `set-env.bat` 文件：

```batch
@echo off
echo 设置微信环境变量...

setx WECHAT_APPID "wx1234567890abcdef"
setx WECHAT_SECRET "abcdef1234567890abcdef1234567890ab"

echo.
echo 环境变量设置完成！
echo 请重新打开命令提示符或重启应用以使环境变量生效。
pause
```

**使用方法：**
1. 编辑 `set-env.bat`，替换为实际的 AppID 和 AppSecret
2. 右键点击文件，选择 **"以管理员身份运行"**
3. 重新打开终端或重启应用

**注意**：`setx` 命令设置的环境变量需要重新打开终端才能生效。

---

## 验证环境变量是否生效

### 在 Java 应用中验证

启动应用后，查看日志输出。如果配置正确，会在 debug 日志中看到：

```
当前使用的微信配置 - AppID: wx1234567890abcdef, AppSecret: abcdef1234567890abcdef1234567890ab
```

### 在终端中验证

**CMD:**
```cmd
echo %WECHAT_APPID%
echo %WECHAT_SECRET%
```

**PowerShell:**
```powershell
$env:WECHAT_APPID
$env:WECHAT_SECRET
```

---

## 常见问题

### Q1: 设置环境变量后，应用仍然读取不到？

**原因**：环境变量设置后，已运行的进程不会自动获取新的环境变量。

**解决**：
1. 关闭所有已打开的 CMD、PowerShell、IDE 窗口
2. 重新打开终端或 IDE
3. 重新启动应用

### Q2: 如何删除环境变量？

**方法1：通过系统设置**
1. 打开环境变量设置（见方法一）
2. 选择要删除的变量
3. 点击 **"删除"** 按钮

**方法2：通过 PowerShell**
```powershell
[System.Environment]::SetEnvironmentVariable("WECHAT_APPID", $null, "User")
[System.Environment]::SetEnvironmentVariable("WECHAT_SECRET", $null, "User")
```

### Q3: 用户变量和系统变量有什么区别？

- **用户变量**：只对当前登录用户有效，推荐使用
- **系统变量**：对所有用户有效，需要管理员权限

### Q4: 环境变量设置后多久生效？

- **用户变量**：需要重新打开终端或重启应用
- **系统变量**：需要注销重新登录或重启计算机

### Q5: 如何查看所有环境变量？

**CMD:**
```cmd
set
```

**PowerShell:**
```powershell
Get-ChildItem Env:
```

---

## 推荐方案

### 开发环境

使用 **方法一（系统设置界面）** 设置用户环境变量，永久生效，方便管理。

### 生产环境

使用 **方法三（PowerShell）** 或通过系统管理工具设置，确保环境变量持久化。

---

## 相关文档

- [微信配置说明](./微信配置说明.md)
- [启动脚本使用说明](./启动脚本使用说明.md)

