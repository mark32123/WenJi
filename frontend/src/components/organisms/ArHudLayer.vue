<template>
  <div class="ar-hud-layer" :class="{ 'is-tracking': isTracking }">
    <div class="hud-top">
      <transition name="fade">
        <div v-if="isTracking" class="era-display">
          <span class="era-label font-serif">{{ currentEra }}</span>
          <span class="era-subtitle">{{ currentEraYears }}</span>
        </div>
      </transition>
      
      <div class="top-actions">
        <InkButton
          type="ghost"
          size="sm"
          @click="handleClose"
        >
          返回
        </InkButton>
      </div>
    </div>
    
    <div class="hud-center">
      <transition name="scale">
        <div v-if="showTargetHint && !isTracking" class="target-hint">
          <div class="hint-ring" />
          <span class="hint-text">将镜头对准文物</span>
        </div>
      </transition>
      
      <transition name="fade">
        <div v-if="showInfo && currentArtifact" class="info-bubble">
          <PaperSurface padding="sm" bordered>
            <div class="bubble-content">
              <VerticalText
                :text="currentArtifact.name"
                font-size="lg"
                font-weight="medium"
                color="dailan"
              />
              <div class="bubble-meta">
                <span class="meta-item">{{ currentArtifact.dynasty }}</span>
                <span v-if="currentArtifact.category" class="meta-divider">·</span>
                <span class="meta-item">{{ currentArtifact.category }}</span>
              </div>
            </div>
          </PaperSurface>
        </div>
      </transition>
    </div>
    
    <div class="hud-bottom">
      <div class="time-control">
        <TimeSlider
          v-model="currentYear"
          :min="minYear"
          :max="maxYear"
          :marks="eraMarks"
          :format-value="formatYear"
          @change="handleYearChange"
        />
      </div>
      
      <div class="bottom-actions">
        <InkButton
          type="secondary"
          size="sm"
          :disabled="!isTracking"
          @click="handleCollect"
        >
          收藏
        </InkButton>
        
        <InkButton
          type="ghost"
          size="sm"
          @click="handleSoundToggle"
        >
          {{ soundEnabled ? '静音' : '声音' }}
        </InkButton>
      </div>
    </div>
    
    <transition name="slide-up">
      <div v-if="showSuccessMessage" class="success-message">
        <SealStamp text="已阅" size="md" :active="true" />
        <span class="success-text">已收入藏经阁</span>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import PaperSurface from '@/components/atoms/PaperSurface.vue'
import VerticalText from '@/components/atoms/VerticalText.vue'
import InkButton from '@/components/atoms/InkButton.vue'
import SealStamp from '@/components/atoms/SealStamp.vue'
import TimeSlider from '@/components/molecules/TimeSlider.vue'

const props = defineProps({
  isTracking: {
    type: Boolean,
    default: false
  },
  currentArtifact: {
    type: Object,
    default: null
  },
  minYear: {
    type: Number,
    default: -221
  },
  maxYear: {
    type: Number,
    default: 2024
  }
})

const emit = defineEmits(['close', 'collect', 'yearChange', 'soundToggle'])

const currentYear = ref(0)
const soundEnabled = ref(true)
const showTargetHint = ref(true)
const showInfo = ref(false)
const showSuccessMessage = ref(false)

const eraMarks = computed(() => [
  { label: '秦汉', value: -200 },
  { label: '隋唐', value: 600 },
  { label: '宋元', value: 1100 },
  { label: '明清', value: 1600 },
  { label: '近代', value: 1900 }
])

const currentEra = computed(() => {
  const year = currentYear.value
  if (year < -220) return '先秦'
  if (year < 220) return '秦汉'
  if (year < 581) return '魏晋'
  if (year < 907) return '隋唐'
  if (year < 1279) return '宋元'
  if (year < 1912) return '明清'
  return '近代'
})

const currentEraYears = computed(() => {
  const year = currentYear.value
  if (year < 0) {
    return `公元前 ${Math.abs(year)} 年`
  }
  return `公元 ${year} 年`
})

const formatYear = (value) => {
  if (value < 0) {
    return `前${Math.abs(value)}`
  }
  return `${value}`
}

const handleClose = () => {
  emit('close')
}

const handleCollect = () => {
  showSuccessMessage.value = true
  emit('collect', props.currentArtifact)
  
  setTimeout(() => {
    showSuccessMessage.value = false
  }, 2000)
}

const handleYearChange = (year) => {
  emit('yearChange', year)
}

const handleSoundToggle = () => {
  soundEnabled.value = !soundEnabled.value
  emit('soundToggle', soundEnabled.value)
}

watch(() => props.isTracking, (newVal) => {
  if (newVal) {
    showTargetHint.value = false
    setTimeout(() => {
      showInfo.value = true
    }, 300)
  } else {
    showInfo.value = false
    showTargetHint.value = true
  }
})

watch(() => props.currentArtifact, (artifact) => {
  if (artifact && artifact.year) {
    currentYear.value = artifact.year
  }
})
</script>

<style scoped>
.ar-hud-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: env(safe-area-inset-top) env(safe-area-inset-right) env(safe-area-inset-bottom) env(safe-area-inset-left);
}

.ar-hud-layer > * {
  pointer-events: auto;
}

.hud-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
}

.era-display {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 12px 16px;
  background: rgba(45, 64, 89, 0.85);
  border-radius: 8px;
  backdrop-filter: blur(8px);
}

.era-label {
  font-size: 18px;
  color: #F0F4F8;
  letter-spacing: 0.1em;
}

.era-subtitle {
  font-size: 11px;
  color: rgba(240, 244, 248, 0.7);
}

.top-actions {
  display: flex;
  gap: 8px;
}

.hud-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  pointer-events: none;
}

.target-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  pointer-events: auto;
}

.hint-ring {
  width: 120px;
  height: 120px;
  border: 2px dashed rgba(168, 218, 220, 0.5);
  border-radius: 50%;
  animation: pulse-ring 2s ease-in-out infinite;
}

@keyframes pulse-ring {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
  }
}

.hint-text {
  font-size: 14px;
  color: rgba(240, 244, 248, 0.9);
  background: rgba(45, 64, 89, 0.7);
  padding: 8px 16px;
  border-radius: 20px;
  font-family: 'Noto Serif SC', serif;
}

.info-bubble {
  pointer-events: auto;
  max-width: 200px;
}

.bubble-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.bubble-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #8B9A9C;
}

.meta-divider {
  opacity: 0.5;
}

.hud-bottom {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(
    180deg,
    transparent 0%,
    rgba(26, 26, 26, 0.6) 100%
  );
}

.time-control {
  padding: 0 8px;
}

.bottom-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.success-message {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  background: rgba(245, 242, 235, 0.95);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(45, 64, 89, 0.2);
}

.success-text {
  font-size: 14px;
  color: #2D4059;
  font-family: 'Noto Serif SC', serif;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.scale-enter-active,
.scale-leave-active {
  transition: all 0.4s ease;
}

.scale-enter-from,
.scale-leave-to {
  transform: scale(0.8);
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.4s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translate(-50%, -50%) translateY(20px);
  opacity: 0;
}

@media (max-width: 428px) {
  .hud-top,
  .hud-bottom {
    padding: 12px;
  }
  
  .era-display {
    padding: 10px 12px;
  }
  
  .era-label {
    font-size: 16px;
  }
  
  .hint-ring {
    width: 100px;
    height: 100px;
  }
}
</style>
