<template>
  <Teleport to="body">
    <transition name="card-slide">
      <div v-if="visible" class="ai-guide-card-overlay" @click.self="handleClose">
        <div class="ai-guide-card">
          <div class="card-header">
            <div class="artifact-image">
              <img 
                v-if="artifact.imageUrl" 
                :src="artifact.imageUrl" 
                :alt="artifact.name"
              />
              <div v-else class="image-placeholder">
                <span>🏺</span>
              </div>
            </div>
            
            <div class="header-info">
              <h2 class="artifact-title font-serif">{{ artifact.name }}</h2>
              <div class="artifact-tags">
                <span v-if="artifact.dynasty" class="tag dynasty">{{ artifact.dynasty }}</span>
                <span v-if="artifact.category" class="tag category">{{ artifact.category }}</span>
              </div>
            </div>
            
            <button class="close-btn" @click="handleClose">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12"/>
              </svg>
            </button>
          </div>
          
          <div class="card-body">
            <div v-if="!aiState.started" class="ai-intro">
              <div class="scholar-avatar">
                <div class="avatar-glow" />
                <svg class="scholar-svg" viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg">
                  <defs>
                    <linearGradient id="hatGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" stop-color="#2D4059"/>
                      <stop offset="100%" stop-color="#1A2A3A"/>
                    </linearGradient>
                    <linearGradient id="robeGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                      <stop offset="0%" stop-color="#A8DADC"/>
                      <stop offset="100%" stop-color="#7FB3B5"/>
                    </linearGradient>
                    <linearGradient id="faceGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                      <stop offset="0%" stop-color="#F5E6D3"/>
                      <stop offset="100%" stop-color="#E8D4BE"/>
                    </linearGradient>
                  </defs>
                  
                  <ellipse cx="60" cy="95" rx="35" ry="15" fill="rgba(45,64,89,0.1)"/>
                  
                  <path d="M35 85 Q35 70 45 65 L75 65 Q85 70 85 85 L85 110 L35 110 Z" fill="url(#robeGradient)"/>
                  <path d="M45 65 Q50 75 60 75 Q70 75 75 65" fill="none" stroke="#8B9A9C" stroke-width="1"/>
                  
                  <ellipse cx="60" cy="50" rx="22" ry="26" fill="url(#faceGradient)"/>
                  
                  <ellipse cx="60" cy="32" rx="28" ry="8" fill="url(#hatGradient)"/>
                  <rect x="32" y="24" width="56" height="10" rx="2" fill="url(#hatGradient)"/>
                  <rect x="45" y="18" width="30" height="8" rx="1" fill="#3D5A80"/>
                  
                  <ellipse cx="52" cy="48" rx="3" ry="2" fill="#2D4059"/>
                  <ellipse cx="68" cy="48" rx="3" ry="2" fill="#2D4059"/>
                  
                  <path d="M55 58 Q60 62 65 58" fill="none" stroke="#8B6B5B" stroke-width="1.5" stroke-linecap="round"/>
                  
                  <path d="M42 42 Q45 38 48 42" fill="none" stroke="#8B6B5B" stroke-width="1" stroke-linecap="round"/>
                  <path d="M72 42 Q75 38 78 42" fill="none" stroke="#8B6B5B" stroke-width="1" stroke-linecap="round"/>
                  
                  <path d="M35 70 Q25 75 20 85" fill="none" stroke="url(#robeGradient)" stroke-width="8" stroke-linecap="round"/>
                  <path d="M85 70 Q95 75 100 85" fill="none" stroke="url(#robeGradient)" stroke-width="8" stroke-linecap="round"/>
                  
                  <circle cx="60" cy="75" r="2" fill="#C43C3C"/>
                  <circle cx="60" cy="82" r="1.5" fill="#C43C3C"/>
                </svg>
                <div class="avatar-name">书生</div>
              </div>
              <p class="intro-text font-serif">点击下方按钮，让 AI 书生为您讲解这件文物的前世今生</p>
              <button class="start-ai-btn" @click="startAiGuide" :disabled="aiState.loading">
                <span v-if="aiState.loading" class="loading-icon">
                  <svg class="rotate" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10" stroke-dasharray="60" stroke-dashoffset="20"/>
                  </svg>
                </span>
                <span v-else>📚 开始讲解</span>
              </button>
            </div>
            
            <div v-else class="ai-content">
              <div class="guide-header">
                <div class="guide-avatar">
                  <svg viewBox="0 0 40 40" xmlns="http://www.w3.org/2000/svg">
                    <defs>
                      <linearGradient id="miniHat" x1="0%" y1="0%" x2="100%" y2="100%">
                        <stop offset="0%" stop-color="#2D4059"/>
                        <stop offset="100%" stop-color="#1A2A3A"/>
                      </linearGradient>
                      <linearGradient id="miniRobe" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" stop-color="#A8DADC"/>
                        <stop offset="100%" stop-color="#7FB3B5"/>
                      </linearGradient>
                    </defs>
                    <ellipse cx="20" cy="32" rx="12" ry="5" fill="rgba(45,64,89,0.1)"/>
                    <path d="M10 28 Q10 22 15 20 L25 20 Q30 22 30 28 L30 38 L10 38 Z" fill="url(#miniRobe)"/>
                    <ellipse cx="20" cy="16" rx="8" ry="9" fill="#F5E6D3"/>
                    <ellipse cx="20" cy="10" rx="10" ry="3" fill="url(#miniHat)"/>
                    <rect x="10" y="7" width="20" height="4" rx="1" fill="url(#miniHat)"/>
                    <circle cx="17" cy="15" r="1" fill="#2D4059"/>
                    <circle cx="23" cy="15" r="1" fill="#2D4059"/>
                    <path d="M18 19 Q20 21 22 19" fill="none" stroke="#8B6B5B" stroke-width="0.8" stroke-linecap="round"/>
                  </svg>
                </div>
                <span class="guide-name font-serif">AI 书生</span>
              </div>
              
              <div class="guide-text" ref="textContainer">
                <p class="typewriter-text font-serif">{{ displayText }}</p>
                <span v-if="aiState.typing" class="cursor">|</span>
              </div>
              
              <transition name="fade">
                <div v-if="aiState.showQuestion && currentQuestion" class="quiz-section">
                  <div class="quiz-header">
                    <span class="quiz-icon">❓</span>
                    <span class="quiz-title font-serif">互动问答</span>
                  </div>
                  
                  <p class="question-text font-serif">{{ currentQuestion.question }}</p>
                  
                  <div class="options-list">
                    <button
                      v-for="(option, index) in currentQuestion.options"
                      :key="index"
                      class="option-btn"
                      :class="getOptionClass(index)"
                      :disabled="aiState.answered"
                      @click="selectAnswer(index)"
                    >
                      <span class="option-letter">{{ ['A', 'B', 'C', 'D'][index] }}</span>
                      <span class="option-text">{{ option }}</span>
                    </button>
                  </div>
                  
                  <transition name="fade">
                    <div v-if="aiState.answered" class="answer-feedback">
                      <div class="feedback-icon" :class="isCorrect ? 'correct' : 'wrong'">
                        {{ isCorrect ? '✓' : '✗' }}
                      </div>
                      <p class="feedback-text font-serif">
                        {{ isCorrect ? '回答正确！' : '回答错误' }}
                      </p>
                      <p class="explanation font-serif">{{ currentQuestion.explanation }}</p>
                      
                      <div class="ai-chat-section">
                        <div class="chat-divider">
                          <span>还想了解更多？</span>
                        </div>
                        
                        <div class="chat-messages" ref="chatContainer">
                          <div 
                            v-for="(msg, idx) in chatMessages" 
                            :key="idx" 
                            class="chat-message"
                            :class="msg.role"
                          >
                            <div class="msg-avatar">
                              <span v-if="msg.role === 'user'">我</span>
                              <svg v-else viewBox="0 0 24 24" width="20" height="20">
                                <circle cx="12" cy="8" r="5" fill="#A8DADC"/>
                                <path d="M4 20 Q4 14 12 14 Q20 14 20 20" fill="#A8DADC"/>
                              </svg>
                            </div>
                            <div class="msg-content">
                              <p>{{ msg.content }}</p>
                            </div>
                          </div>
                          <div v-if="chatState.streaming" class="chat-message assistant">
                            <div class="msg-avatar">
                              <svg viewBox="0 0 24 24" width="20" height="20">
                                <circle cx="12" cy="8" r="5" fill="#A8DADC"/>
                                <path d="M4 20 Q4 14 12 14 Q20 14 20 20" fill="#A8DADC"/>
                              </svg>
                            </div>
                            <div class="msg-content streaming">
                              <span class="dot"></span>
                              <span class="dot"></span>
                              <span class="dot"></span>
                            </div>
                          </div>
                        </div>
                        
                        <div class="chat-input-area">
                          <input
                            v-model="chatInput"
                            type="text"
                            class="chat-input"
                            placeholder="向书生提问..."
                            @keyup.enter="sendChatMessage"
                            :disabled="chatState.streaming"
                          />
                          <button 
                            class="send-btn" 
                            @click="sendChatMessage"
                            :disabled="!chatInput.trim() || chatState.streaming"
                          >
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                              <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
                            </svg>
                          </button>
                        </div>
                      </div>
                    </div>
                  </transition>
                </div>
              </transition>
            </div>
          </div>
          
          <div class="card-footer">
            <button class="action-btn" :class="{ liked: isLiked }" @click="toggleLike">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              <span>{{ likeCount }}</span>
            </button>
            
            <button class="action-btn" @click="handleShare">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="18" cy="5" r="3"/>
                <circle cx="6" cy="12" r="3"/>
                <circle cx="18" cy="19" r="3"/>
                <path d="M8.59 13.51l6.83 3.98M15.41 6.51l-6.82 3.98"/>
              </svg>
              <span>分享</span>
            </button>
            
            <button class="action-btn ar-btn" @click="handleArExperience">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="12 2 22 8.5 22 15.5 12 22 2 15.5 2 8.5 12 2"/>
                <line x1="12" y1="22" x2="12" y2="15.5"/>
                <polyline points="22 8.5 12 15.5 2 8.5"/>
              </svg>
              <span>AR体验</span>
            </button>
          </div>
          
          <div class="card-texture" />
          <div class="card-border-pattern" />
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onUnmounted, nextTick } from 'vue'
import { aiApi } from '@/api'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  artifact: {
    type: Object,
    required: true,
    validator: (v) => v && v.id && v.name
  }
})

const emit = defineEmits(['close', 'like', 'share', 'arExperience', 'aiComplete'])

const textContainer = ref(null)
const chatContainer = ref(null)
const isLiked = ref(false)
const likeCount = ref(0)

const aiState = ref({
  started: false,
  loading: false,
  typing: false,
  showQuestion: false,
  answered: false,
  selectedAnswer: null
})

const chatState = ref({
  streaming: false,
  chatId: null
})

const chatInput = ref('')
const chatMessages = ref([])

const fullText = ref('')
const displayText = ref('')
const currentQuestion = ref(null)
const isCorrect = computed(() => {
  return aiState.value.selectedAnswer === currentQuestion.value?.answer
})

let typewriterTimer = null

const getArtifactIntro = () => {
  const { name, dynasty, category } = props.artifact
  
  if (category === '自然景观') {
    return `${name}是一处令人叹为观止的自然奇观。它形成于${dynasty || '遥远的地质年代'}，是大自然鬼斧神工的杰作。这里的每一处景观都凝聚着亿万年的地质变迁，展现了自然界的神奇力量。漫步其中，仿佛穿越时空，感受大自然的壮美与神秘。`
  }
  
  if (category === '博物馆') {
    return `${name}是一座承载着历史记忆的文化殿堂。${dynasty === '现代' ? '它建于现代，' : `它始建于${dynasty}，`}收藏了众多珍贵文物，是了解中华文明的重要窗口。馆内每一件展品都诉说着一段历史，等待着您去探索和发现。`
  }
  
  if (category === '古建筑') {
    return `${name}是一座极具历史价值的古建筑。它建于${dynasty || '古代'}，见证了无数历史风云的变幻。从其精妙的建筑结构和独特的艺术风格中，我们可以窥见古人的智慧与审美追求。每一根梁柱、每一片瓦当，都承载着厚重的历史。`
  }
  
  return `这件${name}是一件极具历史价值的珍贵文物。它诞生于${dynasty || '古代'}，见证了那个时代的辉煌与沧桑。从其精美的工艺和独特的造型中，我们可以窥见古人的智慧与审美追求。每一道纹路、每一处细节，都诉说着跨越千年的故事。`
}

const fetchAiGuide = async () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        intro: getArtifactIntro(),
        question: `${props.artifact.name}最显著的艺术特征是什么？`,
        options: [
          '造型优美，线条流畅',
          '纹饰繁复，工艺精湛',
          '色彩艳丽，保存完好',
          '体积硕大，气势恢宏'
        ],
        answer: 1,
        explanation: `正确答案是B。${props.artifact.name}的纹饰繁复精美，工艺技术达到了极高的水平，体现了当时工匠们超凡的技艺和对美的极致追求。`
      })
    }, 1500)
  })
}

const startAiGuide = async () => {
  aiState.value.loading = true
  
  try {
    const data = await fetchAiGuide()
    
    fullText.value = data.intro
    currentQuestion.value = {
      question: data.question,
      options: data.options,
      answer: data.answer,
      explanation: data.explanation
    }
    
    aiState.value.loading = false
    aiState.value.started = true
    aiState.value.typing = true
    
    startTypewriter()
  } catch (error) {
    console.error('AI讲解加载失败:', error)
    aiState.value.loading = false
  }
}

const startTypewriter = () => {
  let index = 0
  const speed = 50
  
  typewriterTimer = setInterval(() => {
    if (index < fullText.value.length) {
      displayText.value += fullText.value.charAt(index)
      index++
      
      if (textContainer.value) {
        textContainer.value.scrollTop = textContainer.value.scrollHeight
      }
    } else {
      clearInterval(typewriterTimer)
      aiState.value.typing = false
      
      setTimeout(() => {
        aiState.value.showQuestion = true
      }, 500)
    }
  }, speed)
}

const selectAnswer = (index) => {
  if (aiState.value.answered) return
  
  aiState.value.selectedAnswer = index
  aiState.value.answered = true
  
  chatState.value.chatId = `chat_${props.artifact.id}_${Date.now()}`
  
  emit('aiComplete', {
    artifact: props.artifact,
    correct: isCorrect.value
  })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

const sendChatMessage = async () => {
  const message = chatInput.value.trim()
  if (!message || chatState.value.streaming) return
  
  chatMessages.value.push({
    role: 'user',
    content: message
  })
  chatInput.value = ''
  scrollToBottom()
  
  chatState.value.streaming = true
  
  const { name, dynasty, category } = props.artifact
  let artifactDesc = `${name}`
  if (category === '自然景观') {
    artifactDesc += `（自然景观，形成于${dynasty || '地质年代'}）`
  } else if (category === '博物馆') {
    artifactDesc += `（博物馆，${dynasty === '现代' ? '现代建筑' : `始建于${dynasty}`}）`
  } else if (category === '古建筑') {
    artifactDesc += `（古建筑，建于${dynasty || '古代'}）`
  } else {
    artifactDesc += `（${dynasty || '古代'}，${category || '文物'}）`
  }
  
  try {
    const response = await aiApi.chat(
      `关于${artifactDesc}：${message}`,
      chatState.value.chatId
    )
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let aiResponse = ''
    let buffer = ''
    
    chatMessages.value.push({
      role: 'assistant',
      content: ''
    })
    
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
            aiResponse += content
          }
        } else if (line.trim() && !line.startsWith(':')) {
          aiResponse += line
        }
      }
      
      chatMessages.value[chatMessages.value.length - 1].content = aiResponse
      scrollToBottom()
    }
  } catch (error) {
    console.error('AI对话失败:', error)
    chatMessages.value.push({
      role: 'assistant',
      content: '抱歉，我暂时无法回答这个问题，请稍后再试。'
    })
  } finally {
    chatState.value.streaming = false
    scrollToBottom()
  }
}

const getOptionClass = (index) => {
  if (!aiState.value.answered) return ''
  
  if (index === currentQuestion.value.answer) {
    return 'correct'
  }
  if (index === aiState.value.selectedAnswer && !isCorrect.value) {
    return 'wrong'
  }
  return ''
}

const toggleLike = () => {
  isLiked.value = !isLiked.value
  likeCount.value += isLiked.value ? 1 : -1
  emit('like', { artifact: props.artifact, liked: isLiked.value })
}

const handleShare = () => {
  emit('share', props.artifact)
}

const handleArExperience = () => {
  emit('arExperience', props.artifact)
  handleClose()
}

const handleClose = () => {
  if (typewriterTimer) {
    clearInterval(typewriterTimer)
  }
  
  aiState.value = {
    started: false,
    loading: false,
    typing: false,
    showQuestion: false,
    answered: false,
    selectedAnswer: null
  }
  displayText.value = ''
  fullText.value = ''
  currentQuestion.value = null
  
  chatState.value = {
    streaming: false,
    chatId: null
  }
  chatMessages.value = []
  chatInput.value = ''
  
  emit('close')
}

watch(() => props.visible, (newVal) => {
  if (!newVal) {
    handleClose()
  }
})

onUnmounted(() => {
  if (typewriterTimer) {
    clearInterval(typewriterTimer)
  }
})
</script>

<style scoped>
.ai-guide-card-overlay {
  position: fixed;
  inset: 0;
  background: rgba(26, 26, 26, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
  padding: 0 16px;
}

.ai-guide-card {
  width: 100%;
  max-width: 480px;
  max-height: 85vh;
  background: #FDFBF7;
  border-radius: 24px 24px 0 0;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.card-texture {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
  opacity: 0.03;
  pointer-events: none;
}

.card-border-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    #A8DADC 20%,
    #2D4059 50%,
    #A8DADC 80%,
    transparent 100%
  );
}

.card-header {
  display: flex;
  gap: 16px;
  padding: 20px;
  padding-top: 24px;
  position: relative;
  z-index: 1;
}

.artifact-image {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
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
  font-size: 32px;
  opacity: 0.3;
}

.header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.artifact-title {
  font-size: 20px;
  color: #2D4059;
  margin: 0;
  letter-spacing: 0.05em;
}

.artifact-tags {
  display: flex;
  gap: 8px;
}

.tag {
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 12px;
  background: rgba(45, 64, 89, 0.08);
  color: #5D4E37;
}

.tag.dynasty {
  background: rgba(168, 218, 220, 0.3);
  color: #2D4059;
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(45, 64, 89, 0.08);
  border: none;
  color: #8B9A9C;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(45, 64, 89, 0.15);
  color: #2D4059;
}

.card-body {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px 20px;
  position: relative;
  z-index: 1;
}

.ai-intro {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  text-align: center;
}

.scholar-avatar {
  position: relative;
  width: 140px;
  height: 140px;
  margin-bottom: 12px;
}

.avatar-glow {
  position: absolute;
  inset: -15px;
  background: radial-gradient(circle, rgba(201, 162, 39, 0.2) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse-glow 3s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.05); }
}

.avatar-ring {
  position: absolute;
  inset: -5px;
  border: 2px solid rgba(201, 162, 39, 0.3);
  border-radius: 50%;
  animation: ring-rotate 8s linear infinite;
}

@keyframes ring-rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.scholar-svg {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  filter: drop-shadow(0 8px 24px rgba(45, 64, 89, 0.2));
}

.avatar-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #C9A227 0%, #B8860B 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid #FDFBF7;
  box-shadow: 0 2px 8px rgba(201, 162, 39, 0.4);
}

.badge-text {
  font-size: 10px;
  font-weight: bold;
  color: #FFF;
}

.scholar-title {
  font-size: 18px;
  color: #2D4059;
  font-weight: 600;
  margin-bottom: 8px;
  letter-spacing: 0.1em;
}

.intro-text {
  font-size: 14px;
  color: #8B9A9C;
  line-height: 1.8;
  margin: 0 0 20px 0;
}

.start-ai-btn {
  padding: 14px 36px;
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  border: none;
  border-radius: 28px;
  color: #F5F2EB;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 16px rgba(45, 64, 89, 0.25);
}

.start-ai-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(45, 64, 89, 0.35);
}

.start-ai-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.loading-icon .rotate {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.ai-content {
  padding: 16px 0;
}

.guide-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: rgba(45, 64, 89, 0.03);
  border-radius: 16px;
}

.guide-avatar {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
}

.guide-avatar svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 2px 6px rgba(45, 64, 89, 0.15));
}

.guide-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.guide-name {
  font-size: 15px;
  color: #2D4059;
  font-weight: 600;
}

.guide-status {
  font-size: 12px;
  color: #8B9A9C;
}

.guide-text {
  background: rgba(45, 64, 89, 0.04);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  min-height: 100px;
  max-height: 200px;
  overflow-y: auto;
}

.typewriter-text {
  font-size: 14px;
  color: #3D3D3D;
  line-height: 1.8;
  margin: 0;
}

.cursor {
  color: #2D4059;
  animation: blink 0.8s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.quiz-section {
  background: rgba(168, 218, 220, 0.1);
  border-radius: 16px;
  padding: 20px;
  margin-top: 16px;
}

.quiz-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.quiz-icon {
  font-size: 18px;
}

.quiz-title {
  font-size: 15px;
  color: #2D4059;
  font-weight: 500;
}

.question-text {
  font-size: 14px;
  color: #3D3D3D;
  line-height: 1.6;
  margin: 0 0 16px 0;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #FDFBF7;
  border: 1px solid rgba(45, 64, 89, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: left;
}

.option-btn:hover:not(:disabled) {
  background: rgba(168, 218, 220, 0.15);
  border-color: #A8DADC;
}

.option-btn:disabled {
  cursor: default;
}

.option-btn.correct {
  background: rgba(72, 187, 120, 0.15);
  border-color: #48BB78;
}

.option-btn.wrong {
  background: rgba(245, 101, 101, 0.1);
  border-color: #F56565;
}

.option-letter {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(45, 64, 89, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #2D4059;
  flex-shrink: 0;
}

.option-btn.correct .option-letter {
  background: #48BB78;
  color: #F5F2EB;
}

.option-btn.wrong .option-letter {
  background: #F56565;
  color: #F5F2EB;
}

.option-text {
  font-size: 13px;
  color: #3D3D3D;
}

.answer-feedback {
  margin-top: 16px;
  padding: 16px;
  background: #FDFBF7;
  border-radius: 12px;
  text-align: center;
}

.feedback-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin: 0 auto 12px;
}

.feedback-icon.correct {
  background: rgba(72, 187, 120, 0.2);
  color: #48BB78;
}

.feedback-icon.wrong {
  background: rgba(245, 101, 101, 0.15);
  color: #F56565;
}

.feedback-text {
  font-size: 16px;
  color: #2D4059;
  margin: 0 0 8px 0;
}

.explanation {
  font-size: 13px;
  color: #8B9A9C;
  line-height: 1.6;
  margin: 0;
}

.ai-chat-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(45, 64, 89, 0.1);
}

.chat-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  color: #8B9A9C;
  font-size: 12px;
}

.chat-divider::before,
.chat-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(45, 64, 89, 0.1);
}

.chat-messages {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-message {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.chat-message.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(45, 64, 89, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #8B9A9C;
  flex-shrink: 0;
}

.chat-message.assistant .msg-avatar {
  background: rgba(168, 218, 220, 0.3);
}

.msg-content {
  max-width: 75%;
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 13px;
  line-height: 1.5;
}

.chat-message.user .msg-content {
  background: #2D4059;
  color: #F5F2EB;
  border-bottom-right-radius: 4px;
}

.chat-message.assistant .msg-content {
  background: rgba(45, 64, 89, 0.08);
  color: #2D4059;
  border-bottom-left-radius: 4px;
}

.msg-content p {
  margin: 0;
}

.msg-content.streaming {
  display: flex;
  gap: 4px;
  align-items: center;
}

.msg-content.streaming .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #A8DADC;
  animation: bounce 1.4s infinite ease-in-out both;
}

.msg-content.streaming .dot:nth-child(1) { animation-delay: -0.32s; }
.msg-content.streaming .dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.chat-input-area {
  display: flex;
  gap: 8px;
  align-items: center;
}

.chat-input {
  flex: 1;
  height: 40px;
  border: 1px solid rgba(45, 64, 89, 0.15);
  border-radius: 20px;
  padding: 0 16px;
  font-size: 14px;
  outline: none;
  background: #FFF;
}

.chat-input:focus {
  border-color: #A8DADC;
}

.chat-input:disabled {
  background: rgba(45, 64, 89, 0.05);
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  color: #F5F2EB;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.card-footer {
  display: flex;
  justify-content: space-around;
  padding: 16px 20px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
  border-top: 1px solid rgba(45, 64, 89, 0.08);
  position: relative;
  z-index: 1;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  background: transparent;
  border: none;
  color: #8B9A9C;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  color: #2D4059;
}

.action-btn.liked {
  color: #C44536;
}

.action-btn.liked svg {
  fill: currentColor;
}

.action-btn.ar-btn {
  color: #2D4059;
}

.card-slide-enter-active,
.card-slide-leave-active {
  transition: all 0.4s ease;
}

.card-slide-enter-from,
.card-slide-leave-to {
  opacity: 0;
}

.card-slide-enter-from .ai-guide-card,
.card-slide-leave-to .ai-guide-card {
  transform: translateY(100%);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 428px) {
  .ai-guide-card {
    max-height: 90vh;
  }
  
  .card-header {
    padding: 16px;
  }
  
  .artifact-image {
    width: 64px;
    height: 64px;
  }
  
  .artifact-title {
    font-size: 18px;
  }
  
  .ai-intro {
    padding: 30px 16px;
  }
  
  .scholar-avatar {
    width: 100px;
    height: 120px;
  }
}
</style>
