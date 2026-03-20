<template>
  <Layout title="AI导游" :showBack="true">
    <!-- 当前景点 -->
    <div class="spot-card">
      <h1 class="spot-name">{{ currentSpot.name }}</h1>
      <p class="spot-desc">{{ currentSpot.desc }}</p>
    </div>

    <!-- AR 入口 -->
    <button class="ar-btn" @click="openAR">
      👁️ AR 复原古窑场景
    </button>
    <router-link to="/user/history" class="history-link">
      📜 查看历史问答
    </router-link>
    
    <!-- AI 智能问答 -->
    <section class="ai-section">
      <h2 class="ai-title">向智游导游提问</h2>

      <div class="input-container">
        <textarea
          v-model="questionText"
          class="ask-input"
          placeholder="例如：这块瓷片是哪个朝代的？"
          @keydown.enter.exact.prevent="submitQuestion"
          :disabled="isResponding"
        ></textarea>
        <button class="attach-btn" @click="triggerFileInput" :disabled="isResponding">📎</button>
        <input
          ref="fileInputRef"
          type="file"
          accept="image/*"
          multiple
          @change="handleFileSelect"
          style="display: none"
        />
      </div>

      <!-- 图片预览区 -->
      <div class="preview-area" v-if="previewUrls.length > 0">
        <div
          v-for="(url, index) in previewUrls"
          :key="index"
          class="preview-item"
        >
          <img :src="url" alt="预览" />
          <button class="remove-preview" @click="removePreview(index)" :disabled="isResponding">×</button>
        </div>
      </div>

      <p class="privacy-note">
        📷 可上传图片，AI 将结合图文为您解答（图片仅用于本次问答）
      </p>

      <div class="mic-row">
        <button class="mic-btn" @click="useVoice" :disabled="isResponding">🎤</button>
        <button class="send-btn" @click="submitQuestion" :disabled="isResponding || !canSubmit">
          {{ isResponding ? '⏳' : '➤' }}
        </button>
      </div>

      <!-- AI 回答区域 -->
      <div v-if="answer || isResponding" class="answer-box">
        <div class="answer-content">{{ answer }}</div>
        <div v-if="isResponding" class="typing-indicator">文迹正在思考中...</div>
      </div>
    </section>

    <!-- 功能预告 -->
    <p class="coming-soon">💡 自动语音讲解、路线推荐等功能开发中…</p>
  </Layout>
</template>

<script setup>
import { ref, computed, onBeforeUnmount } from 'vue'
import { chatService } from '@/api/user' // 使用项目中定义的 API 服务
import Layout from '@/components/Layout.vue'
// 引入SSEHandler
import { SSEHandler } from '@/utils/sseHandler.js'
// ====== 响应式状态 ======
const questionText = ref('')
const fileInputRef = ref(null)
const uploadedFiles = ref([])
const previewUrls = ref([])
const answer = ref('')
const isResponding = ref(false)
const currentChatId = ref(null) // 添加这一行

// 当前景点（可从路由或 API 获取）
const currentSpot = {
  name: '青花秘境',
  desc: '明代官窑核心烧造区'
}

// 计算属性：判断是否可以提交
const canSubmit = computed(() => {
  return (questionText.value.trim() || uploadedFiles.value.length > 0) && !isResponding.value
})

// ====== 方法 ======
import { useRouter } from 'vue-router'
const router = useRouter()


function triggerFileInput() {
  if (!isResponding.value) {
    fileInputRef.value?.click()
  }
}

function handleFileSelect(event) {
  if (isResponding.value) return
  
  const files = Array.from(event.target.files).filter(file =>
    file.type.startsWith('image/')
  )

  // 清空旧预览 URL（释放内存）
  previewUrls.value.forEach(url => URL.revokeObjectURL(url))
  previewUrls.value = []

  uploadedFiles.value = files
  previewUrls.value = files.map(file => URL.createObjectURL(file))
}

function removePreview(index) {
  if (isResponding.value) return
  
  // 释放 URL
  URL.revokeObjectURL(previewUrls.value[index])
  // 移除
  previewUrls.value.splice(index, 1)
  uploadedFiles.value.splice(index, 1)
}



// 在组件初始化时创建一个新的会话
function createNewChatSession() {
  // 生成一个唯一的聊天ID
  const chatId = `chat_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  currentChatId.value = chatId;
  return chatId;
}
async function submitQuestion() {
  const text = questionText.value.trim()
  const hasImage = uploadedFiles.value.length > 0

  if (!text && !hasImage) {
    alert('请输入问题，或上传图片')
    return
  }

  if (isResponding.value) return // 防止重复提交

  // 如果还没有会话ID，则创建一个
  if (!currentChatId.value) {
    createNewChatSession();
  }

  // 重置答案并设置响应状态
  answer.value = ''
  isResponding.value = true

  try {
    // 构建表单数据
    const formData = new FormData()
    
    // 添加必需的参数
    formData.append('chatId', currentChatId.value) // 添加会话ID
    
    if (text) {
      formData.append('prompt', text)
    }
    
    // 添加图片（如果有）
    uploadedFiles.value.forEach((file, index) => {
      formData.append('images', file)
    })

    // 使用SSEHandler处理流式响应
    const sseHandler = new SSEHandler()
    
    sseHandler.onData = (data) => {
      // 实时追加AI回复内容
      if (typeof data === 'string') {
        answer.value += data
      } else if (data.content) {
        answer.value += data.content
      }
    }

    sseHandler.onError = (error) => {
      console.error('SSE Error:', error)
      answer.value = `请求失败: ${error.message || '未知错误'}`
      isResponding.value = false
    }

    sseHandler.onComplete = () => {
      isResponding.value = false
    }

    await sseHandler.sendStreamRequest('/api/ai/chat', formData)
    
  } catch (error) {
    console.error('AI 提问失败:', error)
    answer.value = '请求发送失败，请检查网络后重试'
    isResponding.value = false
  }
}
// 如果你想开始一个新会话，可以添加这个函数
function startNewSession() {
  createNewChatSession();
  answer.value = '';
  questionText.value = '';
  uploadedFiles.value = [];
  previewUrls.value = [];
}

function useVoice() {
  if (isResponding.value) return
  
  const input = prompt('模拟语音输入：', '这块瓷片是什么年代的？')
  if (input) {
    questionText.value = input
    submitQuestion()
  }
}

function openAR() {
  alert('AR模式即将开启\n（当前为演示）')
  // 实际可跳转 AR 页面：router.push('/ar')
}

// ====== 生命周期清理 ======
onBeforeUnmount(() => {
  // 组件卸载前释放所有 object URLs
  previewUrls.value.forEach(url => URL.revokeObjectURL(url))
  previewUrls.value = []
})
</script>

<style scoped>
.spot-card {
  width: 100%;
  max-width: 340px;
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
  text-align: center;
}

.spot-name {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 8px;
}

.spot-desc {
  font-size: 15px;
  color: #8c7b6b;
  margin-bottom: 20px;
}

.ai-section {
  width: 100%;
  max-width: 360px;
  margin-bottom: 24px;
}

.ai-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  text-align: center;
}
.history-link {
  display: block;
  width: 100%;
  max-width: 360px;
  text-align: center;
  color: #A68A64;
  text-decoration: none;
  font-size: 15px;
  margin: 12px 0 24px;
  transition: color 0.2s;
}

.history-link:hover {
  color: #8C7B6B;
}

.input-container {
  position: relative;
  margin-bottom: 12px;
}

.ask-input {
  width: 100%;
  padding: 12px 13px 12px 16px;
  border: 1px solid #c4b8a8;
  border-radius: 10px;
  background: #faf8f5;
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  color: #3a3530;
  resize: vertical;
  min-height: 50px;
}

.ask-input:disabled {
  background-color: #f5f5f5;
  opacity: 0.6;
  cursor: not-allowed;
}

.attach-btn {
  position: absolute;
  right: -20px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #a68a64;
  font-size: 20px;
  cursor: pointer;
}

.attach-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.preview-area {
  display: flex;
  gap: 8px;
  margin: 10px 0;
  flex-wrap: wrap;
}

.preview-item {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-preview {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 16px;
  height: 16px;
  background: #c44536;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  cursor: pointer;
  border: none;
}

.remove-preview:disabled {
  cursor: not-allowed;
}

.mic-row {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 8px;
}

.mic-btn,
.send-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 24px;
  border: none;
}

.mic-btn {
  background: #efeae5;
  color: #a68a64;
}

.mic-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn {
  background: #a68a64;
  color: white;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.answer-box {
  margin-top: 16px;
  padding: 14px;
  background: #faf8f5;
  border-radius: 10px;
  font-size: 15px;
  line-height: 1.6;
  min-height: 60px;
}

.typing-indicator {
  margin-top: 10px;
  font-style: italic;
  color: #8c7b6b;
}

.ar-btn {
  width: 100%;
  max-width: 390px;
  background: white;
  color: #a68a64;
  border: 1px solid #a68a64;
  border-radius: 12px;
  padding: 14px;
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 20px;
}

.coming-soon {
  margin-top: 10px;
  color: #c4b8a8;
  font-size: 13px;
  text-align: center;
  line-height: 1.5;
}

.privacy-note {
  font-size: 12px;
  color: #8c7b6b;
  margin-top: 8px;
  text-align: center;
}
</style>