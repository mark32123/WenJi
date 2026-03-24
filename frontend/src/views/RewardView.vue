<template>
  <div class="reward-view">
    <div class="view-header">
      <InkButton type="ghost" size="sm" @click="handleBack">
        返回
      </InkButton>
      <h1 class="view-title font-serif">得趣</h1>
      <div class="placeholder" />
    </div>
    
    <RewardSignature
      v-if="artifact"
      :artifact="artifact"
      :poem="currentPoem"
      :experience-gained="experienceGained"
      @collect="handleCollect"
      @save="handleSave"
    />
    
    <div v-if="artifact" class="poem-actions">
      <button 
        class="generate-poem-btn" 
        @click="generatePoem"
        :disabled="generatingPoem || poemGenerationsLeft <= 0"
      >
        <span v-if="generatingPoem" class="loading-spinner"></span>
        <span v-else>✨ AI赋诗</span>
      </button>
      <span class="generations-left">剩余 {{ poemGenerationsLeft }} 次</span>
    </div>
    
    <div v-else class="empty-state">
      <PaperSurface class="empty-card" padding="lg">
        <span class="empty-icon">📜</span>
        <p class="empty-text font-serif">暂无收藏</p>
        <p class="empty-hint">前往山河卷探索文物</p>
        <InkButton type="primary" @click="goExplore">
          开始探索
        </InkButton>
      </PaperSurface>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { RewardSignature } from '@/components'
import { aiApi } from '@/api'
import PaperSurface from '@/components/atoms/PaperSurface.vue'
import InkButton from '@/components/atoms/InkButton.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const artifact = ref(null)
const experienceGained = ref(10)
const currentPoem = ref('')
const generatingPoem = ref(false)
const MAX_POEM_GENERATIONS = 5
const poemGenerationsLeft = ref(MAX_POEM_GENERATIONS)

const handleBack = () => {
  router.back()
}

const handleCollect = (collectedArtifact) => {
  userStore.collectArtifact(collectedArtifact)
  userStore.addExperience(experienceGained.value)
  userStore.saveToStorage()
}

const handleSave = (savedArtifact) => {
  console.log('Save artifact:', savedArtifact)
}

const goExplore = () => {
  router.push('/')
}

const generatePoem = async () => {
  if (generatingPoem.value || poemGenerationsLeft.value <= 0 || !artifact.value) return
  
  generatingPoem.value = true
  
  try {
    const prompt = `请为${artifact.value.name}（${artifact.value.dynasty || '古代'}${artifact.value.category ? '，' + artifact.value.category : ''}）创作一首七言绝句或五言绝句，要求：1. 突出文物的历史韵味 2. 体现其艺术特色 3. 语言典雅优美。只输出诗句，不要其他解释。`
    
    const response = await aiApi.chat(prompt, `poem_${artifact.value.id}`)
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let poem = ''
    let buffer = ''
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const content = line.slice(5).trim()
          if (content && content !== '[DONE]') {
            poem += content
          }
        } else if (line.trim() && !line.startsWith(':')) {
          poem += line
        }
      }
    }
    
    if (poem.trim()) {
      currentPoem.value = poem.trim()
      poemGenerationsLeft.value--
      localStorage.setItem('wenji_poem_generations', JSON.stringify({
        date: new Date().toDateString(),
        left: poemGenerationsLeft.value
      }))
    }
  } catch (error) {
    console.error('生成诗词失败:', error)
  } finally {
    generatingPoem.value = false
  }
}

onMounted(() => {
  const artifactId = route.query.id
  
  if (artifactId) {
    const found = userStore.artifacts.find(a => a.id === artifactId)
    if (found) {
      artifact.value = found
    }
  }
  
  if (!artifact.value) {
    artifact.value = {
      id: 'demo-artifact',
      name: '青花瓷瓶',
      dynasty: '明朝',
      category: '瓷器',
      location: '景德镇',
      imageUrl: '/images/demo-porcelain.jpg',
      poem: '素胚勾勒出青花笔锋浓转淡，瓶身描绘的牡丹一如你初妆'
    }
  }
  
  currentPoem.value = artifact.value.poem || '岁月无声，文物有灵'
  
  if (route.query.exp) {
    experienceGained.value = parseInt(route.query.exp) || 10
  }
  
  const saved = localStorage.getItem('wenji_poem_generations')
  if (saved) {
    const data = JSON.parse(saved)
    if (data.date === new Date().toDateString()) {
      poemGenerationsLeft.value = data.left
    }
  }
})
</script>

<style scoped>
.reward-view {
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(
    180deg,
    #F0F4F8 0%,
    #E8ECEF 50%,
    #F0F4F8 100%
  );
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  padding-top: calc(env(safe-area-inset-top) + 16px);
  position: sticky;
  top: 0;
  background: rgba(240, 244, 248, 0.95);
  backdrop-filter: blur(8px);
  z-index: 10;
}

.view-title {
  font-size: 20px;
  color: #2D4059;
  letter-spacing: 0.15em;
  margin: 0;
}

.placeholder {
  width: 60px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 60px);
  padding: 24px;
}

.empty-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  text-align: center;
  max-width: 280px;
}

.empty-icon {
  font-size: 48px;
  opacity: 0.5;
}

.empty-text {
  font-size: 18px;
  color: #2D4059;
  margin: 0;
}

.empty-hint {
  font-size: 14px;
  color: #8B9A9C;
  margin: 0;
}

.poem-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-top: -8px;
}

.generate-poem-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  border: none;
  border-radius: 24px;
  color: #F5F2EB;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.generate-poem-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(45, 64, 89, 0.3);
}

.generate-poem-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.generations-left {
  font-size: 12px;
  color: #8B9A9C;
}

@media (max-width: 428px) {
  .view-header {
    padding: 12px 16px;
  }
  
  .view-title {
    font-size: 18px;
  }
}
</style>
