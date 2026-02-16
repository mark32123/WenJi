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
      <div class="section-title">🏆 我的徽章</div>
      <div class="badges">
        <div 
          v-for="(badge, index) in badges" 
          :key="index" 
          class="badge"
        >
          {{ badge.text }}
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

// 默认头像路径 - 青瓷瓶
const defaultAvatarPaths = [
  { d: 'M50,10 C30,10 20,30 20,50 L20,70 C20,85 30,90 50,90 C70,90 80,85 80,70 L80,50 C80,30 70,10 50,10 Z', fill: '#A68A64', opacity: '0.9' },
  { type: 'ellipse', cx: 50, cy: 75, rx: 25, ry: 8, fill: '#8B7355' }
];

const goToEditProfile = () => {
  router.push('/user/userinfo');
}

const goToShop = () => {
  ElMessage.info('暂未开放');
  // router.push('/shop');
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
  height: 830px;
  background: white;
  padding: 28px 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
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
  font-size: 17px;
  font-weight: 600;
  margin: 20px 0 12px;
  color: #5A524A;
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
}
</style>