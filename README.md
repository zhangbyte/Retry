# Retry
## 简介
Retry是配合[Kepler](https://github.com/Kepler-Framework/Kepler-All)（RPC框架）实现客户端和服务端事务最终一致性的组件
**试用场景**：客户端A调用服务端B，若A中事务执行成功，保证调用B也一定成功，则可通过Retry保证最终一致性
**客户端**：Retry对远程调用拦截，在其执行失败后对本次调用进行持久化，不向外抛异常（即默认远程调用成功）。Retry定时轮询client表，对远程调用进行重试
**服务端**：Retry对远程调用拦截，在执行方法前处理客户端发送的uuid，保证操作的幂等性

## 快速开始
#### DEMO
https://github.com/zhangbyte/RetryKepler
#### 依赖
Kepler，Spring，Mybatis
#### MAVEN 配置
```xml
<dependency>
	<groupId>com.retry</groupId>
	<artifactId>retry</artifactId>
	<version>1.0</version>
</dependency>
```
#### spring xml引入retry
1. client
`<import resource="classpath:retry-client.xml" />`
2. server
`<import resource="classpath:retry-server.xml" />`
#### 配置retry数据源（以及其他参数）
```profile
client.db.driverClass=com.mysql.jdbc.Driver
client.db.jdbcUrl=jdbc:mysql://127.0.0.1:3306/retry?useUnicode=true&characterEncoding=utf8&useSSL=true
client.db.user=retry
client.db.password=retry
client.db.table=client

server.db.driverClass=com.mysql.jdbc.Driver
server.db.jdbcUrl=jdbc:mysql://127.0.0.1:3306/retry?useUnicode=true&characterEncoding=utf8&useSSL=true
server.db.user=retry
server.db.password=retry
server.db.table=server
```
client表结构
```sql
CREATE TABLE `client` (
  `uuid` varchar(36) NOT NULL,
  `interfc` varchar(100) NOT NULL,
  `method` varchar(100) NOT NULL,
  `args` varbinary(60000) DEFAULT NULL,
  `args_str` varchar(255) DEFAULT NULL,
  `times` bigint(20) NOT NULL DEFAULT '0' COMMENT '重试次数',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `state` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态 0：待执行 1：执行成功',
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```
server表结构
```sql
CREATE TABLE `server` (
  `uuid` varchar(36) NOT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```
#### 在接口方法上增加@Retryable注解
```java
@Service(version = "0.0.1")
public interface ServiceB {
    @Retryable
    void save(String data);
}
```
