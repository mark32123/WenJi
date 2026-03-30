<!--
  RewardView.vue - 文物收藏/得趣页面
  
  功能说明：
  1. 展示用户探索获得的文物收藏卡片
  2. AI 赋诗功能 - 为文物生成诗词
  3. 收藏功能 - 将文物收入藏经阁
  4. 保存功能 - 保存文物卡片图片
  
  页面路由：
  - /reward?id=xxx&exp=10 - 显示指定 ID 的文物
  - /reward - 无参数时显示默认演示文物
  
  核心交互：
  - 点击"返回"返回上一页
  - 点击"AI赋诗"调用 AI 生成诗词（每日限5次）
  - 点击"收入藏经阁"收藏文物
  - 点击"保存图片"保存卡片
  
  依赖组件：
  - RewardSignature: 文物收藏卡片组件
  - PaperSurface: 纸张效果容器
  - InkButton: 水墨风格按钮
-->
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
      :experience-gained="experienceGained"
      @collect="handleCollect"
      @save="handleSave"
    />
    
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
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { RewardSignature } from '@/components'
import PaperSurface from '@/components/atoms/PaperSurface.vue'
import InkButton from '@/components/atoms/InkButton.vue'
import exhibitsData from '@/data/exhibits.json'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const artifact = ref(null)
const experienceGained = ref(10)

const handleBack = () => {
  router.back()
}

const handleCollect = (collectedArtifact) => {
  userStore.collectArtifact(collectedArtifact)
  userStore.addExperience(experienceGained.value)
  userStore.saveToStorage()
}

const handleSave = async (savedArtifact) => {
  if (!savedArtifact) return
  
  try {
    if (savedArtifact.imageUrl) {
      const response = await fetch(savedArtifact.imageUrl)
      const blob = await response.blob()
      const url = URL.createObjectURL(blob)
      
      const link = document.createElement('a')
      link.href = url
      link.download = `${savedArtifact.name || '文物'}_${Date.now()}.jpg`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
    } else {
      alert('暂无图片可保存')
    }
  } catch (error) {
    console.error('保存图片失败:', error)
    alert('保存图片失败，请稍后重试')
  }
}

const goExplore = () => {
  router.push('/')
}

onMounted(() => {
  const artifactId = route.query.id
  
  if (artifactId) {
    const exhibit = exhibitsData.exhibits.find(e => e.id === artifactId)
    if (exhibit) {
      artifact.value = {
        id: exhibit.id,
        name: exhibit.name,
        dynasty: exhibit.metadata?.era || '',
        category: exhibit.metadata?.category || '',
        location: exhibit.metadata?.location || '',
        museum: exhibit.metadata?.museum || '',
        imageUrl: exhibit.layers?.[0]?.src || ''
      }
    } else {
      const found = userStore.artifacts.find(a => a.id === artifactId)
      if (found) {
        artifact.value = found
      }
    }
  }
  
  if (!artifact.value) {
    artifact.value = {
      id: 'demo-artifact',
      name: '青花瓷瓶',
      dynasty: '明朝',
      category: '瓷器',
      location: '景德镇',
      imageUrl: '/images/qinghuaci.jpg'
    }
  }
  
  if (route.query.exp) {
    experienceGained.value = parseInt(route.query.exp) || 10
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

@media (max-width: 428px) {
  .view-header {
    padding: 12px 16px;
  }
  
  .view-title {
    font-size: 18px;
  }
}
</style>
