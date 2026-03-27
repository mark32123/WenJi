<!--
  RewardSignature.vue - 文物收藏卡片组件
  
  功能说明：
  1. 展示文物的详细信息（图片、名称、诗词、朝代、地点）
  2. 支持图片预览（点击图片放大查看）
  3. 收藏功能（收入藏经阁）
  4. 保存图片功能
  5. 显示获得的阅历值
  
  主要交互：
  - 点击图片打开预览弹窗
  - 点击"收入藏经阁"收藏文物
  - 点击"保存图片"保存卡片图片
  
  Props：
  - artifact: 文物数据对象（必需）
  - poem: 自定义诗词（可选）
  - experienceGained: 获得的阅历值（默认10）
  - animateOnMount: 是否在挂载时播放动画（默认true）
  
  Events：
  - collect: 收藏事件
  - save: 保存事件
-->
<template>
  <div class="reward-signature">
    <PaperSurface class="signature-card" hoverable bordered>
      <div class="card-layout">
        <div class="artifact-image-wrapper" @click="openImagePreview">
          <div class="artifact-image">
            <img 
              v-if="artifact.imageUrl"
              :src="artifact.imageUrl"
              :alt="artifact.name"
              @error="handleImageError"
            />
            <div v-else class="image-placeholder">
              <span>🏺</span>
            </div>
          </div>
          <div class="image-overlay">
            <span class="zoom-icon">🔍</span>
          </div>
        </div>
        
        <div class="info-section">
          <div class="artifact-header">
            <h3 class="artifact-name font-serif">{{ artifact.name }}</h3>
          </div>
          
          <div class="poem-section">
            <p class="poem-text font-serif">{{ displayPoem }}</p>
          </div>
          
          <div class="meta-section">
            <span class="meta-item font-serif">{{ artifact.dynasty }}</span>
            <span v-if="artifact.location" class="meta-item">{{ artifact.location }}</span>
          </div>
        </div>
      </div>
      
      <div class="card-texture" />
    </PaperSurface>
    
    <div class="experience-gain" v-if="experienceGained > 0">
      <span class="gain-label">阅历</span>
      <span class="gain-value">+{{ experienceGained }}</span>
    </div>
    
    <div class="action-buttons">
      <InkButton
        type="primary"
        @click="handleCollect"
      >
        收入藏经阁
      </InkButton>
      
      <InkButton
        type="ghost"
        @click="handleSave"
      >
        保存图片
      </InkButton>
    </div>
    
    <Teleport to="body">
      <transition name="preview">
        <div v-if="showPreview" class="image-preview-overlay" @click="closeImagePreview">
          <div class="preview-container">
            <img 
              v-if="artifact.imageUrl && !imageError"
              :src="artifact.imageUrl"
              :alt="artifact.name"
              class="preview-image"
              @error="handleImageError"
            />
            <div v-else class="preview-placeholder">
              <span>🏺</span>
              <p>图片暂不可用</p>
            </div>
            <div class="preview-info">
              <h3 class="preview-title font-serif">{{ artifact.name }}</h3>
              <p class="preview-meta">{{ artifact.dynasty }} · {{ artifact.location }}</p>
            </div>
            <button class="close-preview-btn" @click.stop="closeImagePreview">×</button>
          </div>
        </div>
      </transition>
      
      <transition name="fade">
        <div v-if="showCollectedMessage" class="collected-overlay">
          <div class="collected-content">
            <span class="collected-icon">✓</span>
            <span class="collected-text font-serif">已收入藏经阁</span>
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script setup>
/**
 * RewardSignature - 文物收藏卡片组件
 * 
 * 用于展示用户探索文物后获得的收藏卡片
 * 包含文物图片、名称、诗词、朝代等信息
 */

import { ref, computed, onMounted } from 'vue'
import PaperSurface from '@/components/atoms/PaperSurface.vue'
import InkButton from '@/components/atoms/InkButton.vue'

// ==================== Props 定义 ====================
const props = defineProps({
  /** 文物数据对象 */
  artifact: {
    type: Object,
    required: true,
    validator: (v) => v && v.id && v.name
  },
  /** 自定义诗词 */
  poem: {
    type: String,
    default: ''
  },
  /** 获得的阅历值 */
  experienceGained: {
    type: Number,
    default: 10
  },
  /** 是否在挂载时播放动画 */
  animateOnMount: {
    type: Boolean,
    default: true
  }
})

// ==================== 事件定义 ====================
const emit = defineEmits(['collect', 'save'])

// ==================== 响应式状态 ====================
const animatePoem = ref(false)        // 是否播放诗词动画
const showCollectedMessage = ref(false) // 是否显示收藏成功提示
const showPreview = ref(false)        // 是否显示图片预览
const imageError = ref(false)         // 图片是否加载失败

// ==================== 计算属性 ====================

/**
 * 显示的诗词文本
 * 优先级：props.poem > artifact.poem > 默认文本
 */
const displayPoem = computed(() => {
  return props.poem || props.artifact.poem || '岁月无声，文物有灵'
})

// ==================== 方法定义 ====================

/** 处理图片加载错误 */
const handleImageError = () => {
  imageError.value = true
}

/** 打开图片预览弹窗 */
const openImagePreview = () => {
  showPreview.value = true
  document.body.style.overflow = 'hidden' // 禁止页面滚动
}

/** 关闭图片预览弹窗 */
const closeImagePreview = () => {
  showPreview.value = false
  document.body.style.overflow = '' // 恢复页面滚动
}

/**
 * 处理收藏按钮点击
 * 显示收藏成功提示并触发 collect 事件
 */
const handleCollect = () => {
  showCollectedMessage.value = true
  emit('collect', props.artifact)
  
  // 2秒后自动隐藏提示
  setTimeout(() => {
    showCollectedMessage.value = false
  }, 2000)
}

/**
 * 处理保存按钮点击
 * 触发 save 事件，由父组件处理保存逻辑
 */
const handleSave = () => {
  emit('save', props.artifact)
}

/**
 * 组件挂载时的初始化
 * 延迟播放诗词动画
 */
onMounted(() => {
  if (props.animateOnMount) {
    setTimeout(() => {
      animatePoem.value = true
    }, 500)
  }
})
</script>

<style scoped>
.reward-signature {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  padding: 24px;
  padding-bottom: 100px;
  background: linear-gradient(
    180deg,
    #F0F4F8 0%,
    #E8ECEF 50%,
    #F0F4F8 100%
  );
}

.signature-card {
  width: 100%;
  max-width: 400px;
  position: relative;
  overflow: visible;
}

.card-layout {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 24px;
}

.artifact-image-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
}

.artifact-image {
  width: 100%;
  aspect-ratio: 4 / 3;
  border-radius: 8px;
  overflow: hidden;
  background: rgba(45, 64, 89, 0.05);
}

.artifact-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.artifact-image-wrapper:hover .artifact-image img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(26, 26, 26, 0);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s ease;
}

.artifact-image-wrapper:hover .image-overlay {
  background: rgba(26, 26, 26, 0.3);
}

.zoom-icon {
  font-size: 32px;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.3s ease;
}

.artifact-image-wrapper:hover .zoom-icon {
  opacity: 1;
  transform: scale(1);
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  opacity: 0.3;
  background: linear-gradient(
    135deg,
    rgba(168, 218, 220, 0.2) 0%,
    rgba(139, 154, 156, 0.2) 100%
  );
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.artifact-header {
  text-align: center;
}

.artifact-name {
  font-size: 24px;
  color: #2D4059;
  margin: 0;
  letter-spacing: 0.15em;
}

.poem-section {
  text-align: center;
  padding: 12px 0;
}

.poem-text {
  font-size: 14px;
  color: #5A6B6E;
  line-height: 2;
  margin: 0;
  letter-spacing: 0.1em;
}

.meta-section {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-item {
  font-size: 12px;
  color: #8B9A9C;
}

.card-texture {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
  opacity: 0.02;
  pointer-events: none;
}

.experience-gain {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(168, 218, 220, 0.2);
  border-radius: 20px;
}

.gain-label {
  font-size: 12px;
  color: #8B9A9C;
  font-family: 'Noto Serif SC', serif;
}

.gain-value {
  font-size: 16px;
  color: #2D4059;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 300px;
}

.image-preview-overlay {
  position: fixed;
  inset: 0;
  background: rgba(26, 26, 26, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.preview-container {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 8px;
}

.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 60px 40px;
  background: rgba(45, 64, 89, 0.2);
  border-radius: 8px;
}

.preview-placeholder span {
  font-size: 64px;
  opacity: 0.5;
}

.preview-placeholder p {
  font-size: 14px;
  color: rgba(245, 242, 235, 0.6);
  margin: 0;
}

.preview-info {
  text-align: center;
  margin-top: 16px;
}

.preview-title {
  font-size: 24px;
  color: #F5F2EB;
  margin: 0 0 8px 0;
  letter-spacing: 0.1em;
}

.preview-meta {
  font-size: 14px;
  color: rgba(245, 242, 235, 0.6);
  margin: 0;
}

.close-preview-btn {
  position: absolute;
  top: -40px;
  right: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(245, 242, 235, 0.1);
  border: none;
  color: #F5F2EB;
  font-size: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s ease;
}

.close-preview-btn:hover {
  background: rgba(245, 242, 235, 0.2);
}

.collected-overlay {
  position: fixed;
  inset: 0;
  background: rgba(26, 26, 26, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
}

.collected-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 32px 48px;
  background: rgba(245, 242, 235, 0.95);
  border-radius: 12px;
}

.collected-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #2D4059;
  color: #F5F2EB;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.collected-text {
  font-size: 16px;
  color: #2D4059;
  letter-spacing: 0.1em;
}

.preview-enter-active,
.preview-leave-active {
  transition: opacity 0.3s ease;
}

.preview-enter-from,
.preview-leave-to {
  opacity: 0;
}

.preview-enter-active .preview-container,
.preview-leave-active .preview-container {
  transition: transform 0.3s ease;
}

.preview-enter-from .preview-container,
.preview-leave-to .preview-container {
  transform: scale(0.9);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 428px) {
  .reward-signature {
    padding: 16px;
    padding-bottom: 80px;
    gap: 16px;
  }
  
  .signature-card {
    max-width: 100%;
  }
  
  .card-layout {
    padding: 16px;
    gap: 16px;
  }
  
  .info-section {
    gap: 12px;
  }
  
  .artifact-name {
    font-size: 20px;
  }
  
  .poem-text {
    font-size: 13px;
  }
  
  .action-buttons {
    max-width: 100%;
  }
  
  .preview-image {
    max-height: 60vh;
  }
  
  .preview-title {
    font-size: 20px;
  }
}
</style>
