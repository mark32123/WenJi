<template>
  <div
    :class="surfaceClasses"
    :style="surfaceStyle"
  >
    <slot />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  width: {
    type: String,
    default: 'auto'
  },
  height: {
    type: String,
    default: 'auto'
  },
  padding: {
    type: String,
    default: 'md'
  },
  rounded: {
    type: String,
    default: 'md'
  },
  shadow: {
    type: String,
    default: 'paper'
  },
  texture: {
    type: Boolean,
    default: true
  },
  hoverable: {
    type: Boolean,
    default: false
  },
  bordered: {
    type: Boolean,
    default: false
  }
})

const paddingMap = {
  none: 'p-0',
  sm: 'p-2',
  md: 'p-4',
  lg: 'p-6',
  xl: 'p-8'
}

const roundedMap = {
  none: 'rounded-none',
  sm: 'rounded-sm',
  md: 'rounded-md',
  lg: 'rounded-lg',
  xl: 'rounded-xl',
  full: 'rounded-2xl'
}

const shadowMap = {
  none: 'shadow-none',
  sm: 'shadow-sm',
  paper: 'shadow-paper',
  hover: 'shadow-paper-hover',
  lg: 'shadow-lg'
}

const surfaceClasses = computed(() => {
  const classes = [
    'relative',
    'bg-xuanzhi',
    'overflow-hidden',
    paddingMap[props.padding] || paddingMap.md,
    roundedMap[props.rounded] || roundedMap.md,
    shadowMap[props.shadow] || shadowMap.paper
  ]
  
  if (props.texture) {
    classes.push('paper-texture')
  }
  
  if (props.hoverable) {
    classes.push('transition-all', 'duration-400', 'ease-elegant', 'hover:shadow-paper-hover', 'hover:-translate-y-0.5')
  }
  
  if (props.bordered) {
    classes.push('ink-border')
  }
  
  return classes
})

const surfaceStyle = computed(() => {
  const style = {}
  
  if (props.width !== 'auto') {
    style.width = props.width
  }
  
  if (props.height !== 'auto') {
    style.height = props.height
  }
  
  return style
})
</script>

<style scoped>
.paper-texture::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
  opacity: 0.03;
  pointer-events: none;
  z-index: 0;
}

.paper-texture > :deep(*) {
  position: relative;
  z-index: 1;
}
</style>
