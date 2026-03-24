<template>
  <div class="virtual-character-3d" :class="{ expanded: isExpanded }">
    <div class="character-body" @click="toggleExpand">
      <div ref="avatarContainer" class="character-3d-container"></div>
      <div v-if="!isExpanded" class="speech-bubble mini">
        <span>{{ miniMessages[currentMiniMessage] }}</span>
      </div>
    </div>
    
    <transition name="slide-up">
      <div v-if="isExpanded" class="character-panel">
        <div class="panel-header">
          <div class="header-avatar">
            <div ref="headerAvatarContainer" class="mini-3d"></div>
          </div>
          <span class="character-name font-serif">文迹书生</span>
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
import * as THREE from 'three'

const isExpanded = ref(false)
const isTyping = ref(false)
const inputText = ref('')
const messages = ref([])
const messagesContainer = ref(null)
const currentMiniMessage = ref(0)
const avatarContainer = ref(null)
const headerAvatarContainer = ref(null)

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

let mainScene = null
let mainCamera = null
let mainRenderer = null
let mainCharacter = null
let mainAnimationId = null
let headerScene = null
let headerCamera = null
let headerRenderer = null
let headerCharacter = null
let headerAnimationId = null
let miniMessageInterval = null

const createCharacter = (scene) => {
  const group = new THREE.Group()
  
  const bodyMat = new THREE.MeshPhongMaterial({ 
    color: 0x2D4059,
    shininess: 30
  })
  const faceMat = new THREE.MeshPhongMaterial({ 
    color: 0xF5E6D3,
    shininess: 30
  })
  const hairMat = new THREE.MeshPhongMaterial({ 
    color: 0x1a1a2e,
    shininess: 50
  })
  const eyeMat = new THREE.MeshPhongMaterial({ 
    color: 0x2D4059,
    shininess: 100
  })
  const hatMat = new THREE.MeshPhongMaterial({ 
    color: 0x8B4513,
    shininess: 20
  })
  
  const body = new THREE.Mesh(
    new THREE.CylinderGeometry(0.35, 0.4, 0.8, 32),
    bodyMat
  )
  body.position.y = -0.2
  group.add(body)
  
  const head = new THREE.Mesh(
    new THREE.SphereGeometry(0.35, 32, 32),
    faceMat
  )
  head.position.y = 0.45
  group.add(head)
  
  const hairBase = new THREE.Mesh(
    new THREE.SphereGeometry(0.38, 32, 32, 0, Math.PI * 2, 0, Math.PI / 2),
    hairMat
  )
  hairBase.position.y = 0.5
  group.add(hairBase)
  
  const hairTop = new THREE.Mesh(
    new THREE.CylinderGeometry(0.1, 0.15, 0.3, 16),
    hairMat
  )
  hairTop.position.y = 0.9
  group.add(hairTop)
  
  const hatBrim = new THREE.Mesh(
    new THREE.CylinderGeometry(0.5, 0.5, 0.05, 32),
    hatMat
  )
  hatBrim.position.y = 0.85
  group.add(hatBrim)
  
  const hatTop = new THREE.Mesh(
    new THREE.CylinderGeometry(0.25, 0.35, 0.25, 32),
    hatMat
  )
  hatTop.position.y = 1.0
  group.add(hatTop)
  
  const leftEye = new THREE.Mesh(
    new THREE.SphereGeometry(0.05, 16, 16),
    eyeMat
  )
  leftEye.position.set(-0.12, 0.5, 0.32)
  group.add(leftEye)
  
  const rightEye = new THREE.Mesh(
    new THREE.SphereGeometry(0.05, 16, 16),
    eyeMat
  )
  rightEye.position.set(0.12, 0.5, 0.32)
  group.add(rightEye)
  
  const leftArm = new THREE.Mesh(
    new THREE.CylinderGeometry(0.08, 0.08, 0.5, 16),
    bodyMat
  )
  leftArm.position.set(-0.45, -0.1, 0)
  leftArm.rotation.z = Math.PI / 6
  group.add(leftArm)
  
  const rightArm = new THREE.Mesh(
    new THREE.CylinderGeometry(0.08, 0.08, 0.5, 16),
    bodyMat
  )
  rightArm.position.set(0.45, -0.1, 0)
  rightArm.rotation.z = -Math.PI / 6
  group.add(rightArm)
  
  const leftHand = new THREE.Mesh(
    new THREE.SphereGeometry(0.1, 16, 16),
    faceMat
  )
  leftHand.position.set(-0.55, -0.3, 0)
  group.add(leftHand)
  
  const rightHand = new THREE.Mesh(
    new THREE.SphereGeometry(0.1, 16, 16),
    faceMat
  )
  rightHand.position.set(0.55, -0.3, 0)
  group.add(rightHand)
  
  group.userData = {
    body,
    head,
    leftArm,
    rightArm,
    leftHand,
    rightHand,
    leftEye,
    rightEye
  }
  
  scene.add(group)
  return group
}

const initThreeJS = (container, isMain) => {
  if (!container) return
  
  const width = container.clientWidth
  const height = container.clientHeight
  
  const scene = new THREE.Scene()
  scene.background = null
  
  const camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 100)
  camera.position.set(0, 0.5, 3)
  camera.lookAt(0, 0.3, 0)
  
  const renderer = new THREE.WebGLRenderer({ 
    antialias: true, 
    alpha: true 
  })
  renderer.setSize(width, height)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setClearColor(0x000000, 0)
  container.appendChild(renderer.domElement)
  
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)
  
  const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8)
  directionalLight.position.set(2, 3, 2)
  scene.add(directionalLight)
  
  const backLight = new THREE.DirectionalLight(0xffffff, 0.3)
  backLight.position.set(-2, 1, -2)
  scene.add(backLight)
  
  const character = createCharacter(scene)
  
  if (isMain) {
    mainScene = scene
    mainCamera = camera
    mainRenderer = renderer
    mainCharacter = character
  } else {
    headerScene = scene
    headerCamera = camera
    headerRenderer = renderer
    headerCharacter = character
  }
  
  const animate = () => {
    const animationId = requestAnimationFrame(animate)
    
    if (isMain) {
      mainAnimationId = animationId
    } else {
      headerAnimationId = animationId
    }
    
    const time = Date.now() * 0.001
    
    if (character) {
      character.position.y = Math.sin(time * 2) * 0.03
      character.rotation.y = Math.sin(time * 0.5) * 0.1
      
      if (character.userData.leftArm) {
        character.userData.leftArm.rotation.z = Math.PI / 6 + Math.sin(time * 3) * 0.1
      }
      if (character.userData.rightArm) {
        character.userData.rightArm.rotation.z = -Math.PI / 6 - Math.sin(time * 3) * 0.1
      }
    }
    
    renderer.render(scene, camera)
  }
  
  animate()
}

const cleanupThreeJS = (isMain) => {
  if (isMain) {
    if (mainAnimationId) cancelAnimationFrame(mainAnimationId)
    if (mainRenderer && avatarContainer.value) {
      avatarContainer.value.removeChild(mainRenderer.domElement)
    }
    mainScene = null
    mainCamera = null
    mainRenderer = null
    mainCharacter = null
  } else {
    if (headerAnimationId) cancelAnimationFrame(headerAnimationId)
    if (headerRenderer && headerAvatarContainer.value) {
      headerAvatarContainer.value.removeChild(headerRenderer.domElement)
    }
    headerScene = null
    headerCamera = null
    headerRenderer = null
    headerCharacter = null
  }
}

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
  if (isExpanded.value && messages.value.length === 0) {
    messages.value.push({
      text: '你好！我是文迹书生，有什么可以帮你的吗？',
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
    await scrollToBottom()
  }
}

const handleQuickAction = (action) => {
  inputText.value = action.prompt
  sendMessage()
}

const startMiniMessages = () => {
  miniMessageInterval = setInterval(() => {
    currentMiniMessage.value = (currentMiniMessage.value + 1) % miniMessages.length
  }, 4000)
}

watch(isExpanded, (val) => {
  if (val) {
    clearInterval(miniMessageInterval)
    nextTick(() => {
      if (headerAvatarContainer.value && !headerRenderer) {
        initThreeJS(headerAvatarContainer.value, false)
      }
    })
  } else {
    startMiniMessages()
  }
})

onMounted(() => {
  if (avatarContainer.value) {
    initThreeJS(avatarContainer.value, true)
  }
  startMiniMessages()
})

onUnmounted(() => {
  cleanupThreeJS(true)
  cleanupThreeJS(false)
  clearInterval(miniMessageInterval)
})
</script>

<style scoped>
.virtual-character-3d {
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

.character-3d-container {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #F5F2EB 0%, #E8ECEF 100%);
  box-shadow: 0 4px 20px rgba(45, 64, 89, 0.3);
}

.speech-bubble.mini {
  position: absolute;
  top: -10px;
  right: 75px;
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  color: #F5F2EB;
  padding: 8px 14px;
  border-radius: 12px;
  font-size: 12px;
  white-space: nowrap;
  animation: float 2s ease-in-out infinite;
  box-shadow: 0 2px 10px rgba(45, 64, 89, 0.2);
}

.speech-bubble.mini::after {
  content: '';
  position: absolute;
  right: -6px;
  top: 50%;
  transform: translateY(-50%);
  border: 6px solid transparent;
  border-left-color: #3D5A80;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.character-panel {
  position: absolute;
  bottom: 80px;
  right: 0;
  width: 340px;
  max-height: 480px;
  background: #F5F2EB;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(45, 64, 89, 0.25);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  color: #F5F2EB;
}

.header-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
  background: #F5F2EB;
}

.mini-3d {
  width: 100%;
  height: 100%;
}

.character-name {
  flex: 1;
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #F5F2EB;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.panel-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  max-height: 260px;
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
  padding: 12px 16px;
  border-radius: 18px;
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
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  color: #F5F2EB;
  border-bottom-right-radius: 4px;
}

.message.typing {
  align-self: flex-start;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 14px 18px;
  background: #E8ECEF;
  border-radius: 18px;
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
  padding: 8px 14px;
  background: #E8ECEF;
  border: none;
  border-radius: 18px;
  font-size: 12px;
  color: #2D4059;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  color: #F5F2EB;
}

.panel-input {
  display: flex;
  gap: 10px;
  padding: 14px 16px;
  border-top: 1px solid #E8ECEF;
  background: #FFF;
}

.panel-input input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #E8ECEF;
  border-radius: 24px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.panel-input input:focus {
  border-color: #2D4059;
}

.send-btn {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  border: none;
  border-radius: 50%;
  color: #F5F2EB;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s, box-shadow 0.2s;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(45, 64, 89, 0.3);
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
  .virtual-character-3d {
    right: 16px;
    bottom: 90px;
  }
  
  .character-3d-container {
    width: 60px;
    height: 60px;
  }
  
  .character-panel {
    width: calc(100vw - 32px);
    right: -8px;
    max-height: 420px;
  }
}
</style>
