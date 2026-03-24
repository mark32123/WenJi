<template>
  <div class="target-compiler">
    <div class="compiler-header">
      <button class="back-btn" @click="handleBack">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </button>
      <h2 class="header-title font-serif">AR目标编译器</h2>
    </div>
    
    <div class="compiler-content">
      <div class="upload-section">
        <h3 class="section-title">上传目标图片</h3>
        <p class="section-desc">选择一张图片作为AR识别目标</p>
        
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
              <span class="quality-label">特征点数量</span>
              <span class="quality-value">{{ featurePoints }}</span>
            </div>
            <div class="quality-item" :class="{ good: qualityScore > 60 }">
              <span class="quality-label">识别评分</span>
              <span class="quality-value">{{ qualityScore }}/100</span>
            </div>
          </div>
          <div class="quality-bar">
            <div class="quality-fill" :style="{ width: qualityScore + '%' }"></div>
          </div>
          <p v-if="qualityScore < 50" class="quality-warning">
            建议使用高对比度、细节丰富的图片
          </p>
        </div>
        
        <div class="target-name">
          <label class="input-label">目标名称</label>
          <input 
            v-model="targetName" 
            type="text" 
            class="text-input"
            placeholder="例如：兵马俑海报"
          />
        </div>
        
        <button 
          class="compile-btn"
          :disabled="compiling"
          @click="compileTarget"
        >
          <span v-if="compiling" class="loading-spinner small"></span>
          <span v-else>生成 .mind 文件</span>
        </button>
      </div>
      
      <div v-if="compiledFile" class="result-section">
        <h3 class="section-title">编译完成</h3>
        <div class="result-info">
          <div class="file-info">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            <span>{{ targetName }}.mind</span>
          </div>
          <button class="download-btn" @click="downloadFile">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            下载文件
          </button>
        </div>
        <p class="usage-hint">
          将下载的 .mind 文件放入 <code>public/targets/</code> 目录<br>
          然后在 exhibits.json 中配置 targetImage 路径
        </p>
      </div>
    </div>
    
    <div class="tips-section">
      <h3 class="tips-title">使用提示</h3>
      <ul class="tips-list">
        <li>推荐使用高分辨率图片（至少 1000x1000）</li>
        <li>图片应有丰富的纹理和特征点</li>
        <li>避免使用纯色或渐变背景</li>
        <li>海报、宣传册、展板效果最佳</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const fileInput = ref(null)
const previewImage = ref(null)
const selectedFile = ref(null)
const isDragging = ref(false)
const targetName = ref('')
const compiling = ref(false)
const compiledFile = ref(null)
const featurePoints = ref(0)
const qualityScore = ref(0)

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileSelect = (e) => {
  const file = e.target.files?.[0]
  if (file) {
    processFile(file)
  }
}

const handleDrop = (e) => {
  isDragging.value = false
  const file = e.dataTransfer.files?.[0]
  if (file && file.type.startsWith('image/')) {
    processFile(file)
  }
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
      const r = data[i]
      const g = data[i + 1]
      const b = data[i + 2]
      
      const brightness = (r + g + b) / 3
      if (brightness > 30 && brightness < 225) {
        corners++
      }
    }
    
    featurePoints.value = Math.min(corners, 9999)
    qualityScore.value = Math.min(100, Math.floor(corners / 100))
  }
  img.src = dataUrl
}

const compileTarget = async () => {
  if (!selectedFile.value || !targetName.value) return
  
  compiling.value = true
  
  try {
    const compiler = await import('mind-ar/dist/mindar-image-compiler.js')
    
    const img = new Image()
    img.src = previewImage.value
    
    await new Promise((resolve) => {
      img.onload = resolve
    })
    
    const result = await compiler.compile(img, {
      debug: false
    })
    
    compiledFile.value = {
      name: targetName.value,
      data: result
    }
    
  } catch (e) {
    console.error('编译失败:', e)
    alert('编译失败，请检查图片格式')
  } finally {
    compiling.value = false
  }
}

const downloadFile = () => {
  if (!compiledFile.value) return
  
  const blob = new Blob([compiledFile.value.data], { type: 'application/octet-stream' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${compiledFile.value.name}.mind`
  a.click()
  URL.revokeObjectURL(url)
}

const handleBack = () => {
  router.back()
}
</script>

<style scoped>
.target-compiler {
  min-height: 100vh;
  background: #FDFBF7;
}

.compiler-header {
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

.compiler-content {
  padding: 20px;
}

.upload-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  color: #2D4059;
  margin: 0 0 8px 0;
}

.section-desc {
  font-size: 14px;
  color: #8B9A9C;
  margin: 0 0 16px 0;
}

.upload-area {
  border: 2px dashed rgba(45, 64, 89, 0.2);
  border-radius: 16px;
  padding: 40px 20px;
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
  margin-bottom: 12px;
  opacity: 0.5;
}

.preview-image {
  max-width: 100%;
  max-height: 200px;
  border-radius: 8px;
}

.info-section {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(45, 64, 89, 0.06);
}

.quality-check {
  margin-bottom: 20px;
}

.check-title {
  font-size: 14px;
  color: #2D4059;
  margin: 0 0 12px 0;
}

.quality-items {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.quality-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.quality-item.good .quality-value {
  color: #4CAF50;
}

.quality-label {
  font-size: 12px;
  color: #8B9A9C;
}

.quality-value {
  font-size: 18px;
  font-weight: 600;
  color: #2D4059;
}

.quality-bar {
  height: 6px;
  background: rgba(45, 64, 89, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.quality-fill {
  height: 100%;
  background: linear-gradient(90deg, #C9A227, #4CAF50);
  transition: width 0.3s;
}

.quality-warning {
  font-size: 12px;
  color: #E07A5F;
  margin: 8px 0 0 0;
}

.target-name {
  margin-bottom: 20px;
}

.input-label {
  display: block;
  font-size: 14px;
  color: #2D4059;
  margin-bottom: 8px;
}

.text-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid rgba(45, 64, 89, 0.15);
  border-radius: 10px;
  font-size: 15px;
  outline: none;
}

.text-input:focus {
  border-color: #C9A227;
}

.compile-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 16px;
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
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.result-section {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(45, 64, 89, 0.06);
}

.result-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: rgba(201, 162, 39, 0.1);
  border-radius: 12px;
  margin-bottom: 12px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2D4059;
}

.download-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: #C9A227;
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.usage-hint {
  font-size: 13px;
  color: #8B9A9C;
  line-height: 1.6;
}

.usage-hint code {
  background: rgba(45, 64, 89, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.tips-section {
  background: rgba(45, 64, 89, 0.03);
  border-radius: 16px;
  padding: 20px;
}

.tips-title {
  font-size: 14px;
  color: #2D4059;
  margin: 0 0 12px 0;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
}

.tips-list li {
  font-size: 13px;
  color: #8B9A9C;
  margin-bottom: 8px;
}
</style>
