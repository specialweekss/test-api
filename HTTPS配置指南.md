# 云服务器 HTTPS 配置指南

## 方案概述

在云服务器上使用 HTTPS 访问 8080 端口，推荐使用 **Nginx 反向代理** 方案，这是最常用且最稳定的方式。

## 方案一：使用 Nginx 反向代理（推荐）

### 架构说明
```
客户端 (HTTPS 443) → Nginx (反向代理) → Spring Boot (HTTP 8080)
```

### 步骤 1: 安装 Nginx

#### CentOS/RHEL:
```bash
sudo yum install nginx -y
sudo systemctl start nginx
sudo systemctl enable nginx
```

#### Ubuntu/Debian:
```bash
sudo apt-get update
sudo apt-get install nginx -y
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 步骤 2: 安装 Certbot（用于获取 Let's Encrypt 免费证书）

#### CentOS/RHEL:
```bash
sudo yum install certbot python3-certbot-nginx -y
```

#### Ubuntu/Debian:
```bash
sudo apt-get install certbot python3-certbot-nginx -y
```

### 步骤 3: 配置域名解析

在域名服务商处添加 A 记录，将域名指向云服务器 IP：
```
类型: A
主机记录: @ 或 www
记录值: 你的云服务器公网IP
TTL: 600
```

### 步骤 4: 配置 Nginx 反向代理

创建 Nginx 配置文件：

```bash
sudo vi /etc/nginx/conf.d/test-api.conf
```

添加以下配置（**先使用 HTTP，稍后配置 HTTPS**）：

```nginx
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;

    # 重定向到 HTTPS（配置证书后启用）
    # return 301 https://$server_name$request_uri;

    # 临时使用 HTTP（配置证书前）
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket 支持（如果需要）
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

测试配置并重载：

```bash
# 测试配置语法
sudo nginx -t

# 重载配置
sudo systemctl reload nginx
```

### 步骤 5: 配置防火墙

```bash
# CentOS/RHEL (firewalld)
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload

# Ubuntu/Debian (ufw)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw reload
```

### 步骤 6: 获取 SSL 证书（Let's Encrypt 免费证书）

```bash
# 申请证书（替换为你的域名）
sudo certbot --nginx -d your-domain.com -d www.your-domain.com
```

Certbot 会自动： 
1. 验证域名所有权
2. 获取 SSL 证书
3. 配置 Nginx 使用 HTTPS
4. 设置自动续期

### 步骤 7: 验证 HTTPS 配置

Certbot 会自动修改 Nginx 配置，最终配置类似：

```nginx
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;

    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    # SSL 配置（Certbot 会自动添加）
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
```

### 步骤 8: 测试 HTTPS 访问

```bash
# 测试 HTTP 自动跳转到 HTTPS
curl -I http://your-domain.com

# 测试 HTTPS 访问
curl -I https://your-domain.com

# 在浏览器中访问
https://your-domain.com/api/game/user-data?userId=123456
```

### 步骤 9: 设置证书自动续期

Let's Encrypt 证书有效期为 90 天，Certbot 会自动设置续期任务：

```bash
# 查看续期任务
sudo systemctl status certbot.timer

# 手动测试续期
sudo certbot renew --dry-run
```

---

## 方案二：Spring Boot 直接配置 HTTPS（不推荐）

如果不想使用 Nginx，可以直接在 Spring Boot 中配置 HTTPS，但**不推荐**，因为：
- 需要手动管理证书
- 无法使用 80 端口自动跳转
- 证书续期需要重启应用

### 步骤 1: 获取 SSL 证书

使用 Certbot 获取证书（不配置 Nginx）：

```bash
sudo certbot certonly --standalone -d your-domain.com
```

证书会保存在：`/etc/letsencrypt/live/your-domain.com/`

### 步骤 2: 转换证书格式

Spring Boot 需要 PKCS12 格式的证书：

```bash
# 安装 openssl（如果没有）
sudo yum install openssl -y
# 或
sudo apt-get install openssl -y

# 转换为 PKCS12 格式
sudo openssl pkcs12 -export \
    -in /etc/letsencrypt/live/your-domain.com/fullchain.pem \
    -inkey /etc/letsencrypt/live/your-domain.com/privkey.pem \
    -out /opt/test-api/keystore.p12 \
    -name tomcat \
    -CAfile /etc/letsencrypt/live/your-domain.com/chain.pem \
    -caname root

# 设置密码（记住这个密码，配置文件中需要）
# 设置文件权限
sudo chmod 600 /opt/test-api/keystore.p12
sudo chown root:root /opt/test-api/keystore.p12
```

### 步骤 3: 配置 Spring Boot

在 `application-prod.properties` 中添加：

```properties
# HTTPS 配置
server.port=8443
server.ssl.key-store=/opt/test-api/keystore.p12
server.ssl.key-store-password=你设置的密码
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat

# HTTP 自动跳转到 HTTPS（需要额外配置）
```

### 步骤 4: 配置 HTTP 到 HTTPS 重定向

需要添加配置类，但这种方式比较复杂，**强烈推荐使用 Nginx 方案**。

---

## 方案三：使用云服务商 SSL 证书

如果使用阿里云、腾讯云等云服务商，可以使用他们提供的免费 SSL 证书：

### 阿里云 SSL 证书

1. 在阿里云控制台申请免费 SSL 证书
2. 下载证书（Nginx 版本）
3. 上传到服务器并配置 Nginx

### 腾讯云 SSL 证书

1. 在腾讯云控制台申请免费 SSL 证书
2. 下载证书（Nginx 版本）
3. 上传到服务器并配置 Nginx

---

## 完整 Nginx 配置示例

```nginx
# HTTP 重定向到 HTTPS
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    return 301 https://$server_name$request_uri;
}

# HTTPS 配置
server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;

    # SSL 证书
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    # SSL 配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    # 安全头
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # 反向代理到 Spring Boot
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # 缓冲设置
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
    }

    # 健康检查（可选）
    location /health {
        proxy_pass http://localhost:8080/actuator/health;
        access_log off;
    }
}
```

---

## 常见问题

### Q1: 证书申请失败

**原因**：
- 域名解析未生效
- 80 端口被占用
- 防火墙阻止访问

**解决**：
```bash
# 检查域名解析
nslookup your-domain.com

# 检查 80 端口
sudo netstat -tlnp | grep :80

# 检查防火墙
sudo firewall-cmd --list-all
```

### Q2: HTTPS 访问 502 Bad Gateway

**原因**：Spring Boot 应用未运行或端口不对

**解决**：
```bash
# 检查应用是否运行
ps aux | grep test-api

# 检查 8080 端口
sudo netstat -tlnp | grep :8080

# 重启应用
cd /opt/test-api
./restart.sh
```

### Q3: 证书续期失败

**解决**：
```bash
# 手动续期
sudo certbot renew

# 查看续期日志
sudo tail -f /var/log/letsencrypt/letsencrypt.log
```

### Q4: 需要同时支持 HTTP 和 HTTPS

修改 Nginx 配置，不重定向 HTTP：

```nginx
server {
    listen 80;
    listen 443 ssl http2;
    server_name your-domain.com;
    
    # SSL 配置（仅 HTTPS 需要）
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    location / {
        proxy_pass http://localhost:8080;
        # ... 其他配置
    }
}
```

---

## 安全建议

1. **强制使用 HTTPS**：配置 HTTP 自动跳转到 HTTPS
2. **使用 HSTS**：添加 `Strict-Transport-Security` 头
3. **定期更新证书**：确保证书自动续期正常工作
4. **隐藏服务器信息**：在 Nginx 中隐藏版本信息
5. **限制请求大小**：防止大文件上传攻击

---

## 快速部署命令总结

```bash
# 1. 安装 Nginx
sudo yum install nginx -y  # CentOS
# 或
sudo apt-get install nginx -y  # Ubuntu

# 2. 安装 Certbot
sudo yum install certbot python3-certbot-nginx -y  # CentOS
# 或
sudo apt-get install certbot python3-certbot-nginx -y  # Ubuntu

# 3. 配置 Nginx（创建配置文件）
sudo vi /etc/nginx/conf.d/test-api.conf

# 4. 测试并重载 Nginx
sudo nginx -t
sudo systemctl reload nginx

# 5. 开放防火墙端口
sudo firewall-cmd --permanent --add-service=http --add-service=https
sudo firewall-cmd --reload

# 6. 申请 SSL 证书
sudo certbot --nginx -d your-domain.com -d www.your-domain.com

# 7. 验证 HTTPS
curl -I https://your-domain.com
```

完成以上步骤后，就可以使用 `https://your-domain.com` 访问你的 API 了！

---

## 注意事项

1. **域名必须解析**：申请证书前确保域名已正确解析到服务器 IP
2. **80 端口必须开放**：Let's Encrypt 需要通过 80 端口验证域名
3. **证书自动续期**：Certbot 会自动设置定时任务，无需手动操作
4. **云服务器安全组**：确保在云平台控制台开放 80 和 443 端口

