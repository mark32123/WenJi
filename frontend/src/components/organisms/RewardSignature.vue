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
import { ref, computed, onMounted } from 'vue'
import PaperSurface from '@/components/atoms/PaperSurface.vue'
import InkButton from '@/components/atoms/InkButton.vue'

const props = defineProps({
  artifact: {
    type: Object,
    required: true,
    validator: (v) => v && v.id && v.name
  },
  poem: {
    type: String,
    default: ''
  },
  experienceGained: {
    type: Number,
    default: 10
  },
  animateOnMount: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['collect', 'save'])

const animatePoem = ref(false)
const showCollectedMessage = ref(false)
const showPreview = ref(false)
const imageError = ref(false)

const displayPoem = computed(() => {
  return props.poem || props.artifact.poem || '岁月无声，文物有灵'
})

const handleImageError = () => {
  imageError.value = true
}

const openImagePreview = () => {
  showPreview.value = true
  document.body.style.overflow = 'hidden'
}

const closeImagePreview = () => {
  showPreview.value = false
  document.body.style.overflow = ''
}

const handleCollect = () => {
  showCollectedMessage.value = true
  emit('collect', props.artifact)
  
  setTimeout(() => {
    showCollectedMessage.value = false
  }, 2000)
}

const handleSave = () => {
  emit('save', props.artifact)
}

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
