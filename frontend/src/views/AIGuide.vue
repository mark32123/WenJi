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

      <!-- 会话信息 -->
      <div v-if="currentChatId" class="session-info">
        <span class="continue-message">继续之前的对话</span>
        <span class="session-time">{{ formatDate(sessionStartTime || new Date()) }}</span>
      </div>

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
        <div v-if="!isResponding" class="answer-content">{{ answer }}</div>
        <div v-if="isResponding" class="typing-indicator">文迹正在思考中...</div>
      </div>
    </section>

    <!-- 功能预告 -->
    <p class="coming-soon">💡 自动语音讲解、路线推荐等功能开发中…</p>
  </Layout>
</template>

<script setup>
import { ref, computed, onBeforeUnmount, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { chatService } from '@/api/user' // 使用项目中定义的 API 服务
import Layout from '@/components/Layout.vue'
// ====== 响应式状态 ======
const questionText = ref('')
const fileInputRef = ref(null)
const uploadedFiles = ref([])
const previewUrls = ref([])
const answer = ref('')
const isResponding = ref(false)
const currentChatId = ref(null) // 添加这一行

const route = useRoute()

// 格式化日期函数
function formatDate(date) {
  if (!date) return '未知时间'
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 组件挂载时检查URL参数
const sessionStartTime = ref(null)
onMounted(() => {
  const chatIdFromUrl = route.query.chatId
  const startTimeFromUrl = route.query.startTime
  if (chatIdFromUrl) {
    console.log('从URL获取到会话ID:', chatIdFromUrl)
    console.log('从URL获取到开始时间:', startTimeFromUrl)
    currentChatId.value = chatIdFromUrl
    sessionStartTime.value = startTimeFromUrl
  } else {
    // 如果没有URL参数，创建新会话
    createNewChatSession()
  }
})

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

  // 确保有会话ID
  if (!currentChatId.value) {
    createNewChatSession();
  }

  console.log('提交问题，使用会话ID:', currentChatId.value)

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

    // 使用 fetch API 来处理流式响应
    const token = localStorage.getItem('token');
    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      body: formData
    });
    
    if (response.ok) {
      // 检查是否是流式响应
      if (response.body) {
        // 使用流式读取
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let accumulatedText = '';
            
        console.log('开始流式读取');
        
        while (true) {
          const { done, value } = await reader.read();
                  
          if (done) {
            console.log('流式读取完成');
            break;
          }
                  
          // 解码流式数据
          const chunk = decoder.decode(value, { stream: true });
          console.log('收到原始流式数据:', chunk);
          
          // 处理SSE格式：提取data字段的内容
          const lines = chunk.split('\n');
          for (const line of lines) {
            if (line.startsWith('data:')) {
              const data = line.substring(5).trim();
              if (data) {
                accumulatedText += data;
                // 实时更新回答
                answer.value = accumulatedText;
                console.log('处理后的流式数据:', data);
                console.log('当前累计文本:', accumulatedText);
              }
            }
          }
        }
                
        console.log('AI 回答完成:', accumulatedText);
      } else {
        // 非流式响应
        const responseText = await response.text();
        answer.value = responseText;
        console.log('非流式响应:', responseText);
      }
    } else {
      // 处理错误响应
      const errorResult = await response.text();
      console.error('请求失败:', errorResult)
      try {
        const errorObj = JSON.parse(errorResult);
        answer.value = `请求失败：${errorObj.msg || '服务器内部错误'}`;
      } catch {
        answer.value = '请求失败：服务器内部错误';
      }
    }
    
  } catch (error) {
    console.error('AI 提问失败:', error)
    if (error.message.includes('Network')) {
      answer.value = '网络连接失败，请检查网络后重试';
    } else if (error.message.includes('401')) {
      answer.value = '登录已过期，请重新登录';
    } else {
      answer.value = '请求发送失败，请稍后重试';
    }
  } finally {
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

.session-info {
  background-color: #f8f5f0;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
  border-left: 4px solid #a68a64;
}

.session-id-tag {
  font-size: 12px;
  color: #8c7b6b;
  font-weight: 500;
}

.continue-message {
  font-size: 14px;
  color: #a68a64;
  font-weight: 600;
}

.session-time {
  font-size: 12px;
  color: #8c7b6b;
  margin-left: 12px;
  font-weight: 500;
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