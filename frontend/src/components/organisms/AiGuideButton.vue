<template>
  <div class="ai-guide-container">
    <transition name="bubble">
      <div v-if="showChat" class="chat-bubble">
        <div class="chat-header">
          <span class="chat-title font-serif">AI导游</span>
          <button class="close-chat-btn" @click="closeChat">×</button>
        </div>
        
        <div class="chat-messages" ref="messagesRef">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message"
            :class="msg.role"
          >
            <div class="message-content">
              {{ msg.content }}
            </div>
          </div>
          
          <div v-if="isTyping" class="message assistant">
            <div class="typing-indicator">
              <span class="typing-dot" />
              <span class="typing-dot" />
              <span class="typing-dot" />
            </div>
          </div>
        </div>
        
        <div class="chat-input-area">
          <button class="voice-btn" @click="toggleVoice">
            <span v-if="!isListening">🎤</span>
            <span v-else class="listening">🎙️</span>
          </button>
          
          <input
            v-model="inputText"
            type="text"
            class="chat-input"
            placeholder="请输入问题..."
            @keyup.enter="sendMessage"
          />
          
          <button class="send-btn" @click="sendMessage" :disabled="!inputText.trim()">
            发送
          </button>
        </div>
      </div>
    </transition>
    
    <button
      class="ai-float-btn"
      :class="{ active: showChat }"
      @click="toggleChat"
    >
      <span class="btn-icon">{{ showChat ? '✕' : '🤖' }}</span>
    </button>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'

const showChat = ref(false)
const inputText = ref('')
const isTyping = ref(false)
const isListening = ref(false)
const messagesRef = ref(null)

const messages = ref([
  {
    role: 'assistant',
    content: '您好！我是文迹AI导游，有什么可以帮您的吗？'
  }
])

const toggleChat = () => {
  showChat.value = !showChat.value
}

const closeChat = () => {
  showChat.value = false
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text) return
  
  messages.value.push({
    role: 'user',
    content: text
  })
  
  inputText.value = ''
  
  await nextTick()
  scrollToBottom()
  
  isTyping.value = true
  
  setTimeout(() => {
    const responses = [
      '这是一个很好的问题！让我为您介绍一下...',
      '根据我的了解，这个文物有着悠久的历史...',
      '这个景点非常值得一看，建议您...',
      '您可以在AR模式下更直观地欣赏这件文物...'
    ]
    
    messages.value.push({
      role: 'assistant',
      content: responses[Math.floor(Math.random() * responses.length)]
    })
    
    isTyping.value = false
    
    nextTick(() => {
      scrollToBottom()
    })
  }, 1500)
}

const toggleVoice = () => {
  isListening.value = !isListening.value
  
  if (isListening.value) {
    console.log('开始语音识别...')
    
    setTimeout(() => {
      inputText.value = '请介绍一下这个文物'
      isListening.value = false
    }, 2000)
  } else {
    console.log('停止语音识别')
  }
}

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}
</script>

<style scoped>
.ai-guide-container {
  position: fixed;
  bottom: 80px;
  right: 20px;
  z-index: 100;
}

.ai-float-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2D4059 0%, #1A1A1A 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(45, 64, 89, 0.3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.ai-float-btn:hover {
  transform: scale(1.1);
}

.ai-float-btn.active {
  background: linear-gradient(135deg, #C44536 0%, #8B2E23 100%);
}

.btn-icon {
  font-size: 24px;
}

.chat-bubble {
  position: absolute;
  bottom: 70px;
  right: 0;
  width: 320px;
  max-height: 450px;
  background: #F5F2EB;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(45, 64, 89, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #2D4059 0%, #1A1A1A 100%);
}

.chat-title {
  font-size: 16px;
  color: #F5F2EB;
  letter-spacing: 0.1em;
}

.close-chat-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  color: #F5F2EB;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  max-height: 300px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  display: flex;
  max-width: 85%;
}

.message.user {
  align-self: flex-end;
}

.message.assistant {
  align-self: flex-start;
}

.message-content {
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
}

.message.user .message-content {
  background: #2D4059;
  color: #F5F2EB;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-content {
  background: rgba(45, 64, 89, 0.08);
  color: #2D4059;
  border-bottom-left-radius: 4px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 10px 14px;
  background: rgba(45, 64, 89, 0.08);
  border-radius: 16px;
  border-bottom-left-radius: 4px;
}

.typing-dot {
  width: 8px;
  height: 8px;
  background: #8B9A9C;
  border-radius: 50%;
  animation: typing 1.4s ease-in-out infinite;
}

.typing-dot:nth-child(1) { animation-delay: 0s; }
.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-4px);
  }
}

.chat-input-area {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid rgba(45, 64, 89, 0.08);
}

.voice-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: rgba(45, 64, 89, 0.06);
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.voice-btn:hover {
  background: rgba(45, 64, 89, 0.1);
}

.voice-btn .listening {
  animation: pulse-voice 1s ease-in-out infinite;
}

@keyframes pulse-voice {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.chat-input {
  flex: 1;
  height: 40px;
  border: 1px solid rgba(45, 64, 89, 0.15);
  border-radius: 20px;
  padding: 0 16px;
  font-size: 14px;
  background: #FFF;
  color: #2D4059;
  outline: none;
}

.chat-input:focus {
  border-color: #A8DADC;
}

.send-btn {
  height: 40px;
  padding: 0 16px;
  background: #2D4059;
  border: none;
  border-radius: 20px;
  color: #F5F2EB;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.send-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.bubble-enter-active,
.bubble-leave-active {
  transition: all 0.3s ease;
}

.bubble-enter-from,
.bubble-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}

@media (max-width: 428px) {
  .ai-guide-container {
    bottom: 70px;
    right: 16px;
  }
  
  .ai-float-btn {
    width: 48px;
    height: 48px;
  }
  
  .btn-icon {
    font-size: 20px;
  }
  
  .chat-bubble {
    width: calc(100vw - 32px);
    right: -16px;
    max-height: 400px;
  }
}
</style>
