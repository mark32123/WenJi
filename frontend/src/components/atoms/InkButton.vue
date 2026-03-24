<template>
  <button
    :class="buttonClasses"
    :disabled="disabled"
    @click="handleClick"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <span class="relative z-10">
      <slot>{{ label }}</slot>
    </span>
    <span
      v-if="showInkEffect"
      class="ink-effect"
      :style="inkStyle"
    />
  </button>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  label: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'primary',
    validator: (v) => ['primary', 'secondary', 'ghost', 'text'].includes(v)
  },
  size: {
    type: String,
    default: 'md',
    validator: (v) => ['sm', 'md', 'lg'].includes(v)
  },
  disabled: {
    type: Boolean,
    default: false
  },
  inkEffect: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['click'])

const showInkEffect = ref(false)
const inkStyle = ref({})

const sizeMap = {
  sm: 'px-3 py-1.5 text-sm',
  md: 'px-5 py-2.5 text-base',
  lg: 'px-7 py-3.5 text-lg'
}

const typeClasses = {
  primary: [
    'bg-dailan',
    'text-yuebai',
    'hover:bg-dailan-600',
    'active:bg-dailan-700'
  ],
  secondary: [
    'bg-tianqing',
    'text-dailan',
    'hover:bg-tianqing-400',
    'active:bg-tianqing-500'
  ],
  ghost: [
    'bg-transparent',
    'text-dailan',
    'border',
    'border-dailan/20',
    'hover:bg-dailan/5',
    'hover:border-dailan/30'
  ],
  text: [
    'bg-transparent',
    'text-dailan',
    'hover:text-dailan-600',
    'hover:bg-dailan/5'
  ]
}

const buttonClasses = computed(() => {
  const classes = [
    'relative',
    'overflow-hidden',
    'font-serif',
    'rounded-md',
    'transition-all',
    'duration-400',
    'ease-elegant',
    'focus:outline-none',
    'focus:ring-2',
    'focus:ring-dailan/20',
    'focus:ring-offset-2',
    'focus:ring-offset-xuanzhi',
    ...sizeMap[props.size].split(' '),
    ...typeClasses[props.type]
  ]
  
  if (props.disabled) {
    classes.push('opacity-50', 'cursor-not-allowed')
  }
  
  return classes
})

const handleClick = (event) => {
  if (props.disabled) return
  emit('click', event)
}

const handleMouseEnter = (event) => {
  if (!props.inkEffect || props.disabled) return
  
  const rect = event.target.getBoundingClientRect()
  inkStyle.value = {
    left: '50%',
    top: '50%',
    transform: 'translate(-50%, -50%)'
  }
  showInkEffect.value = true
}

const handleMouseLeave = () => {
  showInkEffect.value = false
}
</script>

<style scoped>
.ink-effect {
  position: absolute;
  width: 200%;
  height: 200%;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
  animation: ink-spread 0.6s ease-out forwards;
  pointer-events: none;
}

@keyframes ink-spread {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0;
  }
}
</style>
