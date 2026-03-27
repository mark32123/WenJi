# 文迹 (WenJi) - 文化遗产探索应用

## 项目简介

文迹是一款基于地理位置的传统文化探索应用，通过"行走+体验+奖励"的模式，让用户在探索文化遗产的过程中获得沉浸式体验。应用融合了AR技术、AI智能讲解和游戏化激励机制，旨在降低文化接触门槛，吸引年轻用户深度参与非遗文化的传承与传播。

## 核心功能

### 1. 山河卷（首页）
- 基于高德地图的文化遗产地图展示
- 景点标记与详情查看
- AI智能助手对话功能
- 城市切换与定位服务

### 2. AR鉴赏
- 图片扫描识别模式（MindAR）
- 二维码扫描触发模式
- 3D文物模型展示
- 文物信息AR叠加显示

### 3. 诗词创作与游记打卡
- AI生成诗词（基于地点）
- 自定义诗词创作
- 游记撰写与发布
- 图片上传与保存
- 数据持久化存储

### 4. 藏经阁（个人中心）
- 用户信息展示与编辑
- 诗词/游记收藏管理
- 文物收藏展示
- 印章成就系统
- 阅历等级体系

### 5. 激励体系
- 打卡获得阅历值
- 等级晋升系统（初识→登堂→入室→登峰→造极→大成）
- 徽章收集
- 积分奖励

## 技术栈

### 前端
- **框架**: Vue 3 + Vite
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **地图服务**: 高德地图 JavaScript API
- **AR技术**: MindAR + A-Frame
- **3D渲染**: Three.js
- **样式**: SCSS + Tailwind CSS

### 后端
- **框架**: Spring Boot 3.x
- **数据库**: MySQL + MyBatis Plus
- **缓存**: Redis
- **认证**: JWT (JSON Web Token)
- **API文档**: Swagger/OpenAPI

## 项目结构

```
demo/
├── frontend/                 # 前端项目
│   ├── src/
│   │   ├── api/             # API接口封装
│   │   ├── components/      # 组件库
│   │   │   ├── atoms/       # 原子组件
│   │   │   ├── molecules/   # 分子组件
│   │   │   └── organisms/   # 有机组件
│   │   ├── data/            # 静态数据
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia状态管理
│   │   └── views/           # 页面视图
│   ├── public/              # 静态资源
│   │   ├── images/          # 图片资源
│   │   ├── models/          # 3D模型
│   │   └── targets/         # AR识别文件
│   └── package.json
│
├── backend/                  # 后端项目
│   ├── src/main/java/com/example/
│   │   ├── Controller/      # 控制器层
│   │   ├── Service/         # 服务层
│   │   ├── Mapper/          # 数据访问层
│   │   ├── Pojo/            # 实体类
│   │   ├── Config/          # 配置类
│   │   └── Common/          # 公共工具类
│   └── pom.xml
│
└── 开发文档.md               # 详细开发文档
```

## 快速开始

### 环境要求
- Node.js >= 20.19.0
- JDK 17+
- MySQL 8.0+
- Redis

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

### 后端启动

```bash
cd backend
mvn spring-boot:run
```

### 配置说明

1. 配置数据库连接（backend/src/main/resources/application.yml）
2. 配置Redis连接
3. 配置高德地图API Key（frontend/src/views/HomeView.vue）

## 主要API接口

### 用户模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /user/login | POST | 用户登录 |
| /user/captcha | GET | 获取验证码 |
| /user/currentUserInfo | GET | 获取当前用户信息 |
| /user/updateUserInfo | PUT | 更新用户信息 |

### 游记模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /travel/publish | POST | 发布游记 |
| /travel/myBlogs | GET | 获取我的游记列表 |
| /travel/delete/{blogId} | DELETE | 删除游记 |

### 地图模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /map/initial | GET | 获取附近景点 |
| /map/{id} | GET | 获取景点详情 |

### AI模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /ai/chat | POST | AI对话（流式响应） |
| /ai/history/{type}/list | GET | 获取会话列表 |

## 数据库表结构

### 用户表 (user)
- 用户基本信息
- 登录凭证
- 个人设置

### 游记表 (travel_blog)
- 游记内容
- 关联景点
- 图片信息

### 景点表 (heritage_site)
- 景点信息
- 地理位置
- 开放时间

### 徽章表 (badge)
- 徽章定义
- 获取条件

## 特色功能说明

### AR识别
- 支持图片扫描识别（需要.mind文件）
- 支持二维码快速触发
- 3D模型实时渲染

### AI诗词生成
- 基于地点的智能创作
- 流式响应输出
- 支持自定义修改

### 数据持久化
- 游记数据存储到数据库
- 诗词数据存储到localStorage
- 用户状态Redis缓存

## 开发团队

本项目为文化遗产数字化传播的创新尝试，旨在通过技术手段让传统文化"活"起来。

## 许可证

MIT License
