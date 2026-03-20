<template>
  <Layout title="个人资料" :showBack="true">
    <div class="page">
      <!-- 用户身份 -->
      <div class="user-section">
        <button class="edit-btn" @click="goToEditProfile">✏️</button>    
        <!-- 用户头像 -->
        <div class="avatar">
          <img 
            v-if="userAvatar && userAvatar.type === 'image'" 
            :src="userAvatar.url" 
            alt="头像" 
            class="avatar-image"
          />
          <svg
            v-else
            width="100%"
            height="100%"
            viewBox="0 0 100 100"
          >
            <!-- 根据用户选择的内置图标显示 -->
            <g v-for="(path, index) in selectedIconPaths" :key="index">
              <path v-if="path.d" :d="path.d" :fill="path.fill" />
              <ellipse v-else-if="path.type === 'ellipse'" 
                       :cx="path.cx" :cy="path.cy" :rx="path.rx" :ry="path.ry" :fill="path.fill" />
            </g>
          </svg>
        </div>
        
        <div class="username">{{ username }}</div>
        <div class="level">{{ level }}</div>
      </div>

      <!-- 文艺权益 -->
      <div class="benefits">
        <div class="benefit-title">🎁 已兑权益</div>
        
        <!-- 积分显示 -->
        <div class="points-display">
          <div class="points-label">我的积分</div>
          <div class="points-value">{{ points }}</div>
        </div>
        <div 
          v-for="(voucher, index) in vouchers" 
          :key="index" 
          class="voucher-item"
        >
          <div class="voucher-name">{{ voucher.name }}</div>
          <div class="voucher-desc">{{ voucher.description }}</div>
        </div>
        <a href="#" class="go-to-shop" @click.prevent="goToShop">→ 前往文迹商城兑换更多</a>
      </div>

      <!-- 已点亮地点 -->
      <div class="section-title">🔥 已点亮地点（{{ visitedPlaces.length }}/{{ totalPlaces }}）</div>
      <div class="places">
        <div 
          v-for="(place, index) in visitedPlaces" 
          :key="index" 
          class="place-tag"
        >
          <span class="place-icon">·</span>{{ place }}
        </div>
      </div>

      <!-- 徽章（圆形瓦当） -->
      <div class="section-title">
        🏆 我的徽章
        <button class="view-all-btn" @click="goToAllBadges">所有徽章</button>
      </div>
      <div class="badges">
        <div 
          v-for="(badge, index) in badges" 
          :key="index" 
          class="badge"
          @click="showBadgeFullscreen(badge)"
        >
          {{ badge.text }}
        </div>
      </div>

      <!-- 全屏徽章展示 -->
      <div v-if="selectedBadge" class="badge-fullscreen" @click="closeBadgeFullscreen">
        <div class="badge-fullscreen-content" @click.stop>
          <div class="badge-fullscreen-close" @click="closeBadgeFullscreen">×</div>
          <div class="badge-fullscreen-badge">
            <div class="badge-fullscreen-inner">
              <div class="badge-fullscreen-text">{{ selectedBadge.text }}</div>
            </div>
          </div>
          <div class="badge-fullscreen-info">
            <div class="badge-fullscreen-title">{{ selectedBadge.text.replace('\n', '') }}</div>
            <div class="badge-fullscreen-desc">恭喜您获得此徽章！这是您在文迹之旅中的一大成就。</div>
          </div>
        </div>
      </div>

      <!-- 已解锁成就 -->
      <div class="section-title">
        🎖️ 已解锁成就
        <button class="view-all-btn" @click="goToAllAchievements">所有成就</button>
      </div>
      <div class="achievements">
        <div
          v-for="(achievement, index) in achievements"
          :key="index"
          class="achievement-item"
        >
          <div class="achievement-title">{{ achievement.title }}</div>
          <div class="achievement-desc">{{ achievement.description }}</div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { onActivated, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import Layout from '@/components/Layout.vue'
import { ElMessage } from 'element-plus';
import { getCurrentUserInfo } from '@/api/user';
const router = useRouter();

const username = ref('');
const level = ref('');
const userAvatar = ref(null);
const selectedIconPaths = ref([]);
const points = ref(parseInt(localStorage.getItem('userPoints')) || 1000); // 从localStorage读取积分，默认为1000

// 默认头像路径 - 青瓷瓶
const defaultAvatarPaths = [
  { d: 'M50,10 C30,10 20,30 20,50 L20,70 C20,85 30,90 50,90 C70,90 80,85 80,70 L80,50 C80,30 70,10 50,10 Z', fill: '#A68A64', opacity: '0.9' },
  { type: 'ellipse', cx: 50, cy: 75, rx: 25, ry: 8, fill: '#8B7355' }
];

const goToEditProfile = () => {
  router.push('/user/userinfo');
}

const goToShop = () => {
  //ElMessage.info('暂未开放');
  router.push('/user/shop');
}

const goToAllBadges = () => {
  router.push('/user/all-badges');
}

const goToAllAchievements = () => {
  router.push('/user/all-achievements');
}

const loadUserInfo = async () => { 
  try{
    const response = await getCurrentUserInfo();
    console.log('获取用户信息响应:', response); // 调试信息
    
    if (response.code === 1) {
      username.value = response.data.username || '...';
      level.value = response.data.level || '普通用户';
      
      // 处理头像数据
      if (response.data.avatarUrl) {
        // 用户上传的头像
        userAvatar.value = {
          type: 'image',
          url: response.data.avatarUrl
        };
        console.log('设置了图片头像:', response.data.avatarUrl);
      } else if (response.data.iconName) {
        // 用户选择的内置图标
        userAvatar.value = {
          type: 'builtin',
          iconName: response.data.iconName
        };
        
        // 根据图标名称设置路径
        setSelectedIconPaths(response.data.iconName);
        console.log('设置了内置头像:', response.data.iconName);
      } else {
        // 使用默认头像
        userAvatar.value = {
          type: 'builtin',
          iconName: 'default'
        };
        selectedIconPaths.value = defaultAvatarPaths;
        console.log('设置了默认头像');
      }
    } else {
      username.value = '...';
      level.value = '普通用户';
      // 设置默认头像
      userAvatar.value = {
        type: 'builtin',
        iconName: 'default'
      };
      selectedIconPaths.value = defaultAvatarPaths;
      console.log('响应错误，设置了默认头像');
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    username.value = '...';
    level.value = '普通用户';
    // 设置默认头像
    userAvatar.value = {
      type: 'builtin',
      iconName: 'default'
    };
    selectedIconPaths.value = defaultAvatarPaths;
    console.log('捕获错误，设置了默认头像');
  }
}

// 根据图标名称设置路径
const setSelectedIconPaths = (iconName) => {
  // 这里可以根据不同的内置图标名称返回对应的路径
  const icons = {
    porcelain: [
      { d: 'M50,10 C30,10 20,30 20,50 L20,70 C20,85 30,90 50,90 C70,90 80,85 80,70 L80,50 C80,30 70,10 50,10 Z', fill: '#A68A64' }
    ],
    tea: [
      { d: 'M30,40 Q50,20 70,40 L65,70 Q50,80 35,70 Z', fill: '#8B7355' }
    ],
    scroll: [
      { d: 'M20,30 h60 v40 h-60 z', fill: '#A68A64', opacity: 0.9 },
      { d: 'M30,50 a8,8 0 1,0 0,0', fill: 'white' },
      { d: 'M70,50 a8,8 0 1,0 0,0', fill: 'white' }
    ],
    fan: [
      { d: 'M50,20 A30,30 0 0,1 80,50 A30,30 0 0,1 50,80 L50,20 Z', fill: '#A68A64' },
      { d: 'M50,20 L40,10 M50,20 L60,10', stroke: '#A68A64', strokeWidth: 4, fill: 'none' }
    ],
    architecture: [
      { d: 'M30,50 h40 v30 h-40 z', fill: '#A68A64' },
      { d: 'M20,40 h60 v10 h-60 z', fill: '#8B7355' },
      { d: 'M10,30 h80 v10 h-80 z', fill: '#A68A64' }
    ],
    lantern: [
      { type: 'ellipse', cx: 50, cy: 30, rx: 15, ry: 10, fill: '#A68A64' },
      { d: 'M35,30 h30 v40 h-30 z', rx: 6, fill: '#A68A64' },
      { type: 'ellipse', cx: 50, cy: 70, rx: 10, ry: 6, fill: '#8B7355' }
    ],
    inkstone: [
      { d: 'M20,50 h60 v20 h-60 z', fill: '#6B5B4F' },
      { d: 'M25,55 h50 v10 h-50 z', fill: '#8B7355' },
      { d: 'M40,45 a5,5 0 1,0 0,0', fill: '#A68A64' }
    ],
    brush: [
      { d: 'M45,20 h10 v60 h-10 z', fill: '#8B7355' },
      { d: 'M48,20 a8,8 0 1,0 0,0', fill: '#A68A64' },
      { d: 'M40,80 h20', stroke: '#6B5B4F', strokeWidth: 3, fill: 'none' }
    ],
    seal: [
      { d: 'M25,25 h50 v50 h-50 z', fill: '#C44536' },
      { d: 'M30,35 h40 v30 h-40 z', fill: '#D4C5B0' },
      { d: 'M40,45 a5,5 0 1,0 0,0', fill: '#A68A64' }
    ],
    bamboo: [
      { d: 'M45,10 h10 v80 h-10 z', fill: '#7D9A7E' },
      { d: 'M42,30 h16', stroke: '#5A7A65', strokeWidth: 2, fill: 'none' },
      { d: 'M42,50 h16', stroke: '#5A7A65', strokeWidth: 2, fill: 'none' },
      { d: 'M42,70 h16', stroke: '#5A7A65', strokeWidth: 2, fill: 'none' }
    ],
    cloud: [
      { d: 'M20,50 a15,15 0 1,0 0,0', fill: '#9FB3C8' },
      { d: 'M35,45 a20,20 0 1,0 0,0', fill: '#A8B5C0' },
      { d: 'M55,50 a15,15 0 1,0 0,0', fill: '#9FB3C8' }
    ],
    mountain: [
      { d: 'M20,80 L50,30 L80,80 Z', fill: '#6B7A8F' },
      { d: 'M10,80 L35,50 L60,80 Z', fill: '#8B9BB3' },
      { d: 'M50,80 L75,45 L100,80 Z', fill: '#7D8FA3' }
    ]
  };

  if (icons[iconName]) {
    selectedIconPaths.value = icons[iconName];
  } else {
    // 如果找不到对应图标，使用默认图标
    selectedIconPaths.value = defaultAvatarPaths;
  }
  console.log('设置图标路径:', iconName, selectedIconPaths.value);
}

const vouchers = ref([
  {
    name: '龙井春茗话费券',
    description: '可抵扣10元通信费用 · 有效期至2026.12'
  },
  {
    name: '青瓷守护礼包',
    description: '含AR体验券 ×2 + 专属徽章'
  }
]);

const visitedPlaces = ref([
  '杭州',
  '苏州',
  '景德镇',
  '泉州',
  '西安'
]);

const totalPlaces = ref(36);

const badges = ref([
  { text: '茶艺\n传人' },
  { text: '绣坊\n学徒' },
  { text: '瓷匠' },
  { text: '古建\n守护者' }
]);

const selectedBadge = ref(null);

const showBadgeFullscreen = (badge) => {
  selectedBadge.value = badge;
  document.body.style.overflow = 'hidden';
};

const closeBadgeFullscreen = () => {
  selectedBadge.value = null;
  document.body.style.overflow = '';
};

const achievements = ref([
  {
    title: '文化使者',
    description: '成功完成三次AI导游讲解任务。'
  },
  {
    title: '历史探寻者',
    description: '参观了五个不同的历史文化景点。'
  }
]);
onMounted(() => {
  loadUserInfo();
});
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.page {
  width: 100%;
  max-width: 390px;
  min-height: calc(100vh - 60px);
  background: white;
  padding: 28px 20px 40px 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
}

/* —————— 用户身份 —————— */
.user-section {
  text-align: center;
  margin-bottom: 28px;
  position: relative;
}

.edit-btn {
  position: absolute;
  top: 0;
  right: 0;
  background: none;
  border: none;
  color: #A68A64;
  font-size: 18px;
  cursor: pointer;
  padding: 4px;
}

.avatar {
  width: 68px;
  height: 68px;
  margin-bottom: 12px;
  display: inline-block;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #a68a64;
  background: #efeae5;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.username {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 6px;
}

.level {
  font-size: 14px;
  color: #8C7B6B;
}

/* —————— 权益区 —————— */
.benefits {
  margin-bottom: 24px;
}

.benefit-title {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #5A524A;
}

/* 积分显示样式 */
.points-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #A68A64 0%, #8B7355 100%);
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(166, 138, 100, 0.2);
}

.points-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.points-value {
  font-size: 20px;
  font-weight: 700;
  color: white;
}

.voucher-item {
  background: #FAF8F5;
  border-left: 3px solid #A68A64;
  padding: 10px 12px;
  margin-bottom: 8px;
  font-size: 15px;
  color: #3A3530;
  border-radius: 0 6px 6px 0;
}

.voucher-name {
  font-weight: 600;
}

.voucher-desc {
  font-size: 13px;
  color: #8C7B6B;
  margin-top: 4px;
}

.go-to-shop {
  display: block;
  text-align: center;
  color: #A68A64;
  font-size: 14px;
  margin-top: 12px;
  text-decoration: none;
}

/* —————— 地点 —————— */
.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 17px;
  font-weight: 600;
  margin: 20px 0 12px;
  color: #5A524A;
}

.view-all-btn {
  background: none;
  border: none;
  color: #A68A64;
  font-size: 14px;
  cursor: pointer;
  padding: 0;
  transition: color 0.3s ease;
}

.view-all-btn:hover {
  color: #8B7355;
}

.places {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.place-tag {
  background: #EFEAE5;
  color: #8C7B6B;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 替换圆点为小篆"迹"或墨点 */
.place-icon {
  font-size: 12px;
  color: #A68A64;
}

/* —————— 徽章（瓦当风格） —————— */
.badges {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.badge {
  width: 68px;
  height: 68px;
  background: white;
  border: 1px solid #C4B8A8;
  border-radius: 50%; /* 圆形瓦当 */
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #A68A64;
  text-align: center;
  line-height: 1.3;
  cursor: pointer;
  transition: all 0.2s ease;
}

.badge:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(166, 138, 100, 0.2);
  border-color: #A68A64;
}

/* —————— 成就展示 —————— */
.achievements {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 4px;
}

.achievement-item {
  background: #FAF8F5;
  border-left: 3px solid #A68A64;
  padding: 10px 12px;
  border-radius: 0 6px 6px 0;
  contain: layout style;
}

.achievement-title {
  font-size: 15px;
  font-weight: 600;
  color: #3A3530;
  margin-bottom: 4px;
}

.achievement-desc {
  font-size: 13px;
  color: #8C7B6B;
  line-height: 1.4;
}

/* —————— 全屏徽章展示 —————— */
.badge-fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.badge-fullscreen-content {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: scaleIn 0.3s ease-in-out;
}

@keyframes scaleIn {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.badge-fullscreen-close {
  position: absolute;
  top: -50px;
  right: -20px;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #5A524A;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.2s ease;
}

.badge-fullscreen-close:hover {
  background: white;
  transform: scale(1.1);
}

.badge-fullscreen-badge {
  width: 200px;
  height: 200px;
  position: relative;
  margin-bottom: 30px;
}

.badge-fullscreen-inner {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #F5EFE9 0%, #E8E0D8 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 30px rgba(166, 138, 100, 0.3), inset 0 0 20px rgba(166, 138, 100, 0.1);
  border: 4px solid #A68A64;
  position: relative;
  overflow: hidden;
}

.badge-fullscreen-inner::before {
  content: '';
  position: absolute;
  top: 10px;
  left: 10px;
  right: 10px;
  bottom: 10px;
  border-radius: 50%;
  border: 2px dashed rgba(166, 138, 100, 0.3);
}

.badge-fullscreen-text {
  font-size: 36px;
  font-weight: 700;
  color: #A68A64;
  text-align: center;
  line-height: 1.3;
  text-shadow: 1px 1px 2px rgba(255, 255, 255, 0.5);
}

.badge-fullscreen-info {
  text-align: center;
  max-width: 300px;
}

.badge-fullscreen-title {
  font-size: 28px;
  font-weight: 700;
  color: #F5EFE9;
  margin-bottom: 16px;
}

.badge-fullscreen-desc {
  font-size: 16px;
  color: #E8E0D8;
  line-height: 1.6;
}
</style>