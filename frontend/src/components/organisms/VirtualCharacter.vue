<template>
  <div class="virtual-character" :class="{ expanded: isExpanded }">
    <div class="character-body" @click="toggleExpand">
      <div class="character-avatar">
        <div class="avatar-glow"></div>
        <div class="avatar-inner">
          <div class="face">
            <div class="eyes">
              <span class="eye left" :class="{ blink: isBlinking }"></span>
              <span class="eye right" :class="{ blink: isBlinking }"></span>
            </div>
            <div class="mouth" :class="{ talking: isTalking }"></div>
          </div>
          <div class="hair"></div>
        </div>
      </div>
      <div v-if="!isExpanded" class="speech-bubble mini">
        <span>{{ miniMessages[currentMiniMessage] }}</span>
      </div>
    </div>
    
    <transition name="slide-up">
      <div v-if="isExpanded" class="character-panel">
        <div class="panel-header">
          <span class="character-name font-serif">文迹小助手</span>
          <button class="close-btn" @click="isExpanded = false">×</button>
        </div>
        
        <div class="panel-messages" ref="messagesContainer">
          <div 
            v-for="(msg, idx) in messages" 
            :key="idx" 
            class="message"
            :class="{ 'from-user': msg.fromUser }"
          >
            <div class="message-content">{{ msg.text }}</div>
          </div>
          <div v-if="isTyping" class="message typing">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
        
        <div class="quick-actions">
          <button 
            v-for="action in quickActions" 
            :key="action.id"
            class="action-btn"
            @click="handleQuickAction(action)"
          >
            {{ action.icon }} {{ action.label }}
          </button>
        </div>
        
        <div class="panel-input">
          <input 
            v-model="inputText" 
            type="text" 
            placeholder="问我任何关于传统文化的问题..."
            @keyup.enter="sendMessage"
          />
          <button class="send-btn" @click="sendMessage" :disabled="!inputText.trim()">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13"/>
              <polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { aiApi } from '@/api'

const isExpanded = ref(false)
const isBlinking = ref(false)
const isTalking = ref(false)
const isTyping = ref(false)
const inputText = ref('')
const messages = ref([])
const messagesContainer = ref(null)
const currentMiniMessage = ref(0)

const miniMessages = [
  '你好呀~',
  '有什么可以帮你的？',
  '点击我聊聊吧~',
  '探索传统文化~'
]

const quickActions = [
  { id: 1, icon: '📜', label: '推荐景点', prompt: '请推荐一些值得去的传统文化景点' },
  { id: 2, icon: '🎨', label: '文物知识', prompt: '给我讲一个有趣的文物故事' },
  { id: 3, icon: '✍️', label: '诗词创作', prompt: '请为我创作一首关于传统文化的诗' },
  { id: 4, icon: '❓', label: '使用帮助', prompt: '请介绍一下这个App的功能' }
]

let blinkInterval = null
let miniMessageInterval = null

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
  if (isExpanded.value && messages.value.length === 0) {
    messages.value.push({
      text: '你好！我是文迹小助手，有什么可以帮你的吗？',
      fromUser: false
    })
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const sendMessage = async () => {
  if (!inputText.value.trim() || isTyping.value) return
  
  const text = inputText.value.trim()
  messages.value.push({ text, fromUser: true })
  inputText.value = ''
  await scrollToBottom()
  
  isTyping.value = true
  isTalking.value = true
  
  try {
    const response = await aiApi.chat(text, `assistant_${Date.now()}`)
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let reply = ''
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
            reply += content
          }
        }
      }
    }
    
    if (reply.trim()) {
      messages.value.push({ text: reply.trim(), fromUser: false })
    } else {
      messages.value.push({ text: '抱歉，我暂时无法回答这个问题，请稍后再试~', fromUser: false })
    }
  } catch (error) {
    console.error('AI对话失败:', error)
    messages.value.push({ text: '网络似乎有点问题，请稍后再试~', fromUser: false })
  } finally {
    isTyping.value = false
    isTalking.value = false
    await scrollToBottom()
  }
}

const handleQuickAction = (action) => {
  inputText.value = action.prompt
  sendMessage()
}

const startBlinking = () => {
  blinkInterval = setInterval(() => {
    isBlinking.value = true
    setTimeout(() => {
      isBlinking.value = false
    }, 150)
  }, 3000 + Math.random() * 2000)
}

const startMiniMessages = () => {
  miniMessageInterval = setInterval(() => {
    currentMiniMessage.value = (currentMiniMessage.value + 1) % miniMessages.length
  }, 4000)
}

watch(isExpanded, (val) => {
  if (val) {
    clearInterval(miniMessageInterval)
  } else {
    startMiniMessages()
  }
})

onMounted(() => {
  startBlinking()
  startMiniMessages()
})

onUnmounted(() => {
  clearInterval(blinkInterval)
  clearInterval(miniMessageInterval)
})
</script>

<style scoped>
.virtual-character {
  position: fixed;
  right: 20px;
  bottom: 100px;
  z-index: 1000;
}

.character-body {
  position: relative;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.character-body:hover {
  transform: scale(1.05);
}

.character-avatar {
  width: 60px;
  height: 60px;
  position: relative;
}

.avatar-glow {
  position: absolute;
  inset: -4px;
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 50%, #2D4059 100%);
  border-radius: 50%;
  animation: glow 2s ease-in-out infinite;
}

@keyframes glow {
  0%, 100% { opacity: 0.6; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.05); }
}

.avatar-inner {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, #F5E6D3 0%, #E8D5C4 100%);
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #2D4059;
}

.face {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 8px;
}

.eyes {
  display: flex;
  gap: 12px;
  margin-bottom: 6px;
}

.eye {
  width: 8px;
  height: 8px;
  background: #2D4059;
  border-radius: 50%;
  transition: transform 0.1s ease;
}

.eye.blink {
  transform: scaleY(0.1);
}

.mouth {
  width: 10px;
  height: 4px;
  background: #C4A484;
  border-radius: 0 0 10px 10px;
  transition: all 0.2s ease;
}

.mouth.talking {
  width: 14px;
  height: 8px;
  animation: talk 0.3s ease infinite;
}

@keyframes talk {
  0%, 100% { height: 4px; }
  50% { height: 10px; }
}

.hair {
  position: absolute;
  top: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 20px;
  background: #2D4059;
  border-radius: 50% 50% 0 0;
}

.speech-bubble.mini {
  position: absolute;
  top: -10px;
  right: 65px;
  background: #2D4059;
  color: #F5F2EB;
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 12px;
  white-space: nowrap;
  animation: float 2s ease-in-out infinite;
}

.speech-bubble.mini::after {
  content: '';
  position: absolute;
  right: -6px;
  top: 50%;
  transform: translateY(-50%);
  border: 6px solid transparent;
  border-left-color: #2D4059;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.character-panel {
  position: absolute;
  bottom: 70px;
  right: 0;
  width: 320px;
  max-height: 450px;
  background: #F5F2EB;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(45, 64, 89, 0.2);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  color: #F5F2EB;
}

.character-name {
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  color: #F5F2EB;
  font-size: 20px;
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.close-btn:hover {
  opacity: 1;
}

.panel-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  max-height: 250px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  max-width: 85%;
}

.message.from-user {
  align-self: flex-end;
}

.message-content {
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.message:not(.from-user) .message-content {
  background: #E8ECEF;
  color: #2D4059;
  border-bottom-left-radius: 4px;
}

.message.from-user .message-content {
  background: #2D4059;
  color: #F5F2EB;
  border-bottom-right-radius: 4px;
}

.message.typing {
  align-self: flex-start;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: #E8ECEF;
  border-radius: 16px;
  border-bottom-left-radius: 4px;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background: #8B9A9C;
  border-radius: 50%;
  animation: bounce 1.4s ease-in-out infinite;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 16px 12px;
}

.action-btn {
  padding: 6px 12px;
  background: #E8ECEF;
  border: none;
  border-radius: 16px;
  font-size: 12px;
  color: #2D4059;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #2D4059;
  color: #F5F2EB;
}

.panel-input {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #E8ECEF;
  background: #FFF;
}

.panel-input input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #E8ECEF;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.panel-input input:focus {
  border-color: #2D4059;
}

.send-btn {
  width: 40px;
  height: 40px;
  background: #2D4059;
  border: none;
  border-radius: 50%;
  color: #F5F2EB;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: #3D5A80;
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
  transform: translateY(20px);
}

@media (max-width: 428px) {
  .virtual-character {
    right: 16px;
    bottom: 90px;
  }
  
  .character-panel {
    width: calc(100vw - 32px);
    right: -8px;
    max-height: 400px;
  }
}
</style>
