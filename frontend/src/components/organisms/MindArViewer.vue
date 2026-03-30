<template>
  <div class="mind-ar-viewer">
    <div class="ar-scene" ref="sceneRef">
      <a-scene
        v-if="hasMindFiles && currentTarget"
        :mindar-image="`imageTargetSrc: ${currentTarget};`"
        color-space="sRGB"
        renderer="colorManagement: true, physicallyCorrectLights"
        vr-mode-ui="enabled: false"
        device-orientation-permission-ui="enabled: false"
        embedded
      >
        <a-assets>
          <img 
            v-for="layer in backgroundLayers" 
            :key="layer.id"
            :id="layer.id" 
            :src="layer.src" 
          />
        </a-assets>
        
        <a-camera position="0 0 0" look-controls="enabled: false"></a-camera>
        
        <a-entity mindar-image-target="targetIndex: 0">
          <a-plane 
            :src="'#' + backgroundLayers[0]?.id"
            position="0 0 0"
            height="0.552"
            width="1"
            rotation="0 0 0"
          ></a-plane>
        </a-entity>
      </a-scene>
      
      <video ref="videoRef" class="ar-video" playsinline autoplay muted v-show="!hasMindFiles"></video>
      
      <div v-if="!isTracking" class="scanning-overlay">
        <div class="scan-frame">
          <div class="corner tl"></div>
          <div class="corner tr"></div>
          <div class="corner bl"></div>
          <div class="corner br"></div>
        </div>
        <p class="scan-hint font-serif">{{ scanHint }}</p>
      </div>
      
      <div v-if="isTracking && currentExhibit" class="ar-content">
        <div class="exhibit-info">
          <h3 class="exhibit-name font-serif">{{ currentExhibit.name }}</h3>
          <p class="exhibit-era">{{ currentExhibit.metadata?.era }}</p>
        </div>
        
        <div class="ar-layers" v-if="!hasMindFiles">
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
      <h2 class="header-title font-serif">AR鉴赏</h2>
      <div class="placeholder"></div>
    </div>
    
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p class="loading-text">{{ loadingText }}</p>
    </div>
    
    <div v-if="error" class="error-overlay">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/>
        <line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <p class="error-text">{{ error }}</p>
      <button class="retry-btn" @click="initAR">重试</button>
    </div>
    
    <div v-if="!loading && !isTracking" class="demo-section">
      <p class="demo-hint">{{ hasMindFiles ? '将摄像头对准文物图片' : '扫描文物二维码或使用演示模式' }}</p>
      <button class="demo-btn" @click="simulateRecognition">
        演示：青花瓷瓶
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import exhibitsData from '@/data/exhibits.json'
import jsQR from 'jsqr'

const router = useRouter()
const route = useRoute()

const sceneRef = ref(null)
const videoRef = ref(null)

const loading = ref(true)
const loadingText = ref('正在初始化...')
const error = ref(null)
const isTracking = ref(false)
const currentExhibit = ref(null)
const currentTarget = ref(null)
const facingMode = ref('environment')
const hasMindFiles = ref(false)

let stream = null
let qrScanInterval = null

const scanHint = computed(() => {
  if (hasMindFiles.value) {
    return '将摄像头对准文物图片'
  }
  return '将二维码放入框内'
})

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

const checkMindFiles = async () => {
  loadingText.value = '检查识别文件...'
  
  try {
    const response = await fetch('/targets/targets.mind', { method: 'GET' })
    if (response.ok) {
      const buffer = await response.arrayBuffer()
      if (buffer.byteLength > 1000) {
        const header = new Uint8Array(buffer, 0, 4)
        if (header[0] === 0x4D && header[1] === 0x49 && header[2] === 0x4E && header[3] === 0x44) {
          hasMindFiles.value = true
          currentTarget.value = '/targets/targets.mind'
          const qinghuaci = exhibitsData.exhibits.find(e => e.id === 'qinghuaci')
          if (qinghuaci) {
            currentExhibit.value = qinghuaci
          }
          console.log('MindAR 识别文件加载成功')
          return true
        }
      }
    }
  } catch (e) {
    console.warn('MindAR 识别文件不存在:', e)
  }
  return false
}

const initAR = async () => {
  loading.value = true
  error.value = null
  
  try {
    const mindFilesExist = await checkMindFiles()
    
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      throw new Error('您的设备不支持摄像头访问')
    }
    
    if (mindFilesExist) {
      loadingText.value = '正在加载 AR 引擎...'
      await startCamera()
      await initAFrameAR()
    } else {
      loadingText.value = '正在启动摄像头...'
      await startCamera()
      startQRScan()
    }
    
    loading.value = false
  } catch (e) {
    console.error('AR初始化失败:', e)
    error.value = e.message || '初始化失败，请检查摄像头权限'
    loading.value = false
  }
}

const startCamera = async () => {
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
}

const startQRScan = () => {
  if (qrScanInterval) {
    clearInterval(qrScanInterval)
  }
  
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  
  qrScanInterval = setInterval(() => {
    if (!videoRef.value || videoRef.value.readyState !== 4) return
    
    canvas.width = videoRef.value.videoWidth
    canvas.height = videoRef.value.videoHeight
    ctx.drawImage(videoRef.value, 0, 0, canvas.width, canvas.height)
    
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
    const code = jsQR(imageData.data, imageData.width, imageData.height)
    
    if (code) {
      console.log('扫描到二维码:', code.data)
      handleQRCode(code.data)
    }
  }, 200)
}

const handleQRCode = (data) => {
  let exhibitId = null
  
  if (data.startsWith('wenji://exhibit/')) {
    exhibitId = data.replace('wenji://exhibit/', '')
  } else if (data.startsWith('http') && data.includes('exhibit=')) {
    const url = new URL(data)
    exhibitId = url.searchParams.get('exhibit')
  } else {
    const exhibit = exhibitsData.exhibits.find(e => 
      e.id === data || e.name.includes(data)
    )
    if (exhibit) {
      exhibitId = exhibit.id
    }
  }
  
  if (exhibitId) {
    const exhibit = exhibitsData.exhibits.find(e => e.id === exhibitId)
    if (exhibit) {
      stopQRScan()
      currentExhibit.value = exhibit
      isTracking.value = true
    }
  }
}

const stopQRScan = () => {
  if (qrScanInterval) {
    clearInterval(qrScanInterval)
    qrScanInterval = null
  }
}

const initAFrameAR = async () => {
  await nextTick()
  
  const scene = document.querySelector('a-scene')
  if (scene) {
    scene.addEventListener('loaded', () => {
      console.log('A-Frame 场景加载完成')
      
      const target = document.querySelector('[mindar-image-target]')
      if (target) {
        target.addEventListener('targetFound', () => {
          console.log('识别到目标')
          isTracking.value = true
        })
        
        target.addEventListener('targetLost', () => {
          console.log('目标丢失')
          isTracking.value = false
        })
      }
    })
  }
}

const simulateRecognition = () => {
  const qinghuaci = exhibitsData.exhibits.find(e => e.id === 'qinghuaci')
  if (qinghuaci) {
    currentExhibit.value = qinghuaci
    isTracking.value = true
  }
}

const closeTracking = () => {
  isTracking.value = false
  
  if (!hasMindFiles.value) {
    startQRScan()
  }
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

const handleBack = () => {
  cleanup()
  router.back()
}

const cleanup = () => {
  stopQRScan()
  
  if (stream) {
    stream.getTracks().forEach(track => track.stop())
    stream = null
  }
  
  if (videoRef.value) {
    videoRef.value.srcObject = null
  }
}

onMounted(() => {
  const exhibitId = route.query.exhibit
  if (exhibitId) {
    const exhibit = exhibitsData.exhibits.find(e => e.id === exhibitId)
    if (exhibit) {
      currentExhibit.value = exhibit
      isTracking.value = true
    }
  }
  
  initAR()
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

.back-btn {
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

.placeholder {
  width: 44px;
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

.demo-section {
  position: absolute;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 15;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.demo-hint {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  margin: 0;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
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
