<template>
  <div class="mind-ar-viewer">
    <div class="ar-scene" ref="sceneRef">
      <video ref="videoRef" class="ar-video" playsinline autoplay muted></video>
      
      <div v-if="!isTracking" class="scanning-overlay">
        <div class="scan-frame">
          <div class="corner tl"></div>
          <div class="corner tr"></div>
          <div class="corner bl"></div>
          <div class="corner br"></div>
        </div>
        <p class="scan-hint font-serif">将摄像头对准文物图片</p>
      </div>
      
      <div v-if="isTracking && currentExhibit" class="ar-content">
        <div class="exhibit-info">
          <h3 class="exhibit-name font-serif">{{ currentExhibit.name }}</h3>
          <p class="exhibit-era">{{ currentExhibit.metadata?.era }}</p>
        </div>
        
        <div class="ar-layers">
          <div v-if="backgroundLayers.length === 0" class="no-image">
            <span class="no-image-icon">🖼️</span>
            <span class="no-image-text">暂无图片</span>
          </div>
          <img 
            v-for="layer in backgroundLayers" 
            :key="layer.id"
            :src="layer.src"
            class="ar-layer background"
            :style="getLayerStyle(layer)"
            @error="handleImageError"
            @load="handleImageLoad"
          />
        </div>
        
        <div class="ar-controls">
          <button class="control-btn close" @click="closeTracking">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
          <button class="control-btn info" @click="showExhibitDetail">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
    
    <div class="viewer-header">
      <button class="back-btn" @click="handleBack">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </button>
      <h2 class="header-title font-serif">AR 识图</h2>
      <button class="switch-btn" @click="switchCamera">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
          <circle cx="12" cy="13" r="4"/>
        </svg>
      </button>
    </div>
    
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p class="loading-text">正在初始化摄像头...</p>
    </div>
    
    <div v-if="error" class="error-overlay">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/>
        <line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <p class="error-text">{{ error }}</p>
      <button class="retry-btn" @click="initCamera">重试</button>
    </div>
    
    <div class="demo-controls">
      <button class="demo-btn" @click="simulateRecognition">
        演示识别
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import exhibitsData from '@/data/exhibits.json'

const router = useRouter()
const userStore = useUserStore()

const sceneRef = ref(null)
const videoRef = ref(null)

const loading = ref(true)
const error = ref(null)
const isTracking = ref(false)
const currentExhibit = ref(null)
const facingMode = ref('environment')

let stream = null

const backgroundLayers = computed(() => {
  if (!currentExhibit.value) return []
  return currentExhibit.value.layers?.filter(l => l.type === 'background') || []
})

const getLayerStyle = (layer) => {
  return {
    transform: `scale(${layer.scale || 1}) translateY(${layer.offsetY || 0}px)`,
    zIndex: layer.zIndex || 1
  }
}

const initCamera = async () => {
  loading.value = true
  error.value = null
  
  try {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      throw new Error('您的设备不支持摄像头访问')
    }
    
    stream = await navigator.mediaDevices.getUserMedia({
      video: {
        facingMode: facingMode.value,
        width: { ideal: 1280 },
        height: { ideal: 720 }
      },
      audio: false
    })
    
    if (videoRef.value) {
      videoRef.value.srcObject = stream
      await videoRef.value.play()
    }
    
    loading.value = false
  } catch (e) {
    console.error('摄像头初始化失败:', e)
    error.value = e.message || '初始化失败，请检查摄像头权限'
    loading.value = false
  }
}

const simulateRecognition = () => {
  const exhibits = exhibitsData.exhibits
  console.log('所有展品:', exhibits)
  if (exhibits.length > 0) {
    const randomExhibit = exhibits[Math.floor(Math.random() * exhibits.length)]
    console.log('选中展品:', randomExhibit)
    console.log('展品图片:', randomExhibit.layers)
    currentExhibit.value = randomExhibit
    isTracking.value = true
    
    if (randomExhibit.unlockReward) {
      userStore.addExperience(randomExhibit.unlockReward.experience || 0)
    }
  }
}

const closeTracking = () => {
  isTracking.value = false
  currentExhibit.value = null
}

const handleImageError = (e) => {
  console.warn('图片加载失败:', e.target.src)
  e.target.style.display = 'none'
}

const handleImageLoad = (e) => {
  console.log('图片加载成功:', e.target.src)
}

const showExhibitDetail = () => {
  if (currentExhibit.value) {
    router.push({
      path: '/reward',
      query: { id: currentExhibit.value.id }
    })
  }
}

const switchCamera = async () => {
  facingMode.value = facingMode.value === 'environment' ? 'user' : 'environment'
  cleanup()
  await initCamera()
}

const handleBack = () => {
  cleanup()
  router.back()
}

const cleanup = () => {
  if (stream) {
    stream.getTracks().forEach(track => track.stop())
    stream = null
  }
  
  if (videoRef.value) {
    videoRef.value.srcObject = null
  }
}

onMounted(() => {
  initCamera()
})

onUnmounted(() => {
  cleanup()
})
</script>

<style scoped>
.mind-ar-viewer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #000;
  overflow: hidden;
}

.ar-scene {
  position: relative;
  width: 100%;
  height: 100%;
}

.ar-video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ar-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.scanning-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.scan-frame {
  width: 260px;
  height: 260px;
  position: relative;
}

.corner {
  position: absolute;
  width: 40px;
  height: 40px;
  border-color: #C9A227;
  border-style: solid;
  border-width: 0;
}

.corner.tl {
  top: 0;
  left: 0;
  border-top-width: 3px;
  border-left-width: 3px;
  border-top-left-radius: 12px;
}

.corner.tr {
  top: 0;
  right: 0;
  border-top-width: 3px;
  border-right-width: 3px;
  border-top-right-radius: 12px;
}

.corner.bl {
  bottom: 0;
  left: 0;
  border-bottom-width: 3px;
  border-left-width: 3px;
  border-bottom-left-radius: 12px;
}

.corner.br {
  bottom: 0;
  right: 0;
  border-bottom-width: 3px;
  border-right-width: 3px;
  border-bottom-right-radius: 12px;
}

.scan-hint {
  margin-top: 30px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 16px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.ar-content {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px 120px;
  box-sizing: border-box;
}

.exhibit-info {
  text-align: center;
  pointer-events: auto;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.exhibit-name {
  font-size: 22px;
  color: #fff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.6);
  margin: 0 0 8px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 280px;
}

.exhibit-era {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

.ar-layers {
  width: 70%;
  max-width: 260px;
  flex-shrink: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.ar-layer {
  width: 100%;
  height: auto;
  max-height: 40vh;
  border-radius: 12px;
  object-fit: contain;
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
}

.no-image-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.no-image-text {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.ar-layer.video {
  mix-blend-mode: screen;
}

.ar-controls {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 20px;
  pointer-events: auto;
}

.control-btn {
  width: 56px;
  height: 56px;
  background: rgba(45, 64, 89, 0.8);
  border: none;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  backdrop-filter: blur(10px);
}

.control-btn:active {
  transform: scale(0.95);
}

.viewer-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.6), transparent);
  z-index: 10;
}

.back-btn,
.switch-btn {
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.header-title {
  font-size: 18px;
  color: #fff;
  margin: 0;
}

.loading-overlay,
.error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.8);
  z-index: 20;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(201, 162, 39, 0.3);
  border-top-color: #C9A227;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text,
.error-text {
  color: #fff;
  font-size: 16px;
  margin-top: 20px;
}

.retry-btn {
  margin-top: 20px;
  padding: 12px 32px;
  background: #C9A227;
  border: none;
  border-radius: 24px;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
}

.demo-controls {
  position: absolute;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 15;
}

.demo-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #C9A227, #D4AF37);
  border: none;
  border-radius: 24px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(201, 162, 39, 0.4);
}

.demo-btn:active {
  transform: scale(0.95);
}
</style>
