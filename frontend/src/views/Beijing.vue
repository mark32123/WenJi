<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentIndex = ref(0)

// 引导页图片数组
const guideImages = [
  '/images/beijing1.jpg',
  '/images/beijing2.jpg',
  '/images/beijing3.jpg',
  '/images/beijing4.jpg'
]

// 切换到下一页
const nextPage = () => {
  if (currentIndex.value < guideImages.length - 1) {
    currentIndex.value += 1
  }
}

// 开始体验，跳转到登录页
const startExperience = () => {
  router.push('/login')
}
</script>

<template>
  <div class="guide-wrapper">
    <!-- 轮播图区域 -->
    <div class="guide-swiper">
      <div
        v-for="(img, index) in guideImages"
        :key="index"
        class="guide-slide"
        :class="{ active: index === currentIndex }"
      >
        <img :src="img" :alt="`引导页${index + 1}`" class="guide-image" />
        <!-- 半透明遮罩层，增强按钮和指示器对比度 -->
        <div class="overlay"></div>
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
        class="guide-btn transparent-btn"
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
        ></span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.guide-wrapper {
  width: 100%;
  height: 100vh;
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