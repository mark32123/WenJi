<template>
  <div class="time-slider">
    <div class="slider-track">
      <div 
        class="slider-fill"
        :style="fillStyle"
      />
      <div
        class="slider-thumb"
        :style="thumbStyle"
        @mousedown="startDrag"
        @touchstart="startDrag"
      >
        <div class="thumb-handle">
          <div class="handle-line" />
          <div class="handle-line" />
          <div class="handle-line" />
        </div>
      </div>
    </div>
    
    <div class="slider-labels">
      <span 
        v-for="(mark, index) in marks" 
        :key="index"
        :class="['label-mark', { active: isMarkActive(index) }]"
        @click="handleMarkClick(index)"
      >
        {{ mark.label }}
      </span>
    </div>
    
    <div v-if="showValue" class="current-value">
      <span class="value-text font-serif">{{ displayValue }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: {
    type: Number,
    default: 0
  },
  min: {
    type: Number,
    default: 0
  },
  max: {
    type: Number,
    default: 100
  },
  step: {
    type: Number,
    default: 1
  },
  marks: {
    type: Array,
    default: () => []
  },
  showValue: {
    type: Boolean,
    default: true
  },
  formatValue: {
    type: Function,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const isDragging = ref(false)
const trackRef = ref(null)

const percentage = computed(() => {
  const range = props.max - props.min
  if (range === 0) return 0
  return ((props.modelValue - props.min) / range) * 100
})

const fillStyle = computed(() => ({
  width: `${percentage.value}%`
}))

const thumbStyle = computed(() => ({
  left: `${percentage.value}%`
}))

const displayValue = computed(() => {
  if (props.formatValue) {
    return props.formatValue(props.modelValue)
  }
  return props.modelValue
})

const isMarkActive = (index) => {
  if (!props.marks[index]) return false
  const markValue = props.marks[index].value
  return props.modelValue >= markValue
}

const handleMarkClick = (index) => {
  if (props.marks[index]) {
    const newValue = props.marks[index].value
    emit('update:modelValue', newValue)
    emit('change', newValue)
  }
}

const startDrag = (event) => {
  event.preventDefault()
  isDragging.value = true
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', onDrag)
  document.addEventListener('touchend', stopDrag)
}

const onDrag = (event) => {
  if (!isDragging.value) return
  
  const track = event.target.closest('.slider-track')
  if (!track) return
  
  const rect = track.getBoundingClientRect()
  const clientX = event.type.includes('touch') ? event.touches[0].clientX : event.clientX
  
  let newPercentage = (clientX - rect.left) / rect.width
  newPercentage = Math.max(0, Math.min(1, newPercentage))
  
  const range = props.max - props.min
  let newValue = props.min + newPercentage * range
  
  if (props.step > 0) {
    newValue = Math.round(newValue / props.step) * props.step
  }
  
  newValue = Math.max(props.min, Math.min(props.max, newValue))
  
  emit('update:modelValue', newValue)
}

const stopDrag = () => {
  isDragging.value = false
  emit('change', props.modelValue)
  
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)
}

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)
})
</script>

<style scoped>
.time-slider {
  position: relative;
  width: 100%;
  padding: 8px 0;
}

.slider-track {
  position: relative;
  height: 6px;
  background: linear-gradient(
    90deg,
    rgba(45, 64, 89, 0.1) 0%,
    rgba(45, 64, 89, 0.15) 100%
  );
  border-radius: 3px;
  cursor: pointer;
}

.slider-track::before {
  content: '';
  position: absolute;
  inset: -2px;
  background: linear-gradient(
    180deg,
    rgba(168, 218, 220, 0.3) 0%,
    transparent 50%,
    rgba(168, 218, 220, 0.2) 100%
  );
  border-radius: 5px;
  pointer-events: none;
}

.slider-fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: linear-gradient(
    90deg,
    #A8DADC 0%,
    #89CBCD 100%
  );
  border-radius: 3px;
  transition: width 0.15s ease-out;
}

.slider-thumb {
  position: absolute;
  top: 50%;
  width: 24px;
  height: 40px;
  transform: translate(-50%, -50%);
  cursor: grab;
  z-index: 10;
}

.slider-thumb:active {
  cursor: grabbing;
}

.thumb-handle {
  width: 100%;
  height: 100%;
  background: linear-gradient(
    180deg,
    #2D4059 0%,
    #3A5570 50%,
    #2D4059 100%
  );
  border-radius: 4px;
  box-shadow: 
    0 2px 8px rgba(45, 64, 89, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 2px;
  padding: 6px 4px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.slider-thumb:hover .thumb-handle {
  transform: scale(1.05);
  box-shadow: 
    0 4px 12px rgba(45, 64, 89, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.handle-line {
  width: 12px;
  height: 1px;
  background: rgba(168, 218, 220, 0.6);
  border-radius: 1px;
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  padding: 0 4px;
}

.label-mark {
  font-family: 'Noto Serif SC', serif;
  font-size: 11px;
  color: #8B9A9C;
  cursor: pointer;
  transition: color 0.3s ease;
  user-select: none;
}

.label-mark:hover {
  color: #2D4059;
}

.label-mark.active {
  color: #2D4059;
  font-weight: 500;
}

.current-value {
  position: absolute;
  top: -32px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(45, 64, 89, 0.9);
  color: #F0F4F8;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.2s ease;
  pointer-events: none;
}

.time-slider:hover .current-value,
.time-slider:active .current-value {
  opacity: 1;
}

.value-text {
  letter-spacing: 0.05em;
}
</style>
