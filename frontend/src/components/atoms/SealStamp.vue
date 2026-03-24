<template>
  <div
    :class="containerClasses"
    :style="containerStyle"
    @click="handleClick"
  >
    <svg
      viewBox="0 0 100 100"
      :class="svgClasses"
    >
      <rect
        x="5"
        y="5"
        width="90"
        height="90"
        :class="borderClasses"
        rx="4"
        ry="4"
      />
      <text
        x="50"
        y="50"
        text-anchor="middle"
        dominant-baseline="central"
        :class="textClasses"
        :style="textStyle"
      >
        {{ text }}
      </text>
    </svg>
    <div
      v-if="showStampEffect"
      class="stamp-effect"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  text: {
    type: String,
    default: '已阅'
  },
  size: {
    type: String,
    default: 'md',
    validator: (v) => ['sm', 'md', 'lg', 'xl'].includes(v)
  },
  color: {
    type: String,
    default: 'zhusha',
    validator: (v) => ['zhusha', 'dailan', 'mohei', 'tianqing'].includes(v)
  },
  active: {
    type: Boolean,
    default: false
  },
  animated: {
    type: Boolean,
    default: true
  },
  rotation: {
    type: Number,
    default: -5
  }
})

const emit = defineEmits(['stamp', 'click'])

const showStampEffect = ref(false)

const sizeMap = {
  sm: { width: '32px', height: '32px', fontSize: '12px', strokeWidth: 2 },
  md: { width: '48px', height: '48px', fontSize: '16px', strokeWidth: 2.5 },
  lg: { width: '64px', height: '64px', fontSize: '20px', strokeWidth: 3 },
  xl: { width: '80px', height: '80px', fontSize: '24px', strokeWidth: 3.5 }
}

const colorMap = {
  zhusha: { fill: 'none', stroke: '#8B7355', text: '#5D4E37' },
  dailan: { fill: 'none', stroke: '#2D4059', text: '#2D4059' },
  mohei: { fill: 'none', stroke: '#1A1A1A', text: '#1A1A1A' },
  tianqing: { fill: 'none', stroke: '#A8DADC', text: '#2D4059' }
}

const containerClasses = computed(() => [
  'relative',
  'inline-flex',
  'items-center',
  'justify-content',
  'cursor-pointer',
  'select-none'
])

const containerStyle = computed(() => ({
  width: sizeMap[props.size].width,
  height: sizeMap[props.size].height,
  transform: `rotate(${props.rotation}deg)`
}))

const svgClasses = computed(() => [
  'w-full',
  'h-full',
  {
    'animate-seal-stamp': props.active && props.animated
  }
])

const borderClasses = computed(() => [
  'stroke-2'
])

const textClasses = computed(() => [
  'font-serif',
  'font-semibold'
])

const textStyle = computed(() => ({
  fontSize: sizeMap[props.size].fontSize,
  fill: colorMap[props.color].text,
  strokeWidth: sizeMap[props.size].strokeWidth,
  stroke: colorMap[props.color].stroke
}))

const handleClick = () => {
  emit('click')
}

watch(() => props.active, (newVal) => {
  if (newVal && props.animated) {
    showStampEffect.value = true
    emit('stamp')
    setTimeout(() => {
      showStampEffect.value = false
    }, 600)
  }
})
</script>

<style scoped>
.stamp-effect {
  position: absolute;
  inset: -20%;
  background: radial-gradient(circle, rgba(93, 78, 55, 0.15) 0%, transparent 70%);
  animation: ink-spread 0.6s ease-out forwards;
  pointer-events: none;
}

@keyframes ink-spread {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}
</style>
