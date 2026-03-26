<template>
  <div class="admin-tools">
    <div class="admin-header">
      <button class="back-btn" @click="handleBack">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </button>
      <h2 class="header-title font-serif">管理工具</h2>
    </div>
    
    <div class="tabs">
      <button 
        class="tab-btn" 
        :class="{ active: activeTab === 'compiler' }"
        @click="activeTab = 'compiler'"
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
          <circle cx="8.5" cy="8.5" r="1.5"/>
          <polyline points="21 15 16 10 5 21"/>
        </svg>
        目标编译
      </button>
      <button 
        class="tab-btn" 
        :class="{ active: activeTab === 'qrcode' }"
        @click="activeTab = 'qrcode'"
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="3" width="7" height="7"/>
          <rect x="14" y="3" width="7" height="7"/>
          <rect x="3" y="14" width="7" height="7"/>
          <rect x="14" y="14" width="7" height="7"/>
        </svg>
        二维码生成
      </button>
    </div>
    
    <div class="tab-content">
      <div v-show="activeTab === 'compiler'" class="compiler-panel">
        <div class="upload-section">
          <h3 class="section-title">上传目标图片</h3>
          <p class="section-desc">选择宣传册/海报图片作为AR识别目标</p>
          
          <div 
            class="upload-area"
            :class="{ dragging: isDragging }"
            @dragover.prevent="isDragging = true"
            @dragleave="isDragging = false"
            @drop.prevent="handleDrop"
            @click="triggerUpload"
          >
            <input 
              type="file" 
              ref="fileInput" 
              accept="image/*" 
              @change="handleFileSelect"
              hidden
            />
            <div v-if="!previewImage" class="upload-placeholder">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/>
                <line x1="12" y1="3" x2="12" y2="15"/>
              </svg>
              <p>点击或拖拽图片到此处</p>
            </div>
            <img v-else :src="previewImage" class="preview-image" />
          </div>
        </div>
        
        <div v-if="previewImage" class="info-section">
          <div class="quality-check">
            <h4 class="check-title">图片质量检测</h4>
            <div class="quality-items">
              <div class="quality-item" :class="{ good: qualityScore > 60 }">
                <span class="quality-label">特征点</span>
                <span class="quality-value">{{ featurePoints }}</span>
              </div>
              <div class="quality-item" :class="{ good: qualityScore > 60 }">
                <span class="quality-label">评分</span>
                <span class="quality-value">{{ qualityScore }}/100</span>
              </div>
            </div>
            <div class="quality-bar">
              <div class="quality-fill" :style="{ width: qualityScore + '%' }"></div>
            </div>
          </div>
          
          <div class="compile-guide">
            <h4 class="guide-title">编译步骤</h4>
            <ol class="guide-steps">
              <li>点击下方按钮打开在线编译器</li>
              <li>上传此图片</li>
              <li>下载生成的 .mind 文件</li>
              <li>将文件放入 <code>public/targets/</code></li>
            </ol>
          </div>
          
          <button class="compile-btn" @click="compileTarget">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/>
              <polyline points="15 3 21 3 21 9"/>
              <line x1="10" y1="14" x2="21" y2="3"/>
            </svg>
            打开在线编译器
          </button>
        </div>
        
        <div class="tips-section">
          <h3 class="tips-title">使用提示</h3>
          <ul class="tips-list">
            <li>推荐使用高分辨率图片（至少 1000x1000）</li>
            <li>宣传册、海报、展板效果最佳</li>
            <li>避免纯色或渐变背景</li>
          </ul>
        </div>
      </div>
      
      <div v-show="activeTab === 'qrcode'" class="qrcode-panel">
        <div class="qr-section">
          <h3 class="section-title">展品二维码</h3>
          <p class="section-desc">为展品生成二维码，放置在展柜旁供用户扫描</p>
          
          <div class="exhibit-selector">
            <label class="input-label">选择展品</label>
            <select v-model="selectedExhibitId" class="select-input">
              <option value="">请选择展品</option>
              <option v-for="exhibit in exhibits" :key="exhibit.id" :value="exhibit.id">
                {{ exhibit.name }} - {{ exhibit.metadata?.era }}
              </option>
            </select>
          </div>
          
          <div v-if="selectedExhibit" class="qr-preview">
            <div class="qr-card">
              <canvas ref="qrCanvas" class="qr-canvas"></canvas>
              <div class="qr-info">
                <h4 class="exhibit-name font-serif">{{ selectedExhibit.name }}</h4>
                <p class="exhibit-meta">{{ selectedExhibit.metadata?.era }} · {{ selectedExhibit.metadata?.museum }}</p>
              </div>
            </div>
            
            <div class="qr-actions">
              <button class="action-btn primary" @click="downloadQR">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                  <polyline points="7 10 12 15 17 10"/>
                  <line x1="12" y1="15" x2="12" y2="3"/>
                </svg>
                下载二维码
              </button>
              <button class="action-btn secondary" @click="copyLink">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                </svg>
                复制链接
              </button>
            </div>
          </div>
          
          <div class="batch-section">
            <button class="batch-btn" @click="generateAll">
              批量生成全部二维码
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="showToast" class="toast">{{ toastMessage }}</div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import exhibitsData from '@/data/exhibits.json'
import QRCode from 'qrcode'

const router = useRouter()
const exhibits = exhibitsData.exhibits

const activeTab = ref('compiler')

const fileInput = ref(null)
const previewImage = ref(null)
const selectedFile = ref(null)
const isDragging = ref(false)
const targetName = ref('')
const featurePoints = ref(0)
const qualityScore = ref(0)

const selectedExhibitId = ref('')
const qrCanvas = ref(null)
const showToast = ref(false)
const toastMessage = ref('')

const selectedExhibit = computed(() => {
  return exhibits.find(e => e.id === selectedExhibitId.value)
})

const qrLink = computed(() => {
  if (!selectedExhibit.value) return ''
  return `${window.location.origin}/ar?exhibit=${selectedExhibit.value.id}`
})

watch(selectedExhibitId, () => {
  nextTick(() => {
    generateQR()
  })
})

const generateQR = async () => {
  if (!selectedExhibit.value || !qrCanvas.value) return
  
  try {
    await QRCode.toCanvas(qrCanvas.value, qrLink.value, {
      width: 180,
      margin: 2,
      color: { dark: '#2D4059', light: '#ffffff' }
    })
  } catch (err) {
    console.error('生成二维码失败:', err)
  }
}

const downloadQR = () => {
  if (!qrCanvas.value) return
  
  const link = document.createElement('a')
  link.download = `${selectedExhibit.value.id}-qrcode.png`
  link.href = qrCanvas.value.toDataURL('image/png')
  link.click()
  
  showToastMessage('二维码已下载')
}

const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(qrLink.value)
    showToastMessage('链接已复制')
  } catch (err) {
    console.error('复制失败:', err)
  }
}

const generateAll = async () => {
  showToastMessage('正在生成...')
  
  for (const exhibit of exhibits) {
    const link = `${window.location.origin}/ar?exhibit=${exhibit.id}`
    try {
      const canvas = document.createElement('canvas')
      await QRCode.toCanvas(canvas, link, {
        width: 180,
        margin: 2,
        color: { dark: '#2D4059', light: '#ffffff' }
      })
      
      const downloadLink = document.createElement('a')
      downloadLink.download = `${exhibit.id}-qrcode.png`
      downloadLink.href = canvas.toDataURL('image/png')
      downloadLink.click()
      
      await new Promise(resolve => setTimeout(resolve, 300))
    } catch (err) {
      console.error(`生成 ${exhibit.name} 失败:`, err)
    }
  }
  
  showToastMessage('全部生成完成')
}

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileSelect = (e) => {
  const file = e.target.files?.[0]
  if (file) processFile(file)
}

const handleDrop = (e) => {
  isDragging.value = false
  const file = e.dataTransfer.files?.[0]
  if (file && file.type.startsWith('image/')) processFile(file)
}

const processFile = (file) => {
  selectedFile.value = file
  targetName.value = file.name.replace(/\.[^/.]+$/, '')
  
  const reader = new FileReader()
  reader.onload = (e) => {
    previewImage.value = e.target.result
    analyzeImage(e.target.result)
  }
  reader.readAsDataURL(file)
}

const analyzeImage = (dataUrl) => {
  const img = new Image()
  img.onload = () => {
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    canvas.width = img.width
    canvas.height = img.height
    ctx.drawImage(img, 0, 0)
    
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
    const data = imageData.data
    
    let corners = 0
    for (let i = 0; i < data.length; i += 16) {
      const brightness = (data[i] + data[i + 1] + data[i + 2]) / 3
      if (brightness > 30 && brightness < 225) corners++
    }
    
    featurePoints.value = Math.min(corners, 9999)
    qualityScore.value = Math.min(100, Math.floor(corners / 100))
  }
  img.src = dataUrl
}

const compileTarget = () => {
  showToastMessage('正在跳转到在线编译器...')
  
  localStorage.setItem('mindar_compiler_image', previewImage.value)
  localStorage.setItem('mindar_compiler_name', targetName.value)
  
  window.open('https://hiukim.github.io/mind-ar-js-doc/tools/compiler/', '_blank')
}

const handleBack = () => {
  router.back()
}

const showToastMessage = (message) => {
  toastMessage.value = message
  showToast.value = true
  setTimeout(() => { showToast.value = false }, 2000)
}
</script>

<style scoped>
.admin-tools {
  min-height: 100vh;
  background: #FDFBF7;
}

.admin-header {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: #2D4059;
}

.back-btn {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.header-title {
  flex: 1;
  text-align: center;
  font-size: 18px;
  color: #fff;
  margin: 0 40px 0 16px;
}

.tabs {
  display: flex;
  padding: 12px 16px;
  gap: 8px;
  background: #fff;
  border-bottom: 1px solid rgba(45, 64, 89, 0.1);
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px;
  background: transparent;
  border: none;
  border-radius: 10px;
  color: #8B9A9C;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn.active {
  background: #2D4059;
  color: #fff;
}

.tab-content {
  padding: 16px;
}

.section-title {
  font-size: 16px;
  color: #2D4059;
  margin: 0 0 4px 0;
}

.section-desc {
  font-size: 13px;
  color: #8B9A9C;
  margin: 0 0 16px 0;
}

.upload-area {
  border: 2px dashed rgba(45, 64, 89, 0.2);
  border-radius: 12px;
  padding: 32px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-area.dragging {
  border-color: #C9A227;
  background: rgba(201, 162, 39, 0.05);
}

.upload-placeholder {
  color: #8B9A9C;
}

.upload-placeholder svg {
  margin-bottom: 8px;
  opacity: 0.5;
}

.preview-image {
  max-width: 100%;
  max-height: 160px;
  border-radius: 8px;
}

.info-section,
.result-section,
.qr-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-top: 16px;
  box-shadow: 0 2px 8px rgba(45, 64, 89, 0.06);
}

.quality-check {
  margin-bottom: 16px;
}

.check-title {
  font-size: 13px;
  color: #2D4059;
  margin: 0 0 10px 0;
}

.quality-items {
  display: flex;
  justify-content: space-around;
  margin-bottom: 10px;
}

.quality-item {
  text-align: center;
}

.quality-item.good .quality-value {
  color: #4CAF50;
}

.quality-label {
  font-size: 11px;
  color: #8B9A9C;
}

.quality-value {
  font-size: 16px;
  font-weight: 600;
  color: #2D4059;
}

.quality-bar {
  height: 4px;
  background: rgba(45, 64, 89, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.quality-fill {
  height: 100%;
  background: linear-gradient(90deg, #C9A227, #4CAF50);
}

.input-label {
  display: block;
  font-size: 13px;
  color: #2D4059;
  margin-bottom: 6px;
}

.text-input,
.select-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid rgba(45, 64, 89, 0.15);
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  background: #fff;
}

.text-input:focus,
.select-input:focus {
  border-color: #C9A227;
}

.target-name {
  margin-bottom: 16px;
}

.compile-guide {
  margin-bottom: 16px;
  padding: 12px;
  background: rgba(201, 162, 39, 0.1);
  border-radius: 8px;
}

.guide-title {
  font-size: 13px;
  color: #2D4059;
  margin: 0 0 8px 0;
}

.guide-steps {
  margin: 0;
  padding-left: 20px;
}

.guide-steps li {
  font-size: 12px;
  color: #6B7280;
  margin-bottom: 4px;
}

.guide-steps code {
  background: rgba(45, 64, 89, 0.1);
  padding: 1px 4px;
  border-radius: 3px;
  font-size: 11px;
}

.compile-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.compile-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-spinner.small {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.result-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: rgba(201, 162, 39, 0.1);
  border-radius: 8px;
  margin-bottom: 10px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2D4059;
  font-size: 14px;
}

.download-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: #C9A227;
  border: none;
  border-radius: 6px;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}

.usage-hint {
  font-size: 12px;
  color: #8B9A9C;
  margin: 0;
}

.usage-hint code {
  background: rgba(45, 64, 89, 0.1);
  padding: 2px 4px;
  border-radius: 3px;
}

.tips-section {
  margin-top: 16px;
  padding: 16px;
  background: rgba(45, 64, 89, 0.03);
  border-radius: 12px;
}

.tips-title {
  font-size: 13px;
  color: #2D4059;
  margin: 0 0 10px 0;
}

.tips-list {
  margin: 0;
  padding-left: 16px;
}

.tips-list li {
  font-size: 12px;
  color: #8B9A9C;
  margin-bottom: 6px;
}

.exhibit-selector {
  margin-bottom: 16px;
}

.qr-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-card {
  background: linear-gradient(135deg, #F8F5F0 0%, #fff 100%);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  border: 1px solid rgba(201, 162, 39, 0.2);
}

.qr-canvas {
  border-radius: 8px;
}

.qr-info {
  margin-top: 12px;
}

.exhibit-name {
  font-size: 16px;
  color: #2D4059;
  margin: 0 0 4px 0;
}

.exhibit-meta {
  font-size: 12px;
  color: #8B9A9C;
  margin: 0;
}

.qr-actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
}

.action-btn.primary {
  background: linear-gradient(135deg, #C9A227, #D4AF37);
  color: #fff;
}

.action-btn.secondary {
  background: #F3F4F6;
  color: #374151;
}

.batch-section {
  margin-top: 20px;
  text-align: center;
}

.batch-btn {
  padding: 10px 20px;
  background: #2D4059;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
}

.toast {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(45, 64, 89, 0.95);
  color: #fff;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 13px;
  z-index: 100;
}
</style>
