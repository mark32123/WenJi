<template>
  <PaperSurface
    :hoverable="interactive"
    :bordered="true"
    padding="sm"
    class="poem-badge"
    :class="{ 'is-new': isNew }"
    @click="handleClick"
  >
    <div class="badge-content">
      <div class="poem-header">
        <span class="dynasty-tag font-serif">{{ poem.dynasty }}</span>
        <span class="author-text">{{ poem.author }}</span>
      </div>
      
      <div class="poem-body">
        <VerticalText
          :text="displayContent"
          font-size="sm"
          color="mohei"
          :letter-spacing="'0.15em'"
          :animate="animateText"
        />
      </div>
      
      <div class="poem-footer">
        <span class="title-text font-serif">{{ poem.title }}</span>
      </div>
      
      <SealStamp
        v-if="showSeal"
        :text="sealText"
        size="sm"
        :active="isNew"
        class="badge-seal"
      />
    </div>
    
    <transition name="glow">
      <div v-if="isNew" class="new-glow" />
    </transition>
  </PaperSurface>
</template>

<script setup>
import { computed } from 'vue'
import PaperSurface from '@/components/atoms/PaperSurface.vue'
import VerticalText from '@/components/atoms/VerticalText.vue'
import SealStamp from '@/components/atoms/SealStamp.vue'

const props = defineProps({
  poem: {
    type: Object,
    required: true,
    validator: (v) => v && v.id && v.title && v.content
  },
  maxLines: {
    type: Number,
    default: 4
  },
  showSeal: {
    type: Boolean,
    default: true
  },
  sealText: {
    type: String,
    default: '雅赏'
  },
  isNew: {
    type: Boolean,
    default: false
  },
  interactive: {
    type: Boolean,
    default: true
  },
  animateText: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

const displayContent = computed(() => {
  const lines = props.poem.content.split(/[，。！？、；]/g).filter(l => l.trim())
  if (lines.length <= props.maxLines) {
    return props.poem.content
  }
  return lines.slice(0, props.maxLines).join('，') + '……'
})

const handleClick = () => {
  if (props.interactive) {
    emit('click', props.poem)
  }
}
</script>

<style scoped>
.poem-badge {
  position: relative;
  width: 100%;
  min-height: 120px;
  cursor: pointer;
  transition: all 0.4s ease;
}

.poem-badge.is-new {
  border-color: rgba(192, 57, 43, 0.2);
}

.badge-content {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 8px;
}

.poem-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dynasty-tag {
  font-size: 10px;
  color: #8B9A9C;
  background: rgba(45, 64, 89, 0.06);
  padding: 2px 8px;
  border-radius: 10px;
}

.author-text {
  font-size: 11px;
  color: #8B9A9C;
  font-family: 'Noto Serif SC', serif;
}

.poem-body {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 8px 0;
}

.poem-footer {
  text-align: center;
}

.title-text {
  font-size: 12px;
  color: #2D4059;
  letter-spacing: 0.1em;
}

.badge-seal {
  position: absolute;
  bottom: 4px;
  right: 4px;
}

.new-glow {
  position: absolute;
  inset: -1px;
  border-radius: inherit;
  background: linear-gradient(
    135deg,
    rgba(192, 57, 43, 0.1) 0%,
    transparent 50%,
    rgba(168, 218, 220, 0.1) 100%
  );
  pointer-events: none;
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}

.glow-enter-active,
.glow-leave-active {
  transition: opacity 0.4s ease;
}

.glow-enter-from,
.glow-leave-to {
  opacity: 0;
}

@media (max-width: 428px) {
  .poem-badge {
    min-height: 100px;
  }
  
  .poem-body {
    padding: 4px 0;
  }
}
</style>
