<template>
  <div
    :class="containerClasses"
    :style="containerStyle"
    ref="textRef"
  >
    <span
      v-for="(char, index) in characters"
      :key="index"
      :class="charClasses"
      :style="getCharStyle(index)"
    >
      {{ char }}
    </span>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  text: {
    type: String,
    required: true
  },
  direction: {
    type: String,
    default: 'rtl',
    validator: (v) => ['rtl', 'ltr'].includes(v)
  },
  fontSize: {
    type: String,
    default: 'base'
  },
  fontWeight: {
    type: String,
    default: 'normal'
  },
  color: {
    type: String,
    default: 'dailan'
  },
  letterSpacing: {
    type: String,
    default: '0.15em'
  },
  lineHeight: {
    type: String,
    default: '1.8'
  },
  fontFamily: {
    type: String,
    default: 'serif'
  },
  animate: {
    type: Boolean,
    default: false
  },
  animationDelay: {
    type: Number,
    default: 50
  }
})

const textRef = ref(null)

const fontSizeMap = {
  xs: 'text-xs',
  sm: 'text-sm',
  base: 'text-base',
  lg: 'text-lg',
  xl: 'text-xl',
  '2xl': 'text-2xl',
  'display-sm': 'text-display-sm',
  'display-md': 'text-display-md',
  'display-lg': 'text-display-lg'
}

const fontWeightMap = {
  light: 'font-light',
  normal: 'font-normal',
  medium: 'font-medium',
  semibold: 'font-semibold',
  bold: 'font-bold'
}

const colorMap = {
  dailan: 'text-dailan',
  mohei: 'text-mohei',
  qinghui: 'text-qinghui',
  tianqing: 'text-tianqing',
  zhusha: 'text-zhusha'
}

const fontFamilyMap = {
  serif: 'font-serif',
  sans: 'font-sans',
  display: 'font-display'
}

const characters = computed(() => {
  const chars = props.text.split('')
  return props.direction === 'rtl' ? chars : chars.reverse()
})

const containerClasses = computed(() => {
  return [
    'inline-flex',
    'flex-col',
    'items-center'
  ]
})

const containerStyle = computed(() => ({
  writingMode: 'vertical-rl',
  letterSpacing: props.letterSpacing,
  lineHeight: props.lineHeight
}))

const charClasses = computed(() => {
  const classes = [
    'inline-block',
    fontSizeMap[props.fontSize] || fontSizeMap.base,
    fontWeightMap[props.fontWeight] || fontWeightMap.normal,
    colorMap[props.color] || colorMap.dailan,
    fontFamilyMap[props.fontFamily] || fontFamilyMap.serif
  ]
  
  if (props.animate) {
    classes.push('opacity-0', 'animate-fade-in-up')
  }
  
  return classes
})

const getCharStyle = (index) => {
  if (props.animate) {
    return {
      animationDelay: `${index * props.animationDelay}ms`,
      animationFillMode: 'forwards'
    }
  }
  return {}
}
</script>

<style scoped>
.vertical-text-fallback {
  writing-mode: vertical-rl;
  -webkit-writing-mode: vertical-rl;
  -ms-writing-mode: tb-rl;
  text-orientation: mixed;
  -webkit-text-orientation: mixed;
}
</style>
