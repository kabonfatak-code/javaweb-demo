# BBS 部署说明

## 本地 MySQL

默认连接配置：

- URL: `jdbc:mysql://localhost:3306/bbs?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true`
- 用户: `root`
- 密码: `123456`

如果你的 MySQL 密码不同，可以在 Tomcat VM options 中设置：

```text
-Dbbs.db.url=jdbc:mysql://localhost:3306/bbs?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
-Dbbs.db.user=root
-Dbbs.db.password=你的密码
```

初始化数据库：

```powershell
mysql -uroot -p < database/schema.sql
```

应用启动时也会自动创建缺失的数据表，并写入默认管理员：

```text
admin / admin123
```

## 公网发布和 50 人访问

把 `target/demo-1.0-SNAPSHOT.war` 部署到公网服务器的 Tomcat 10.1：

1. 服务器安装 JDK 17+、Tomcat 10.1、MySQL 8。
2. 开放安全组或防火墙端口 `80` 或 `8080`。
3. 导入 `database/schema.sql`。
4. 配置 Tomcat VM options 中的 `bbs.db.*` 数据库参数。
5. 把 WAR 放进 Tomcat `webapps`，启动 Tomcat。

50 人并发访问的建议配置：

```xml
<Connector port="8080"
           protocol="HTTP/1.1"
           maxThreads="100"
           acceptCount="100"
           connectionTimeout="20000" />
```

MySQL 建议 `max_connections >= 100`。真实短信验证码需要接入阿里云短信、腾讯云短信等平台，把 `SmsCodeServlet` 中的模拟返回替换为短信平台发送接口即可。
