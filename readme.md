# 谢滨荣

***专业：软件工程 爱好：计算机、音乐、游戏***

## 完成的Redis相关功能

### 1. 安全配置文件 (`SecurityConfig.java`)
- **功能**：配置Spring Security安全策略
- **主要实现**：
    - 关闭CSRF保护
    - 配置跨域请求处理
    - 放行Redis相关接口的权限验证
    - 其他接口需认证（可根据生产环境调整）

### 2. Redis配置文件 (`RedisConfig.java`)
- **功能**：配置Redis连接和序列化策略
- **主要实现**：
    - 创建并配置RedisTemplate实例
    - 配置键的序列化器为StringRedisSerializer
    - 配置值的序列化器为GenericJackson2JsonRedisSerializer，支持JSON格式存储对象

### 3. Redis控制器 (`RedisController.java`)
- **功能**：提供Redis操作的HTTP接口
- **主要实现**：
    - `/redis/set`：设置带过期时间的字符串值
    - `/redis/get/{key}`：获取指定键的值
    - `/redis/hash`：设置哈希字段的值

### 4. Redis存储模板 (`RedisStorageTemplate.java`)
- **功能**：提供Redis各种数据类型的操作方法
- **主要实现**：
    - **字符串操作**：set、get、delete、expire等
    - **哈希操作**：hSet、hGet、hGetAll、hDelete等
    - **列表操作**：lPush、rPush、lRange、lPop、rPop等
    - **集合操作**：sAdd、sMembers、sIsMember、sRemove等
    - **有序集合操作**：zAdd、zRange、zRangeByScore、zScore等
- **特点**：支持泛型，自动处理序列化和反序列化