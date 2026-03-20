<template>
  <Layout title="文迹 - 地图探索" :showBack="false">
    <template #header-actions>
      <!-- 个人详情按钮 -->
      <div class="user-avatar" @click="goToProfile">
        <span class="avatar-icon">📜</span>
      </div>
    </template>
    <div class="map-wrapper">
      <!-- 地图容器 -->
      <div ref="mapRef" class="map-container"></div>

      <!-- 定位按钮 -->
      <button class="location-btn" @click="locateUser" :class="{'locating': isLocating}">
        <span class="location-icon">📍</span>
      </button>
      
      <!-- AR体验按钮 -->
      <div class="ar-enter-container">
        <button class="ar-enter-btn" @click="goToAR">
          <span class="ar-icon">🔮</span>
          <span class="ar-text">进入AR体验</span>
        </button>
      </div>
    <!-- 底部信息弹窗 -->
    <transition name="slide-up">
      <div v-if="showInfoPanel" class="info-panel-overlay" @click.self="closeInfoPanel">
        <div class="info-panel">
          <div class="info-header">
            <h3>{{ infoData.name }}</h3>
            <button class="close-btn" @click="closeInfoPanel">×</button>
          </div>
          <div class="info-content">
          <div class="info-body">
            <div class="info-left">
              <img
                v-if="infoData.name"
                :src="getImageUrl(infoData.name)"
                :alt="infoData.name"
                class="site-image"
                @error="handleImageError"
              />
            </div>
            <div class="info-middle">
              <div v-if="infoData.type" class="info-item">
                <span class="label">类型:</span>
                <span class="value">{{ infoData.type }}</span>
              </div>
              <div v-if="infoData.province" class="info-item">
                <span class="label">省份:</span>
                <span class="value">{{ infoData.province }}</span>
              </div>
              <div v-if="infoData.category" class="info-item">
                <span class="label">类别:</span>
                <span class="value">{{ infoData.category }}</span>
              </div>
              <div v-if="infoData.heat_level !== undefined" class="info-item">
                <span class="label">热度:</span>
                <span class="value">{{ (infoData.heat_level * 100).toFixed(0) }}%</span>
              </div>
              <div v-if="infoData.status" class="info-item">
                <span class="label">状态:</span>
                <span class="value" :class="{ 'status-unlocked': infoData.status === 'unlocked', 'status-locked': infoData.status === 'locked' }">
                  {{ infoData.status === 'unlocked' ? '已解锁' : '未解锁' }}
                </span>
              </div>
              <div v-if="infoData.description" class="info-item description">
                <span class="label">介绍:</span>
                <p class="value">{{ infoData.description }}</p>
              </div>
            </div>
            <div class="info-right">
              <div class="ai-section">
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
                <div class="mic-row">
                  <button class="mic-btn" @click="useVoice" :disabled="isResponding">🎤</button>
                  <button class="send-btn" @click="submitQuestion" :disabled="isResponding || !canSubmit">
                    {{ isResponding ? '⏳' : '➤' }}
                  </button>
                </div>
                <div v-if="answer || isResponding" class="answer-box">
                  <div class="answer-content">{{ answer }}</div>
                  <div v-if="isResponding" class="typing-indicator">文迹正在思考中...</div>
                </div>
                <router-link to="/user/history" class="history-link">
                  📜 查看历史问答
                </router-link>
              </div>
            </div>
          </div>
          </div>
        </div>
      </div>
    </transition>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import Layout from '@/components/Layout.vue'
// 引入非遗景点数据和配置
import { CHINA_NON_HERITAGE_SITES, MAP_CONFIG, PROVINCE_DATA } from '@/assets/data/heritage-sites.js'
// 引入SSEHandler
import { SSEHandler } from '@/utils/sseHandler.js'

const router = useRouter()

// 定义地图容器的引用
const mapRef = ref(null)
let myChart = null

// 弹窗状态
const showInfoPanel = ref(false)
const infoData = ref({})

// 用户定位相关状态
const userLocation = ref(null)
const isLocating = ref(false)

// 图片格式状态
const imageFormat = ref({})

// AI问答相关状态
const questionText = ref('')
const fileInputRef = ref(null)
const uploadedFiles = ref([])
const previewUrls = ref([])
const answer = ref('')
const isResponding = ref(false)
const currentChatId = ref(null)

// 计算属性：判断是否可以提交
const canSubmit = computed(() => {
  return (questionText.value.trim() || uploadedFiles.value.length > 0) && !isResponding.value
})

// 创建新的聊天会话
function createNewChatSession() {
  const chatId = `chat_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  currentChatId.value = chatId;
  return chatId;
}

// 触发文件选择
function triggerFileInput() {
  if (!isResponding.value) {
    fileInputRef.value?.click()
  }
}

// 处理文件选择
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

// 移除预览
function removePreview(index) {
  if (isResponding.value) return

  // 释放 URL
  URL.revokeObjectURL(previewUrls.value[index])
  // 移除
  previewUrls.value.splice(index, 1)
  uploadedFiles.value.splice(index, 1)
}

// 提交问题
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
    formData.append('chatId', currentChatId.value)

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

// 使用语音
function useVoice() {
  if (isResponding.value) return

  const input = prompt('模拟语音输入：', '这块瓷片是什么年代的？')
  if (input) {
    questionText.value = input
    submitQuestion()
  }
}

// 获取景点图片URL
const getImageUrl = (siteName) => {
  if (!siteName) return ''
  // 检查是否已经尝试过jpg格式
  const format = imageFormat.value[siteName] || 'gif'
  const path = new URL(`/src/assets/data/景点照片/${siteName}.${format}`, import.meta.url).href
  return path
}

// 处理图片加载错误
const handleImageError = (event) => {
  const siteName = infoData.value.name
  // 如果当前是gif格式，尝试切换到jpg格式
  if (imageFormat.value[siteName] !== 'jpg') {
    imageFormat.value[siteName] = 'jpg'
    // 更新图片src
    event.target.src = getImageUrl(siteName)
  }
}

// 关闭弹窗
const closeInfoPanel = () => {
  showInfoPanel.value = false
  infoData.value = {}
}

// 跳转到个人资料页
function goToProfile() {
  console.log('跳转到个人资料页');
  router.push('/user/profile');
}

// 跳转到AR体验页面
function goToAR() {
  console.log('跳转到AR体验页面');
  router.push('/user/ceramicAr');
}

// 获取用户位置
function locateUser() {
  if (!navigator.geolocation) {
    alert('您的浏览器不支持地理定位功能');
    return;
  }

  isLocating.value = true;

  navigator.geolocation.getCurrentPosition(
    (position) => {
      const { latitude, longitude } = position.coords;
      userLocation.value = {
        lat: latitude,
        lng: longitude
      };
      
      // 在地图上显示用户位置
      updateUserLocationOnMap(latitude, longitude);
      isLocating.value = false;
    },
    (error) => {
      console.error('获取位置失败:', error);
      let errorMsg = '无法获取您的位置';
      switch(error.code) {
        case error.PERMISSION_DENIED:
          errorMsg = '您拒绝了位置权限请求';
          break;
        case error.POSITION_UNAVAILABLE:
          errorMsg = '位置信息不可用';
          break;
        case error.TIMEOUT:
          errorMsg = '获取位置超时';
          break;
      }
      alert(errorMsg);
      isLocating.value = false;
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    }
  );
}

// 在地图上更新用户位置
function updateUserLocationOnMap(lat, lng) {
  if (!myChart) return;

  // 添加用户位置标记
  const option = myChart.getOption();
  
  // 检查是否已经有用户位置系列，如果有则更新，没有则添加
  const userLocationSeries = {
    name: '我的位置',
    type: 'scatter',
    coordinateSystem: 'geo',
    data: [{
      name: '我的位置',
      value: [lng, lat, 100], // 第三个值用于控制点的大小
      itemStyle: {
        color: '#ff4d4f'
      }
    }],
    symbolSize: 15,
    label: {
      show: true,
      formatter: '我的位置',
      position: 'top',
      color: '#ff4d4f',
      fontSize: 12,
      fontWeight: 'bold'
    },
    zlevel: 2, // 确保显示在其他点之上
    emphasis: {
      label: {
        show: true
      }
    }
  };

  // 更新图表选项
  myChart.setOption({
    series: [...option.series, userLocationSeries]
  });

  // 移动地图中心到用户位置
  myChart.setOption({
    geo: {
      center: [lng, lat],
      zoom: 5
    }
  });
}

// 初始化地图函数
const initMap = async () => {
  if (!mapRef.value) return

  // 初始化 echarts 实例
  myChart = echarts.init(mapRef.value)

  try {
    // 1. 获取中国地图 GeoJSON 数据
    const response = await fetch('https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json')
    if (!response.ok) {
      throw new Error('网络请求异常，无法加载地图数据')
    }
    const chinaJson = await response.json()

    // 2. 注册地图
    echarts.registerMap('china', chinaJson)

    // 3. 转换数据格式以适配 ECharts
    // ECharts scatter 系列的 data 需要 [lng, lat, value, ...other] 格式
    // 我们将 CHINA_NON_HERITAGE_SITES 转换为需要的格式，并保留原始对象信息以便 tooltip 使用
    const scatterData = CHINA_NON_HERITAGE_SITES.map(site => {
      return {
        name: site.name,
        value: [site.lng, site.lat, site.heat_level * 100], // 第三个值用于控制气泡大小或视觉映射
        // 将原始数据挂载到 item 上，方便后续点击或 tooltip 调用
        raw: site 
      }
    })

    // 4. 配置图表选项
    const option = {
      backgroundColor: '#f5f5f5',
      
      title: {
        text: '中国非遗景点分布',
        left: 'center',
        top: 20,
        textStyle: {
          color: '#333'
        }
      },

      geo: {
        map: 'china',
        roam: true, 
        zoom: 1.2,
        label: {
          show: true, 
          color: '#333',
          fontSize: 10
        },
        itemStyle: {
          borderColor: '#999999',
          borderWidth: 1
        }
      },

      series: [
        {
          name: '非遗景点',
          type: 'effectScatter',
          coordinateSystem: 'geo',
          data: scatterData, // 使用转换后的数据
          symbolSize: function (val) {
            // 根据热度值动态调整气泡大小 (val[2] 是热度值)
            return val[2] / 5; 
          },
          showEffectOn: 'render',
          rippleEffect: {
            brushType: 'stroke',
            scale: 3
          },
          label: {
            formatter: '{b}',
            position: 'right',
            show: true,
            fontSize: 10,
            color: '#333'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowColor: '#333'
          },
          zlevel: 1
        }
      ]
    }

    // 5. 设置配置项
    myChart.setOption(option)

    // 6. 监听点击事件
    myChart.on('click', function (params) {
      if (params.componentType === 'series') {
        // 点击景点
        const siteData = params.data.raw
        infoData.value = {
          ...siteData,
          description: PROVINCE_DATA.find(p => p.name === siteData.province)?.description || '暂无介绍'
        }
        showInfoPanel.value = true
      }
    })

  } catch (error) {
    console.error('地图初始化失败:', error)
    if (mapRef.value) {
      mapRef.value.innerHTML = `<div style="text-align:center; padding-top:200px; color:red;">地图加载失败，请检查网络连接。<br>${error.message}</div>`
    }
  }
}

const handleResize = () => {
  if (myChart) {
    myChart.resize()
  }
}

onMounted(() => {
  initMap()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  // 释放所有 object URLs
  previewUrls.value.forEach(url => URL.revokeObjectURL(url))
  previewUrls.value = []

  if (myChart) {
    myChart.dispose()
    myChart = null
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.map-wrapper {
  width: 100%;
  min-height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #E8E4D9 0%, #D4CFC3 50%, #C8D8C8 100%);
  font-family: "Noto Serif SC", serif;
  color: #3A3530;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 添加流动的云雾效果 */
.map-wrapper::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(107, 122, 143, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(139, 115, 85, 0.06) 0%, transparent 50%);
  animation: float 20s ease-in-out infinite;
  pointer-events: none;
  z-index: 0;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.8;
  }
  50% {
    transform: translateY(-20px) scale(1.1);
    opacity: 1;
  }
}

.map-container {
  width: 100%;
  height: calc(100vh - 150px);
  flex: 1;
  background: linear-gradient(135deg, rgba(232, 228, 217, 0.95) 0%, rgba(212, 207, 195, 0.95) 100%);
  border-radius: 12px;
  border: 2px solid rgba(139, 115, 85, 0.3);
  box-shadow: 
    0 4px 16px rgba(107, 122, 143, 0.2),
    inset 0 2px 8px rgba(139, 115, 85, 0.1),
    0 0 40px rgba(107, 122, 143, 0.1);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* 添加流动的光影效果 */
.map-container::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: 
    radial-gradient(circle at 30% 40%, rgba(159, 179, 200, 0.08) 0%, transparent 40%),
    radial-gradient(circle at 70% 60%, rgba(139, 115, 85, 0.06) 0%, transparent 40%);
  animation: lightMove 15s ease-in-out infinite;
  pointer-events: none;
  z-index: 0;
}

@keyframes lightMove {
  0% {
    transform: translate(0, 0);
  }
  33% {
    transform: translate(30%, 20%);
  }
  66% {
    transform: translate(-20%, 30%);
  }
  100% {
    transform: translate(0, 0);
  }
}

/* 个人详情按钮 */
.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #A8B5C0 0%, #7D8FA3 100%);
  border: 2px solid #D4C5B0;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(125, 143, 163, 0.4);
}

.avatar-icon {
  font-size: 20px;
}

.user-avatar:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(125, 143, 163, 0.6);
}

/* AR体验按钮容器 */
.ar-enter-container {
  display: flex;
  justify-content: center;
  margin: 20px 0;
}

/* AR体验按钮 */
.ar-enter-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: linear-gradient(135deg, #9FB3C8 0%, #6B7A8F 100%);
  color: #fff;
  border: 2px solid #D4C5B0;
  border-radius: 50px;
  padding: 15px 30px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(107, 122, 143, 0.4);
  transition: all 0.3s ease;
}

.ar-enter-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(107, 122, 143, 0.6);
}

.ar-enter-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 10px rgba(107, 122, 143, 0.4);
}

.ar-icon {
  font-size: 22px;
}

/* 定位按钮 */
.location-btn {
  position: absolute;
  bottom: 20px;
  right: 20px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #9FB3C8 0%, #6B7A8F 100%);
  border: 2px solid #D4C5B0;
  box-shadow: 0 2px 8px rgba(107, 122, 143, 0.4);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10;
}

.location-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(107, 122, 143, 0.6);
}

.location-btn.locating {
  animation: pulse 1.5s infinite;
}

.location-icon {
  font-size: 24px;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 77, 79, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(255, 77, 79, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(255, 77, 79, 0);
  }
}

/* 底部信息弹窗遮罩层 */
.info-panel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

/* 底部信息弹窗 */
.info-panel {
  background: linear-gradient(180deg, rgba(232, 228, 217, 0.98) 0%, rgba(200, 195, 183, 0.98) 100%);
  border: 2px solid rgba(139, 115, 85, 0.4);
  border-radius: 12px 12px 0 0;
  max-height: 50vh;
  overflow-y: auto;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  box-shadow: 
    0 -4px 20px rgba(107, 122, 143, 0.3),
    0 0 30px rgba(139, 115, 85, 0.1);
  position: relative;
  z-index: 2;
}

/* 添加流动的光晕效果 */
.info-panel::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 80% 20%, rgba(159, 179, 200, 0.12) 0%, transparent 50%),
    radial-gradient(circle at 20% 80%, rgba(139, 115, 85, 0.08) 0%, transparent 50%);
  animation: glow 8s ease-in-out infinite;
  pointer-events: none;
  z-index: -1;
}

@keyframes glow {
  0%, 100% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
}

.info-content {
  padding: 20px;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 2px solid rgba(139, 115, 85, 0.3);
  background: linear-gradient(90deg, rgba(159, 179, 200, 0.15) 0%, transparent 50%);
  position: relative;
}

/* 添加流动的边框光效 */
.info-header::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(159, 179, 200, 0.6) 50%, 
    transparent 100%);
  animation: borderGlow 3s ease-in-out infinite;
}

@keyframes borderGlow {
  0%, 100% {
    transform: translateX(-100%);
  }
  50% {
    transform: translateX(100%);
  }
}

.info-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #3A3530;
  font-family: "Noto Serif SC", serif;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #8C7B6B;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  line-height: 1;
  transition: color 0.3s;
}

.close-btn:hover {
  color: #3A3530;
}

.info-body {
  display: flex;
  flex-direction: row;
  gap: 20px;
}

.info-left {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-middle {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-right {
  flex: 0 0 300px;
  display: flex;
  flex-direction: column;
}

.site-image {
  max-width: 200px;
  max-height: 200px;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(107, 122, 143, 0.3);
  border: 2px solid #D4C5B0;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.info-item .label {
  min-width: 60px;
  color: #6B7A8F;
  font-size: 14px;
  font-weight: 500;
}

.info-item .value {
  color: #3A3530;
  font-size: 14px;
  flex: 1;
}

.info-item.description {
  flex-direction: column;
  gap: 6px;
}

.info-item.description .label {
  min-width: auto;
}

.info-item.description .value {
  line-height: 1.6;
  margin: 0;
}

.status-unlocked {
  color: #52c41a;
}

.status-locked {
  color: #f5222d;
}

/* AI问答区域样式 */
.ai-section {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ai-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  text-align: center;
  color: #3A3530;
}

.input-container {
  position: relative;
}

.ask-input {
  width: 100%;
  padding: 12px 13px 12px 16px;
  border: 2px solid #6B7A8F;
  border-radius: 10px;
  background: #F9F7F2;
  font-family: 'Noto Serif SC', serif;
  font-size: 14px;
  color: #3a3530;
  resize: vertical;
  min-height: 50px;
  box-shadow: inset 0 1px 3px rgba(107, 122, 143, 0.1);
}

.ask-input:disabled {
  background-color: #f5f5f5;
  opacity: 0.6;
  cursor: not-allowed;
}

.attach-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #a68a64;
  font-size: 18px;
  cursor: pointer;
}

.attach-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.preview-area {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.preview-item {
  width: 50px;
  height: 50px;
  border-radius: 6px;
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
  top: -5px;
  right: -5px;
  width: 14px;
  height: 14px;
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
  gap: 12px;
}

.mic-btn,
.send-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 20px;
  border: none;
}

.mic-btn {
  background: linear-gradient(135deg, #9FB3C8 0%, #6B7A8F 100%);
  color: #fff;
  border: 2px solid #D4C5B0;
}

.mic-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn {
  background: linear-gradient(135deg, #9FB3C8 0%, #6B7A8F 100%);
  color: #fff;
  border: 2px solid #D4C5B0;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.answer-box {
  margin-top: 12px;
  padding: 12px;
  background: linear-gradient(180deg, rgba(232, 228, 217, 0.95) 0%, rgba(200, 195, 183, 0.95) 100%);
  border: 2px solid rgba(139, 115, 85, 0.3);
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.6;
  min-height: 50px;
  box-shadow: 
    inset 0 1px 3px rgba(139, 115, 85, 0.1),
    0 0 20px rgba(159, 179, 200, 0.1);
  position: relative;
  overflow: hidden;
}

/* 添加流动的背景光效 */
.answer-box::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: 
    radial-gradient(circle at 50% 50%, rgba(159, 179, 200, 0.08) 0%, transparent 60%);
  animation: answerGlow 6s ease-in-out infinite;
  pointer-events: none;
}

@keyframes answerGlow {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(10%, -10%) scale(1.05);
  }
}

.typing-indicator {
  margin-top: 8px;
  font-style: italic;
  color: #8c7b6b;
  font-size: 12px;
}

.history-link {
  display: block;
  text-align: center;
  color: #A68A64;
  text-decoration: none;
  font-size: 14px;
  margin-top: 8px;
  transition: color 0.2s;
}

.history-link:hover {
  color: #8C7B6B;
}

/* 弹窗动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
}

/* 模型展示区 */
.model-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #C4B8A8;
}

.model-title {
  font-size: 16px;
  font-weight: 600;
  color: #3A3530;
  margin-bottom: 12px;
}

.model-viewer-wrapper {
  width: 100%;
  height: 200px;
  position: relative;
  min-height: 200px;
  z-index: 1;
}

.model-viewer-container {
  width: 100%;
  height: 100%;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e5e2dd;
  box-sizing: border-box;
}

.model-description {
  margin-top: 12px;
  padding: 10px;
  background-color: #f9f7f2;
  border-radius: 4px;
  border-left: 3px solid #A68A64;
}

.model-description p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #5a524a;
}

/* 竖屏优化 */
@media screen and (orientation: portrait) {
  .map-wrapper {
    padding: 10px;
    min-height: calc(100vh - 80px);
  }

  .map-container {
    height: calc(100vh - 120px);
  }
  .info-panel {
    max-height: 60vh;
  }
  
  .info-content {
    padding: 16px;
  }
  
  .info-header h3 {
    font-size: 16px;
  }
  
  .info-item .label,
  .info-item .value {
    font-size: 13px;
  }

  .model-viewer-wrapper {
    height: 180px;
  }

  .info-body {
    flex-direction: column;
    gap: 16px;
  }

  .info-left {
    width: 100%;
  }

  .info-middle {
    width: 100%;
  }

  .info-right {
    width: 100%;
    flex: none;
  }

  .site-image {
    max-width: 150px;
    max-height: 150px;
  }

  .ai-section {
    gap: 10px;
  }

  .ask-input {
    font-size: 13px;
  }

  .mic-btn,
  .send-btn {
    width: 44px;
    height: 44px;
  }
}
</style>
