# IM
IM 是一个轻量级的即时通信服务端。
## 技术栈
* 使用protobuf序列化消息实体,自定义ByteBuf消息格式解决TCP粘包问题
* 提供SPI接口供第三方扩展登录实现，并使用到了工厂、建造者、单例设计模式
* 使用docker-compose容器化技术支持快速启动项目
* 选择Guice框架实现依赖注入，运用了JUC工具包，实现保存连接的容器和保证了消息顺序
## 功能
* 单聊：文字/文件
* 已发送/已送达/已读回执
* 支持集成第三方登录系统
* 方便水平扩展
* 提供客户端jar包

## 快速上手

### 准备工作
使用docker快速启动 IM 服务。

```
# detect if the docker environment is avaliable.
docker -v
```
```
# clone the repository
git clone git@github.com:yuanrw/IM.git
```

### 启动服务

```
cd IM/docker
docker-compose up
```

容器内有demo程序，自动启动多个客户端并且随机发送消息，启动成功后输出如下日志:

## 分布式部署
```
mvn clean package -DskipTests
```
在/target目录下生成 $SERVICE_NAME-$VERSION-bin.zip

### 环境
* java 8+
* mysql 5.7+
* rabbitmq
* redis

### 启动
按照**如下顺序**启动服务:
rest-web --> transfer -->connector

启动`rest-web`的步骤如下,`transfer`和`connector`类似.

#### rest-web
1. 解压

```
unzip rest-web-$VERSION-bin.zip
cd rest-web-$VERSION
```

4. 启动服务

```
java -jar rest-web-$VERSION.jar --spring.config.location=application.properties
```

#### transfer
```
java -jar -Dconfig=transfer.properties transfer-$VERSION.jar
```

#### connector
```
java -jar -Dconfig=connector.properties connector-$VERSION.jar
```

## Nginx配置
所有的服务都能够水平扩展，客户端和connector服务端需要保持长连接。
nginx可以如下配置:

```
stream {
	upstream backend {
        # connector services port
        server 127.0.0.1:9081         max_fails=3 fail_timeout=30s;
        server 127.0.0.1:19081			max_fails=3 fail_timeout=30s;
	}

    server {
        # to keep a persistent connection
        listen 9999 so_keepalive=on;
        proxy_timeout 1d;
        proxy_pass backend;
    }
}
```

## Login
IM含有一个非常简单的登录系统，可以直接使用。  
也支持以下两种登录方式。

```
java -jar rest-web-$VERSION.jar --spring.config.location=application.properties
```
### 自己的登录系统
1. 需要实现`com.yrw.im.rest.spi.UserSpi`中的接口

```
public interface UserSpi<T extends UserBase> {

    /**
     * get user by username and password, return user(id can not be null)
     * if username and password are right, else return null.
     * <p>
     * be sure that your password has been properly encrypted
     *
     * @param username
     * @param pwd
     * @return
     */
    T getUser(String username, String pwd);

    /**
     * get user by id, if id not exist then return null.
     *
     * @param id
     * @return
     */
    T getById(String id);
}
```

3. 打包

```
mvn clean package -DskipTests
```
