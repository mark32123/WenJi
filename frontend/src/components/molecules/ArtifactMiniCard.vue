<template>
  <PaperSurface
    :hoverable="interactive"
    :bordered="true"
    padding="none"
    class="artifact-mini-card"
    @click="handleClick"
  >
    <div class="card-content">
      <div class="artifact-image">
        <img 
          v-if="artifact.imageUrl && !imageError"
          :src="artifact.imageUrl"
          :alt="artifact.name"
          @error="handleImageError"
        />
        <div v-else class="image-placeholder">
          <span class="placeholder-icon">🏺</span>
        </div>
      </div>
      
      <div class="artifact-info">
        <span class="artifact-name font-serif">{{ artifact.name }}</span>
        
        <div class="meta-tags">
          <span v-if="artifact.dynasty" class="meta-tag">
            {{ artifact.dynasty }}
          </span>
          <span v-if="artifact.category" class="meta-tag">
            {{ artifact.category }}
          </span>
        </div>
      </div>
      
      <div v-if="showSeal && isCollected" class="seal-wrapper">
        <SealStamp
          :text="sealText"
          size="sm"
        />
      </div>
    </div>
  </PaperSurface>
</template>

<script setup>
import { ref } from 'vue'
import PaperSurface from '@/components/atoms/PaperSurface.vue'
import SealStamp from '@/components/atoms/SealStamp.vue'

const props = defineProps({
  artifact: {
    type: Object,
    required: true,
    validator: (v) => v && v.id && v.name
  },
  showSeal: {
    type: Boolean,
    default: true
  },
  isCollected: {
    type: Boolean,
    default: false
  },
  sealText: {
    type: String,
    default: '已阅'
  },
  interactive: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['click'])

const imageError = ref(false)

const handleClick = () => {
  if (props.interactive) {
    emit('click', props.artifact)
  }
}

const handleImageError = () => {
  imageError.value = true
}
</script>

<style scoped>
.artifact-mini-card {
  position: relative;
  width: 100%;
  cursor: pointer;
  overflow: hidden;
}

.card-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
}

.artifact-image {
  width: 64px;
  height: 64px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
  background: rgba(45, 64, 89, 0.05);
}

.artifact-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(
    135deg,
    rgba(168, 218, 220, 0.2) 0%,
    rgba(139, 154, 156, 0.2) 100%
  );
}

.placeholder-icon {
  font-size: 24px;
  opacity: 0.5;
}

.artifact-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  min-width: 0;
  overflow: hidden;
}

.artifact-name {
  font-size: 16px;
  font-weight: 500;
  color: #2D4059;
  letter-spacing: 0.1em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.meta-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.meta-tag {
  font-size: 10px;
  color: #8B9A9C;
  background: rgba(45, 64, 89, 0.06);
  padding: 2px 8px;
  border-radius: 10px;
}

.seal-wrapper {
  flex-shrink: 0;
  margin-left: auto;
}

@media (max-width: 428px) {
  .card-content {
    padding: 10px;
    gap: 10px;
  }
  
  .artifact-image {
    width: 56px;
    height: 56px;
  }
  
  .artifact-name {
    font-size: 14px;
  }
}
</style>
