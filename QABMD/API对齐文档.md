# API接口对齐文档

## 概述

本文档记录前端与后端API的对齐情况，包括已对齐、待接入和待实现的接口。

---

## 一、已对齐的API

### 1. 用户模块

| 功能 | 前端API | 后端接口 | 状态 |
|------|---------|----------|------|
| 获取验证码 | `GET /user/captcha` | UserController | ✅ 已对齐 |
| 用户登录 | `POST /user/login` | UserController | ✅ 已对齐 |
| 用户登出 | `POST /user/logout` | UserController | ✅ 已对齐 |
| 获取当前用户信息 | `GET /user/currentUserInfo` | UserController | ✅ 已对齐 |
| 更新用户信息 | `PUT /user/updateUserInfo` | UserController | ✅ 已对齐 |
| 删除用户 | `DELETE /user/deleteUserInfo` | UserController | ✅ 已对齐 |

### 2. 徽章模块

| 功能 | 前端API | 后端接口 | 状态 |
|------|---------|----------|------|
| 获取我的徽章 | `GET /badge/myBadges` | BadgeController | ✅ 已对齐 |

### 3. 文件模块

| 功能 | 前端API | 后端接口 | 状态 |
|------|---------|----------|------|
| 文件上传 | `POST /common/upload` | CommonController | ✅ 已对齐 |

### 4. 景点模块

| 功能 | 前端API | 后端接口 | 状态 |
|------|---------|----------|------|
| 获取附近景点 | `GET /map/initial?lng=&lat=` | HeritageController | ✅ 已对齐 |
| 获取景点详情 | `GET /map/{id}` | HeritageController | ✅ 已对齐 |

### 5. AI聊天模块

| 功能 | 前端API | 后端接口 | 状态 |
|------|---------|----------|------|
| AI聊天(流式) | `POST /ai/chat` | ChatController | ✅ 已对齐 |
| 获取聊天ID列表 | `GET /ai/history/{type}/list` | ChatHistoryController | ✅ 已对齐 |
| 获取聊天历史 | `GET /ai/history/{type}/{sessionId}` | ChatHistoryController | ✅ 已对齐 |
| 删除会话 | `DELETE /ai/history/session/{sessionId}` | ChatHistoryController | ✅ 已对齐 |

---

## 二、后端已有但前端未接入的API

### 1. 旅游心得模块

| 功能 | 后端接口 | Controller | 说明 |
|------|----------|------------|------|
| 发布旅游心得 | `POST /travel/publish` | TravelBlogController | 打卡留念功能需要 |
| 获取我的足迹 | `GET /travel/myBlogs` | TravelBlogController | 个人中心足迹展示需要 |

**前端需要添加：**

```javascript
export const travelApi = {
  publishBlog(data) {
    return api.post('/travel/publish', data)
  },
  
  getMyBlogs() {
    return api.get('/travel/myBlogs')
  }
}
```

---

## 三、前端有但后端未实现的API

### 1. 徽章解锁接口

| 功能 | 前端API | 状态 | 说明 |
|------|---------|------|------|
| 解锁徽章 | `POST /badge/unlock/{badgeId}` | ❌ 后端未实现 | **不需要实现**，徽章是自动解锁的 |

**说明：** 徽章系统采用自动解锁机制，用户发布打卡时会自动检查并发放徽章，无需手动调用解锁接口。

**建议：** 删除前端的 `unlockBadge` API。

---

## 四、徽章系统说明

### 自动解锁机制

```
用户发布打卡 → TravelBlogServiceImpl.publishBlog()
                    ↓
              自动调用 badgeService.checkAndDistributeBadges()
                    ↓
              检查用户进度是否满足徽章条件
                    ↓
              满足条件 → 自动发放徽章 + 奖励积分
```

### 徽章类型

| 类型 | 触发条件 | 说明 |
|------|---------|------|
| `blog` | 发布旅游心得数量 | 如：发布1篇、5篇、10篇获得不同徽章 |
| `footprint` | 打卡不同景点数量 | 如：打卡1个、5个、10个不同景点获得徽章 |

---

## 五、待办事项

- [ ] 前端添加 `travelApi` 接入打卡留念功能
- [ ] 前端删除无用的 `unlockBadge` API
- [ ] 测试打卡留念功能与徽章自动解锁

---

## 六、文件路径参考

### 前端

- API定义: `frontend/src/api/index.js`
- 用户Store: `frontend/src/stores/userStore.js`

### 后端

- 用户控制器: `backend/src/main/java/com/example/Controller/UserController.java`
- 徽章控制器: `backend/src/main/java/com/example/Controller/BadgeController.java`
- 徽章服务: `backend/src/main/java/com/example/Service/Impl/BadgeServiceImpl.java`
- 旅游心得控制器: `backend/src/main/java/com/example/Controller/TravelBlogController.java`
- 旅游心得服务: `backend/src/main/java/com/example/Service/Impl/TravelBlogServiceImpl.java`
- AI聊天控制器: `backend/src/main/java/com/example/Controller/ChatController.java`
- AI历史控制器: `backend/src/main/java/com/example/Controller/ChatHistoryController.java`
- 景点控制器: `backend/src/main/java/com/example/Controller/HeritageController.java`
- 文件控制器: `backend/src/main/java/com/example/Controller/CommonController.java`
