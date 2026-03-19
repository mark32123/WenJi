# 2026-03-19

## 一、统一使用 sessionId，移除冗余的 chatId 字段

### 问题描述
项目中同时存在 `chatId` 和 `sessionId` 两个字段，功能重复，容易混淆。

### 修改文件

#### 1. AIChatMessage.java
- 移除了 `chatId` 字段，只保留 `sessionId`

#### 2. ChatController.java
- 移除所有 `.chatId()` 调用，统一使用 sessionId
- 更新日志消息使用 sessionId

#### 3. AIChatMessageService.java & AIChatMessageServiceImpl.java
- 方法 `getMessagesByChatId` → `getMessagesBySessionId`
- 所有 chatId 引用改为 sessionId

#### 4. AIChatMessageMapper.xml
- SQL 中 `chat_id` 字段改为 `session_id`
- `WHERE chat_id = #{sessionId}` → `WHERE session_id = #{sessionId}`
- `INSERT INTO ai_chat_message (chat_id, ...)` → `INSERT INTO ai_chat_message (session_id, ...)`
- `INNER JOIN ai_chat_session s ON m.chat_id = s.session_id` → `INNER JOIN ai_chat_session s ON m.session_id = s.session_id`

#### 5. ChatHistoryController.java
- 参数名统一：chatId → sessionId
- 移除未使用的 extractNumericChatId 方法
- 更新日志输出

#### 6. RedisChatHistoryUtils.java
- 参数名统一：chatId → sessionId

#### 7. 数据库执行SQL
```sql
ALTER TABLE ai_chat_message DROP COLUMN chat_id;
```

### 注意事项
- 前端生成的 chatId 参数名保持不变，后端接收后存入 sessionId 字段
- 功能完全不受影响，只是命名统一

---

## 二、Redis 常量统一

### 问题描述
Redis 常量分散定义，key 格式不够清晰，存在重复（如 `chat:history:chat:9`）

### 修改文件

#### 1. RedisChatHistoryUtils.java
- 直接引用 `RedisConstants.AI_CHAT_HISTORY_PREFIX` 替代本地常量
- 直接引用 `RedisConstants.AI_CHAT_HISTORY_EXPIRE` 替代本地常量

#### 2. RedisConstants.java
- `AI_CHAT_HISTORY_PREFIX = "chat:history:"` - AI对话历史前缀
- `AI_CHAT_HISTORY_EXPIRE = 30` - AI对话历史过期时间（秒）

### 优化效果
- 统一在 RedisConstants 中管理常量
- 减少代码重复
- key 格式更清晰

---

## 三、UserServiceImpl 代码优化

### 问题描述
获取 Redis loginKey 的逻辑存在重复代码

### 修改文件

#### UserServiceImpl.java
- 添加静态导入：`import static com.example.Common.Utils.UersUtils.getLoginKey;`
- 复用 `UersUtils.getLoginKey()` 方法，避免重复解析 token

### 工具方法（已存在于 UersUtils）
```java
public static String getLoginKey(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Bearer ")) {
        String token = authorization.substring(7).trim();
        return USER_LOGIN_KEY + token;
    }
    return null;
}
```

---

## 四、MyBatis @Param 注解说明

### 作用
用于 MyBatis 参数绑定，指定 XML 中引用的参数名

### 使用场景
| 情况 | 是否需要 @Param |
|------|----------------|
| 单个简单参数 | 不需要 |
| 多个参数 | 需要 |
| List/Map 参数在 foreach 中 | 建议加（更明确） |

### 示例
```java
// Mapper 接口
int batchInsert(@Param("messages") List<AIChatMessage> messages);

// XML 中引用
<foreach collection="messages" item="message" separator=",">
```

---

## 五、对象转换方法说明

### convertToAIChatMessage 方法
位于 `AIChatMessageServiceImpl.java`

```java
private AIChatMessage convertToAIChatMessage(Message message, String sessionId) {
    return AIChatMessage.builder()
            .sessionId(sessionId)
            .role(message.getMessageType().name().toLowerCase())
            .content(message.getText())
            .messageType("text")
            .createTime(LocalDateTime.now())
            .build();
}
```

### 区分
- **序列化**：对象 → JSON/字节流（如 `JSON.toJSONString()`）
- **对象转换**：对象A → 对象B（不同类型之间的数据映射）

此方法是将 Spring AI 的 Message 对象转换为我们的 AIChatMessage 实体，属于对象转换而非序列化。

---

## 六、后续待优化建议

### UersUtils.getLoginKey() 重载
建议添加无参重载方法，自动从 `RequestContextHolder` 获取 request：

```java
public static String getLoginKey() {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes != null) {
        return getLoginKey(attributes.getRequest());
    }
    return null;
}
```
