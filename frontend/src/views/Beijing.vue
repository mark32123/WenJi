<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentIndex = ref(0)
const isTransitioning = ref(false)
let autoPlayTimer = null
let touchStartX = 0
let touchEndX = 0

// 引导页图片数组
const guideImages = [
  '/images/beijing1.jpg',
  '/images/beijing2.jpg',
  '/images/beijing3.jpg',
  '/images/beijing4.jpg'
]

// 获取当前页面的标题
const getSlideTitle = (index) => {
  const titles = [
    '探索文化遗产',
    '沉浸式体验',
    '智能AI导览',
    '开启文化之旅'
  ]
  return titles[index] || ''
}

// 获取当前页面的描述
const getSlideDescription = (index) => {
  const descriptions = [
    '发现中华上下五千年的璀璨文化',
    '身临其境感受历史遗迹的魅力',
    'AI智能助手为您提供专业讲解',
    '立即开始您的文化探索之旅'
  ]
  return descriptions[index] || ''
}

// 切换到指定页面
const goToPage = (index) => {
  if (index !== currentIndex.value && !isTransitioning.value) {
    isTransitioning.value = true
    currentIndex.value = index
    setTimeout(() => {
      isTransitioning.value = false
    }, 500)
  }
}

// 切换到下一页
const nextPage = () => {
  if (currentIndex.value < guideImages.length - 1) {
    goToPage(currentIndex.value + 1)
  }
}

// 切换到上一页
const prevPage = () => {
  if (currentIndex.value > 0) {
    goToPage(currentIndex.value - 1)
  }
}

// 开始体验，跳转到登录页
const startExperience = () => {
  // 添加淡出效果
  const guideWrapper = document.querySelector('.guide-wrapper')
  if (guideWrapper) {
    guideWrapper.classList.add('fade-out')
    setTimeout(() => {
      router.push('/login')
    }, 1000)
  } else {
    router.push('/login')
  }
}

// 自动播放轮播图
const startAutoPlay = () => {
  if (autoPlayTimer) clearInterval(autoPlayTimer)
  autoPlayTimer = setInterval(() => {
    if (currentIndex.value < guideImages.length - 1) {
      nextPage()
    } else {
      // 在最后一页停止自动播放
      stopAutoPlay()
    }
  }, 4000)
}

// 停止自动播放
const stopAutoPlay = () => {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer)
    autoPlayTimer = null
  }
}

// 触摸事件处理
const handleTouchStart = (e) => {
  touchStartX = e.changedTouches[0].screenX
  stopAutoPlay()
}

const handleTouchMove = (e) => {
  touchEndX = e.changedTouches[0].screenX
}

const handleTouchEnd = () => {
  const swipeThreshold = 50
  const diff = touchStartX - touchEndX
  
  if (Math.abs(diff) > swipeThreshold) {
    if (diff > 0) {
      // 向左滑动，下一页
      nextPage()
    } else {
      // 向右滑动，上一页
      prevPage()
    }
  }
  
  // 如果不是最后一页，重新开始自动播放
  if (currentIndex.value < guideImages.length - 1) {
    startAutoPlay()
  }
}

// 组件挂载时开始自动播放
onMounted(() => {
  startAutoPlay()
})

// 组件卸载前清除定时器
onBeforeUnmount(() => {
  stopAutoPlay()
})
</script>

<template>
  <div class="guide-wrapper">
    <!-- 轮播图区域 -->
    <div 
      class="guide-swiper"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove"
      @touchend="handleTouchEnd"
    >
      <div
        v-for="(img, index) in guideImages"
        :key="index"
        class="guide-slide"
        :class="{ active: index === currentIndex }"
      >
        <img :src="img" :alt="`引导页${index + 1}`" class="guide-image" />
        <!-- 半透明遮罩层，增强按钮和指示器对比度 -->
        <div class="overlay"></div>
        
        <!-- 添加引导页文字说明 -->
        <div class="slide-content">
          <h2 class="slide-title">{{ getSlideTitle(index) }}</h2>
          <p class="slide-description">{{ getSlideDescription(index) }}</p>
        </div>
      </div>
    </div>

    <!-- 底部按钮区域 -->
    <div class="guide-footer">
      <!-- 前3页显示“下一页”按钮 -->
      <el-button
        v-if="currentIndex < guideImages.length - 1"
        class="guide-btn transparent-btn"
        @click="nextPage"
      >
        下一页
      </el-button>
      <!-- 第4页显示“开始体验”按钮 -->
      <el-button
        v-else
        class="guide-btn start-btn"
        @click="startExperience"
      >
        开始体验
      </el-button>

      <!-- 指示器 -->
      <div class="guide-indicators">
        <span
          v-for="(_, index) in guideImages"
          :key="index"
          class="indicator"
          :class="{ active: index === currentIndex }"
          @click="goToPage(index)"
        ></span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.guide-wrapper {
  width: 100%;
  height: 100vh;
  transition: opacity 1s ease-in-out;
  overflow: hidden;
  position: relative;
  box-sizing: border-box;
}

.guide-swiper {
  width: 100%;
  height: 100%;
  position: relative;
  max-width: 450px;
  margin: 0 auto;
}

.guide-slide {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  transition: opacity 0.5s ease;
}

.guide-slide.active {
  opacity: 1;
}

.guide-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 半透明遮罩，让按钮更突出 */
.overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.4), transparent 60%);
  pointer-events: none;
}

.guide-footer {
  position: absolute;
  bottom: 60px;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  z-index: 10;
  max-width: 450px;
  margin: 0 auto;
}

/* 透明按钮样式 */
.guide-btn {
  width: 200px;
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
  font-weight: 600;
}

.transparent-btn {
  background-color: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.5);
  color: #ffffff;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.transparent-btn:hover {
  background-color: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.7);
  color: #ffffff;
}

.guide-indicators {
  display: flex;
  gap: 8px;
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.4);
  transition: background-color 0.3s ease;
}

.indicator.active {
  background-color: #ffffff;
}
</style>