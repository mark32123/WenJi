<template>
  <Layout title="文迹 - 探索文化遗产" :showBack="false">
    <div class="home-container">
      <!-- 用户头像 -->
      <div class="user-avatar" @click="goToProfile">📜</div>

      <!-- 内容区域 -->
      <div class="content">
        <h1 class="main-title">欢迎探索非遗文化</h1>

        <!-- 功能卡片 -->
        <div v-for="(feature, index) in features" :key="index" class="feature-card" @click="handleFeatureClick(feature.id)">
          <h2>{{ feature.title }}</h2>
          <p>{{ feature.description }}</p>
        </div>

        <!-- 成就展示区 -->
        <div class="achievements">
          <div class="achievements-title">已解锁成就</div>
          <div 
            v-for="(achievement, index) in achievements" 
            :key="index" 
            class="achievement-item"
          >
            <h3>{{ achievement.title }}</h3>
            <p>{{ achievement.description }}</p>
          </div>
        </div>

        
      </div><!-- 底部分割线与引文 -->
        <div class="divider"></div>
    </div>
  </Layout>
</template>

<script setup>
import { useRouter } from 'vue-router'
import Layout from '@/components/Layout.vue'

const router = useRouter()

const features = [
  {
    id: 'ai-guide',
    title: 'AI导游讲解',
    description: '通过智能导游，深入了解每一个文化景点背后的故事。'
  },
  {
    id: 'ar-display',
    title: 'AR虚拟展示',
    description: '利用增强现实技术，身临其境感受非遗文化的魅力。'
  },
  {
    id: 'map-record',
    title: '地图点亮记录',
    description: '每当你走过一个地方，地图上就会点亮一个新的地标。'
  }
]

const achievements = [
  {
    title: '文化使者',
    description: '成功完成三次AI导游讲解任务。'
  },
  {
    title: '历史探寻者',
    description: '参观了五个不同的历史文化景点。'
  }
]

function goToProfile() {
  // 跳转到用户个人资料页
  console.log('跳转到个人资料页');
  router.push('/user/profile');
}

function handleFeatureClick(featureId) {
  switch(featureId) {
    case 'ai-guide':
      // 跳转到AI导游页面
      console.log('跳转到AI导游页面');
      router.push('/user/aiguide');
      break;
    case 'ar-display':
      // 跳转到AR体验页面
      console.log('跳转到AR体验页面');
      router.push('/user/ceramicAr');
      break;
    case 'map-record':
      // 跳转到地图页面
      console.log('跳转到地图页面');
      router.push('/user/map');
      break;
    default:
      break;
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.home-container {
  background-color: #F8F5F0;
  font-family: "Noto Serif SC", serif;
  color: #3A3530;
  width: 100%;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 用户头像 */
.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: grid; /* 启用 Grid 布局 */
  place-items: center; /* 水平和垂直居中 */
  font-size: 20px;
  background-color: #A68A64;
  cursor: pointer;
  align-self: flex-end;
  margin-bottom: -10px;
}

/* 内容区域 */
.content {
  width: 100%;
  padding: 20px 0;
}

.main-title {
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 20px;
}

/* 功能卡片 */
.feature-card {
  background-color: #FFFFFF;
  border: 1px solid #C4B8A8;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  transition: transform 0.2s ease-in-out;
  cursor: pointer;
}

.feature-card:hover {
  transform: scale(1.02);
}

.feature-card h2 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 10px;
}

.feature-card p {
  font-size: 16px;
  color: #8C7B6B;
}

/* 成就展示区 */
.achievements {
  margin-top: 40px;
}

.achievements-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 10px;
}

.achievement-item {
  background-color: #FFFFFF;
  border: 1px solid #C4B8A8;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 10px;
}

.achievement-item h3 {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 5px;
}

.achievement-item p {
  font-size: 14px;
  color: #8C7B6B;
}

/* 底部分割线 */
.divider {
  width: 80%;
  height: 0.5px;
  background-color: #C4B8A8;
  margin: 20px auto;
}

</style>