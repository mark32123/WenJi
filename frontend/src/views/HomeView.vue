<template>
  <div class="home-view">
    <div class="view-header">
      <h1 class="view-title font-serif">山河卷</h1>
      <div class="header-actions">
        <button class="ai-btn" @click="showAiPanel = true">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2z"/>
            <path d="M12 16v-4"/>
            <path d="M12 8h.01"/>
          </svg>
          <span>AI助手</span>
        </button>
        <div class="user-stats">
          <span class="stat-item">
            <span class="stat-label">阅历</span>
            <span class="stat-value">{{ userStore.experience }}</span>
          </span>
        </div>
      </div>
    </div>
    
    <div class="map-container">
      <CityMap @explore="handleSiteExplore" />
    </div>
    
    <nav class="bottom-nav">
      <router-link to="/" class="nav-item active">
        <span class="nav-icon">山</span>
        <span class="nav-label">山河卷</span>
      </router-link>
      <router-link to="/ar" class="nav-item">
        <span class="nav-icon">鉴</span>
        <span class="nav-label">AR鉴赏</span>
      </router-link>
      <router-link to="/profile" class="nav-item">
        <span class="nav-icon">阁</span>
        <span class="nav-label">藏经阁</span>
      </router-link>
    </nav>
    
    <AiGuideCard
      :visible="showAiCard"
      :artifact="selectedArtifact"
      @close="closeAiCard"
      @like="handleLike"
      @share="handleShare"
      @ar-experience="handleArExperience"
      @ai-complete="handleAiComplete"
    />
    
    <transition name="slide-up">
      <div v-if="showAiPanel" class="ai-panel">
        <div class="ai-panel-header">
          <span class="ai-title font-serif">文迹书生</span>
          <button class="close-btn" @click="showAiPanel = false">×</button>
        </div>
        <div class="ai-panel-messages" ref="messagesContainer">
          <div 
            v-for="(msg, idx) in aiMessages" 
            :key="idx" 
            class="message"
            :class="{ 'from-user': msg.fromUser }"
          >
            <div class="message-content">{{ msg.text }}</div>
          </div>
          <div v-if="isAiTyping" class="message typing">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
        <div class="ai-panel-input">
          <input 
            v-model="aiInput" 
            placeholder="问我任何关于传统文化的问题..."
            @keyup.enter="sendAiMessage"
          />
          <button class="send-btn" @click="sendAiMessage" :disabled="!aiInput.trim() || isAiTyping">
            发送
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { useAuthStore } from '@/stores/authStore'
import { heritageApi, aiApi } from '@/api'
import { CityMap, AiGuideCard } from '@/components'

const router = useRouter()
const userStore = useUserStore()
const authStore = useAuthStore()

const showAiCard = ref(false)
const selectedArtifact = ref(null)
const backendSites = ref([])

const showAiPanel = ref(false)
const aiMessages = ref([
  { text: '您好！我是文迹书生，有什么关于传统文化的问题可以问我~', fromUser: false }
])
const aiInput = ref('')
const isAiTyping = ref(false)
const messagesContainer = ref(null)

const defaultNodes = [
  {
    id: 'qinghuaci',
    name: '青花瓷瓶',
    dynasty: '明朝',
    positionX: 8,
    positionY: 42,
    imageUrl: '/images/qinghuaci.jpg',
    category: '陶瓷艺术',
    location: '景德镇',
    museum: '故宫博物院',
    poem: '素胚勾勒出青花笔锋浓转淡',
    description: '青花瓷是中国陶瓷艺术的瑰宝，以其典雅的蓝白配色闻名于世。'
  },
  {
    id: 'fencaici',
    name: '粉彩瓷瓶',
    dynasty: '清朝',
    positionX: 18,
    positionY: 35,
    imageUrl: '/images/fencaici.jpg',
    category: '陶瓷艺术',
    location: '景德镇',
    museum: '南京博物院',
    poem: '粉黛轻施画中仙',
    description: '粉彩瓷是清代康熙年间创烧的釉上彩新品种，色彩柔和粉润。'
  },
  {
    id: 'simuwuding',
    name: '后母戊鼎',
    dynasty: '商代',
    positionX: 28,
    positionY: 48,
    imageUrl: '/images/simuwuding.jpg',
    category: '青铜器',
    location: '河南安阳',
    museum: '中国国家博物馆',
    poem: '鼎定天下，礼乐文明',
    description: '后母戊鼎是商代晚期青铜器，是迄今为止出土的最重的青铜器，重达832.84公斤。',
    special: true,
    icon: '👑'
  },
  {
    id: 'sijunzhang',
    name: '四羊方尊',
    dynasty: '商代',
    positionX: 35,
    positionY: 38,
    imageUrl: '/images/sijunzhang.jpg',
    category: '青铜器',
    location: '湖南宁乡',
    museum: '中国国家博物馆',
    poem: '四羊方尊，青铜典范',
    description: '四羊方尊是商代晚期青铜礼器，四角各铸一只卷角羊头，造型雄奇。'
  },
  {
    id: 'qingmingshanghetu',
    name: '清明上河图',
    dynasty: '北宋',
    positionX: 42,
    positionY: 45,
    imageUrl: '/images/qingming.jpg',
    category: '书画艺术',
    location: '河南开封',
    museum: '故宫博物院',
    poem: '汴京繁华，尽在画卷',
    description: '北宋画家张择端仅见的存世精品，描绘了北宋都城汴京的繁华景象。',
    special: true,
    icon: '📜'
  },
  {
    id: 'fuchunshanju',
    name: '富春山居图',
    dynasty: '元代',
    positionX: 50,
    positionY: 32,
    imageUrl: '/images/fuchun.jpg',
    category: '书画艺术',
    location: '浙江杭州',
    museum: '浙江省博物馆',
    poem: '山水之间，画中有诗',
    description: '元代画家黄公望的作品，以浙江富春江为背景，被誉为"画中之兰亭"。'
  },
  {
    id: 'yucong',
    name: '良渚玉琮',
    dynasty: '新石器',
    positionX: 58,
    positionY: 40,
    imageUrl: '/images/yucong.jpg',
    category: '玉器',
    location: '浙江杭州',
    museum: '浙江省博物馆',
    poem: '天圆地方，玉润千年',
    description: '良渚文化玉器的代表作品，外方内圆，象征"天圆地方"，展现了5000年前的精湛工艺。'
  },
  {
    id: 'heyinyang',
    name: '何尊',
    dynasty: '西周',
    positionX: 65,
    positionY: 50,
    imageUrl: '/images/hezun.jpg',
    category: '青铜器',
    location: '陕西宝鸡',
    museum: '宝鸡青铜器博物院',
    poem: '铭文镌刻，中国之源',
    description: '西周早期青铜器，铭文中首次出现"中国"二字，是研究中国名称起源的重要文物。',
    special: true,
    icon: '🏛️'
  },
  {
    id: 'changxin-gongdeng',
    name: '长信宫灯',
    dynasty: '西汉',
    positionX: 72,
    positionY: 36,
    imageUrl: '/images/changxin.jpg',
    category: '青铜器',
    location: '河北满城',
    museum: '河北博物院',
    poem: '宫灯长明，照亮千年',
    description: '西汉时期的鎏金铜灯，造型为跪坐宫女执灯，被誉为"中华第一灯"。'
  },
  {
    id: 'dunhuang-feitian',
    name: '敦煌飞天',
    dynasty: '北魏',
    positionX: 80,
    positionY: 44,
    imageUrl: '/images/feitian.jpg',
    category: '壁画艺术',
    location: '甘肃敦煌',
    museum: '敦煌研究院',
    poem: '飞天曼舞，丝路遗珍',
    description: '敦煌壁画中的飞天形象，融合印度、西域和中原文化，是丝路文明的象征。',
    special: true,
    icon: '🎨'
  },
  {
    id: 'sanxingdui-mask',
    name: '三星堆面具',
    dynasty: '商代',
    positionX: 88,
    positionY: 38,
    imageUrl: '/images/sanxingdui.jpg',
    category: '青铜器',
    location: '四川广汉',
    museum: '三星堆博物馆',
    poem: '古蜀神秘，面具藏真',
    description: '古蜀文明的神秘象征，造型夸张，眼球突出，展现了与中原文化截然不同的艺术风格。'
  },
  {
    id: 'jinsha-sunbird',
    name: '太阳神鸟',
    dynasty: '商周',
    positionX: 95,
    positionY: 46,
    imageUrl: '/images/sunbird.jpg',
    category: '金器',
    location: '四川成都',
    museum: '金沙遗址博物馆',
    poem: '金乌负日，神鸟翱翔',
    description: '商周时期古蜀金器，外层为四只逆时针飞行的神鸟，是中国文化遗产标志。'
  }
]

const mapNodes = computed(() => {
  return defaultNodes.map(node => ({
    ...node,
    unlocked: userStore.visitedSites.some(s => s.id === node.id),
    collected: userStore.artifacts.some(a => a.id === node.id)
  }))
})

const handleSiteExplore = (site) => {
  selectedArtifact.value = {
    id: site.id,
    name: site.name,
    dynasty: site.dynasty || site.type,
    category: site.type,
    location: site.address,
    museum: site.name,
    description: site.description,
    imageUrl: site.image
  }
  showAiCard.value = true
  userStore.visitSite(site)
}

const handleNodeClick = (node) => {
  userStore.visitSite(node)
  selectedArtifact.value = node
  showAiCard.value = true
}

const handleNodeCollect = (node) => {
  userStore.collectArtifact(node)
  userStore.addExperience(10)
  userStore.saveToStorage()
}

const closeAiCard = () => {
  showAiCard.value = false
  selectedArtifact.value = null
}

const handleLike = ({ artifact, liked }) => {
  console.log('Like:', artifact.name, liked)
}

const handleShare = (artifact) => {
  console.log('Share:', artifact.name)
  if (navigator.share) {
    navigator.share({
      title: artifact.name,
      text: `来看看这件${artifact.dynasty}的${artifact.name}`,
      url: window.location.href
    })
  }
}

const handleArExperience = (artifact) => {
  router.push({ path: '/ar', query: { id: artifact.id } })
}

const handleAiComplete = ({ artifact, correct }) => {
  if (correct) {
    userStore.addExperience(5)
    userStore.saveToStorage()
  }
}

const sendAiMessage = async () => {
  if (!aiInput.value.trim() || isAiTyping.value) return
  
  const userMessage = aiInput.value.trim()
  aiMessages.value.push({ text: userMessage, fromUser: true })
  aiInput.value = ''
  isAiTyping.value = true
  
  await nextTick()
  scrollToBottom()
  
  try {
    const response = await aiApi.chat(userMessage, 'home_ai')
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let aiReply = ''
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
            aiReply += content
          }
        }
      }
    }
    
    if (aiReply.trim()) {
      aiMessages.value.push({ text: aiReply.trim(), fromUser: false })
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('AI回复失败:', error)
    aiMessages.value.push({ text: '抱歉，我暂时无法回答这个问题，请稍后再试。', fromUser: false })
  } finally {
    isAiTyping.value = false
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onMounted(() => {
  userStore.loadFromStorage()
  
  if (authStore.isLoggedIn) {
    userStore.fetchUserInfo()
  }
  
  fetchNearbySites()
})

const fetchNearbySites = async () => {
  try {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const { longitude, latitude } = position.coords
          const result = await heritageApi.getNearbySites(longitude, latitude)
          if (result.success && result.data) {
            backendSites.value = result.data
          }
        },
        async () => {
          const result = await heritageApi.getNearbySites(116.4074, 39.9042)
          if (result.success && result.data) {
            backendSites.value = result.data
          }
        }
      )
    } else {
      const result = await heritageApi.getNearbySites(116.4074, 39.9042)
      if (result.success && result.data) {
        backendSites.value = result.data
      }
    }
  } catch (error) {
    console.warn('获取附近景点失败:', error)
  }
}

watch(() => authStore.isLoggedIn, (isLoggedIn) => {
  if (isLoggedIn) {
    userStore.fetchUserInfo()
  }
})
</script>

<style scoped>
.home-view {
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  background: #F5E6D3;
  overflow: hidden;
}

.view-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  padding-top: calc(env(safe-area-inset-top) + 12px);
  background: rgba(245, 230, 211, 0.95);
  z-index: 100;
}

.view-title {
  font-size: 20px;
  color: #2D4059;
  letter-spacing: 0.15em;
  margin: 0;
}

.user-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: rgba(45, 64, 89, 0.06);
  border-radius: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-label {
  font-size: 10px;
  color: #8B9A9C;
}

.stat-value {
  font-size: 12px;
  color: #2D4059;
  font-weight: 500;
}

.stat-divider {
  color: #8B9A9C;
  opacity: 0.5;
}

.map-container {
  flex: 1;
  min-height: 0;
  position: relative;
}

.bottom-nav {
  flex-shrink: 0;
  display: flex;
  justify-content: space-around;
  padding: 10px 16px;
  padding-bottom: calc(env(safe-area-inset-bottom) + 10px);
  background: rgba(245, 242, 235, 0.98);
  border-top: 1px solid rgba(45, 64, 89, 0.08);
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  text-decoration: none;
  color: #8B9A9C;
  transition: color 0.3s ease;
}

.nav-item.active {
  color: #2D4059;
}

.nav-icon {
  font-size: 20px;
  font-family: 'Noto Serif SC', serif;
}

.nav-label {
  font-size: 10px;
}

@media (max-width: 428px) {
  .view-header {
    padding: 12px 16px;
  }
  
  .view-title {
    font-size: 20px;
  }
  
  .user-stats {
    padding: 6px 10px;
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  color: #F5F2EB;
  border: none;
  border-radius: 16px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.ai-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(45, 64, 89, 0.3);
}

.ai-panel {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #F5F2EB;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.ai-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  padding-top: calc(env(safe-area-inset-top) + 16px);
  background: #2D4059;
  color: #F5F2EB;
}

.ai-title {
  font-size: 18px;
  letter-spacing: 0.1em;
}

.close-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: #F5F2EB;
  font-size: 20px;
  cursor: pointer;
}

.ai-panel-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 16px;
  background: #E8ECEF;
  color: #2D4059;
}

.message.from-user {
  align-self: flex-end;
  background: #2D4059;
  color: #F5F2EB;
}

.message-content {
  font-size: 14px;
  line-height: 1.5;
}

.typing-indicator {
  display: flex;
  gap: 4px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #8B9A9C;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-4px); }
}

.ai-panel-input {
  display: flex;
  gap: 12px;
  padding: 16px;
  padding-bottom: calc(env(safe-area-inset-bottom) + 16px);
  background: #F5F2EB;
  border-top: 1px solid rgba(45, 64, 89, 0.1);
}

.ai-panel-input input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid rgba(45, 64, 89, 0.2);
  border-radius: 24px;
  font-size: 14px;
  background: #fff;
  outline: none;
}

.ai-panel-input input:focus {
  border-color: #2D4059;
}

.send-btn {
  padding: 12px 24px;
  background: #2D4059;
  color: #F5F2EB;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(100%);
}
</style>
