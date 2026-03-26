<!--
  CityMap.vue - 地图组件
  
  功能说明：
  1. 使用高德地图 API 展示文化遗产景点
  2. 支持城市切换（清远、北京）
  3. 用户定位功能
  4. 景点标记点击显示详情卡片
  5. 导航功能（跳转高德地图导航）
  6. 彩蛋系统（靠近景点时触发奖励）
  
  主要交互：
  - 点击城市按钮切换城市
  - 点击地图标记显示景点详情
  - 点击"我的位置"定位用户位置
  - 点击"探索文物"触发父组件的 explore 事件
  - 点击"导航"跳转高德地图进行导航
  
  依赖：
  - 高德地图 JS API (@amap/amap-jsapi-loader)
  - 后端 API (heritageApi.getNearbySites)
-->
<template>
  <div class="city-map">
    <div class="city-selector">
      <button 
        v-for="city in cities" 
        :key="city.id"
        class="city-btn"
        :class="{ active: currentCity?.id === city.id }"
        @click="switchCity(city)"
      >
        {{ city.name }}
      </button>
    </div>
    
    <div class="map-wrapper" ref="mapContainer"></div>
    
    <div class="map-controls">
      <button class="control-btn" @click="locateUser" :class="{ active: isLocating }">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="3"/>
          <path d="M12 2v4m0 12v4M2 12h4m12 0h4"/>
        </svg>
        <span>{{ isLocating ? '定位中...' : '我的位置' }}</span>
      </button>
    </div>
    
    <transition name="fade-slide">
      <div v-if="showCityInfo" class="city-info-card">
        <h2 class="city-title font-serif">{{ currentCity?.name }}</h2>
        <p class="city-desc">{{ currentCity?.description }}</p>
      </div>
    </transition>
    
    <transition name="slide-up">
      <div v-if="selectedSite" class="site-card">
        <button class="close-btn" @click="selectedSite = null">×</button>
        <div 
          class="site-image" 
          :style="selectedSite.coverImage ? { backgroundImage: `url(${selectedSite.coverImage})` } : {}"
          :class="{ 'no-image': !selectedSite.coverImage }"
        >
          <div v-if="!selectedSite.coverImage" class="image-placeholder">🏛️</div>
          <div class="site-type-tag">{{ selectedSite.category || '文化遗址' }}</div>
        </div>
        <div class="site-content">
          <h3 class="site-name font-serif">{{ selectedSite.name }}</h3>
          <p class="site-address">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
            {{ selectedSite.address }}
          </p>
          <p class="site-desc">{{ selectedSite.description }}</p>
          
          <div class="site-tags">
            <span class="tag">{{ selectedSite.level }}</span>
            <span v-if="selectedSite.distance" class="tag">{{ selectedSite.distance.toFixed(1) }}km</span>
          </div>
          
          <div class="site-actions">
            <button class="action-btn primary" @click="handleExplore(selectedSite)">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polygon points="10 8 16 12 10 16 10 8"/>
              </svg>
              探索文物
            </button>
            <button class="action-btn" @click="handleNavigate(selectedSite)">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="3 11 22 2 13 21 11 13 3 11"/>
              </svg>
              导航
            </button>
          </div>
        </div>
      </div>
    </transition>
    
    <transition name="fade">
      <div v-if="showEasterEgg" class="easter-egg-modal" @click="showEasterEgg = false">
        <div class="easter-egg-content" @click.stop>
          <div class="easter-egg-icon">🎉</div>
          <h3 class="easter-egg-title font-serif">发现彩蛋！</h3>
          <p class="easter-egg-text">{{ easterEggMessage }}</p>
          <div class="easter-egg-reward">
            <span class="reward-label">获得奖励</span>
            <span class="reward-value">+{{ easterEggReward }} 阅历</span>
          </div>
          <button class="easter-egg-btn" @click="showEasterEgg = false">太棒了！</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
/**
 * CityMap - 地图组件
 * 
 * 核心功能：
 * 1. 高德地图初始化与管理
 * 2. 景点标记渲染
 * 3. 用户定位
 * 4. 城市切换
 * 5. 彩蛋系统
 */

import { ref, onMounted, onUnmounted } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { heritageApi } from '@/api'

// ==================== 事件定义 ====================
const emit = defineEmits(['site-select', 'explore', 'easter-egg-found'])

// ==================== 响应式状态 ====================
const mapContainer = ref(null)     // 地图容器 DOM 引用
const selectedSite = ref(null)     // 当前选中的景点
const currentCity = ref(null)      // 当前城市
const isLocating = ref(false)      // 是否正在定位
const showEasterEgg = ref(false)   // 是否显示彩蛋弹窗
const showCityInfo = ref(false)    // 是否显示城市信息卡片
const easterEggMessage = ref('')   // 彩蛋消息
const easterEggReward = ref(0)     // 彩蛋奖励阅历值
const sites = ref([])              // 景点列表

// ==================== 地图相关变量 ====================
let map = null           // 高德地图实例
let AMap = null          // 高德地图 API 对象
let markers = []         // 景点标记数组
let userMarker = null    // 用户位置标记
let cityInfoTimer = null // 城市信息显示定时器

// 高德地图 API Key
const AMAP_KEY = '774b31a8e76ec13cb1e73c4686e5de0e'

/**
 * 支持的城市列表
 * 每个城市包含：id、名称、描述、中心坐标、缩放级别、城市编码
 */
const cities = [
  {
    id: 'qingyuan',
    name: '清远',
    description: '山水清远，岭南明珠',
    center: [113.0562, 23.6817],
    zoom: 12,
    cityCode: '441800'
  },
  {
    id: 'beijing',
    name: '北京',
    description: '六朝古都，文化名城',
    center: [116.4074, 39.9042],
    zoom: 12,
    cityCode: '110100'
  }
]

/** 彩蛋消息列表 */
const easterEggMessages = [
  '你发现了一个隐藏的历史故事！',
  '恭喜你解锁了这片土地的秘密！',
  '古老的传说在此刻被你唤醒！',
  '你找到了时光留下的印记！',
  '历史的长河中，你拾起了一颗明珠！'
]

/**
 * 根据景点分类获取标记颜色
 * @param {string} category - 景点分类
 * @returns {string} 颜色值
 */
const getMarkerColor = (category) => {
  const colors = {
    '博物馆': '#2D4059',
    '古建筑': '#8B4513',
    '自然景观': '#4A7C59',
    '皇家园林': '#B8860B'
  }
  return colors[category] || '#2D4059'
}

/**
 * 从后端获取附近景点数据
 * @param {number} lng - 经度
 * @param {number} lat - 纬度
 * @returns {Array} 景点列表
 */
const fetchSites = async (lng, lat) => {
  try {
    const result = await heritageApi.getNearbySites(lng, lat)
    
    if (result.success && result.data && result.data.length > 0) {
      sites.value = result.data
      return result.data
    }
  } catch (error) {
    console.error('获取景点失败')
  }
  return []
}

/**
 * 初始化高德地图
 * 加载地图 API，创建地图实例，添加控件和标记
 */
const initMap = async () => {
  try {
    // 配置高德地图安全密钥
    window._AMapSecurityConfig = {
      securityJsCode: '5b40f7c1679c23ec1d86c9c7be001ff9',
    }
    
    AMap = await AMapLoader.load({
      key: AMAP_KEY,
      version: '2.0',
      plugins: ['AMap.Scale', 'AMap.ToolBar', 'AMap.Geolocation', 'AMap.Marker']
    })
    
    // 设置默认城市
    currentCity.value = cities[0]
    
    // 创建地图实例
    map = new AMap.Map(mapContainer.value, {
      zoom: currentCity.value.zoom,
      center: currentCity.value.center,
      mapStyle: 'amap://styles/whitesmoke',
      viewMode: '2D'
    })
    
    // 添加比例尺控件
    map.addControl(new AMap.Scale())
    
    // 获取并添加景点标记
    const siteData = await fetchSites(currentCity.value.center[0], currentCity.value.center[1])
    if (siteData.length > 0) {
      addMarkers(siteData)
    }
    
    // 显示城市信息卡片，3秒后自动隐藏
    showCityInfo.value = true
    cityInfoTimer = setTimeout(() => {
      showCityInfo.value = false
    }, 3000)
  } catch (error) {
    console.error('地图加载失败')
  }
}

/**
 * 在地图上添加景点标记
 * @param {Array} siteList - 景点列表
 */
const addMarkers = (siteList) => {
  // 清除现有标记
  markers.forEach(marker => map.remove(marker))
  markers = []
  
  if (!siteList || siteList.length === 0) return
  
  siteList.forEach(site => {
    const lng = Number(site.longitude)
    const lat = Number(site.latitude)
    
    // 跳过无效坐标
    if (isNaN(lng) || isNaN(lat)) {
      return
    }
    
    const color = getMarkerColor(site.category)
    
    // 创建自定义标记 HTML
    const markerContent = `
      <div class="custom-marker" style="
        position: relative;
        width: 32px;
        height: 40px;
      ">
        <div style="
          position: absolute;
          width: 32px;
          height: 32px;
          background: ${color};
          border-radius: 50% 50% 50% 0;
          transform: rotate(-45deg);
          box-shadow: 0 2px 6px rgba(0,0,0,0.3);
          border: 2px solid rgba(255,255,255,0.9);
        "></div>
        <div style="
          position: absolute;
          top: 8px;
          left: 8px;
          width: 16px;
          height: 16px;
          background: rgba(255,255,255,0.9);
          border-radius: 50%;
        "></div>
      </div>
    `
    
    // 创建标记并添加点击事件
    const marker = new AMap.Marker({
      position: [lng, lat],
      content: markerContent,
      offset: new AMap.Pixel(-16, -40)
    })
    
    marker.on('click', () => {
      selectedSite.value = site
    })
    
    map.add(marker)
    markers.push(marker)
  })
}

/**
 * 切换城市
 * @param {Object} city - 城市配置对象
 */
const switchCity = async (city) => {
  if (!map) return
  
  currentCity.value = city
  selectedSite.value = null
  map.setZoomAndCenter(city.zoom, city.center)
  
  // 获取新城市的景点数据
  const siteData = await fetchSites(city.center[0], city.center[1])
  addMarkers(siteData)
  
  // 显示城市信息卡片
  showCityInfo.value = true
  if (cityInfoTimer) clearTimeout(cityInfoTimer)
  cityInfoTimer = setTimeout(() => {
    showCityInfo.value = false
  }, 3000)
}

/**
 * 定位用户位置
 * 优先使用浏览器定位，失败则使用高德定位，最后使用模拟定位
 */
const locateUser = () => {
  isLocating.value = true
  
  // 尝试浏览器原生定位
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { longitude: lng, latitude: lat } = position.coords
        showUserLocation(lng, lat)
        isLocating.value = false
      },
      (error) => {
        // 浏览器定位失败，尝试高德定位
        aMapLocate()
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    )
  } else {
    // 不支持浏览器定位，使用高德定位
    aMapLocate()
  }
}

/**
 * 使用高德地图定位
 * 当浏览器定位不可用时使用
 */
const aMapLocate = () => {
  if (!AMap) {
    // 高德地图未加载，使用模拟定位
    simulateLocation()
    return
  }
  
  AMap.plugin('AMap.Geolocation', () => {
    const geolocation = new AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    })
    
    geolocation.getCurrentPosition((status, result) => {
      isLocating.value = false
      
      if (status === 'complete') {
        const { lng, lat } = result.position
        showUserLocation(lng, lat)
      } else {
        // 高德定位失败，使用模拟定位
        simulateLocation()
      }
    })
  })
}

/**
 * 模拟定位
 * 当所有定位方式都失败时，使用当前城市中心位置加随机偏移
 */
const simulateLocation = () => {
  isLocating.value = false
  const city = currentCity.value
  const randomOffset = () => (Math.random() - 0.5) * 0.02
  const simLng = city.center[0] + randomOffset()
  const simLat = city.center[1] + randomOffset()
  showUserLocation(simLng, simLat)
  
  // 显示提示彩蛋
  easterEggMessage.value = '已为您定位到' + city.name + '市中心区域'
  easterEggReward.value = 5
  showEasterEgg.value = true
}

/**
 * 在地图上显示用户位置
 * @param {number} lng - 经度
 * @param {number} lat - 纬度
 */
const showUserLocation = (lng, lat) => {
  // 移除旧的用户标记
  if (userMarker) {
    map.remove(userMarker)
  }
  
  // 创建用户位置标记（带脉冲动画）
  const userContent = `
    <div style="
      position: relative;
      width: 24px;
      height: 24px;
    ">
      <div style="
        position: absolute;
        width: 24px;
        height: 24px;
        background: rgba(74, 124, 89, 0.3);
        border-radius: 50%;
        animation: pulse 2s infinite;
      "></div>
      <div style="
        position: absolute;
        top: 6px;
        left: 6px;
        width: 12px;
        height: 12px;
        background: #4A7C59;
        border-radius: 50%;
        border: 2px solid white;
        box-shadow: 0 2px 6px rgba(0,0,0,0.3);
      "></div>
    </div>
  `
  
  userMarker = new AMap.Marker({
    position: [lng, lat],
    content: userContent,
    offset: new AMap.Pixel(-12, -12)
  })
  
  map.add(userMarker)
  map.setCenter([lng, lat])
  map.setZoom(15)
  
  checkNearbySites(lng, lat)
}

const checkNearbySites = (lng, lat) => {
  if (sites.value.length === 0) return
  
  const nearbySites = sites.value.filter(site => {
    const distance = calculateDistance(lat, lng, site.latitude, site.longitude)
    return distance < 2
  })
  
  if (nearbySites.length > 0) {
    const randomSite = nearbySites[Math.floor(Math.random() * nearbySites.length)]
    triggerEasterEgg(randomSite)
  }
}

const calculateDistance = (lat1, lng1, lat2, lng2) => {
  const R = 6371
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLng = (lng2 - lng1) * Math.PI / 180
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLng/2) * Math.sin(dLng/2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
  return R * c
}

const triggerEasterEgg = (site) => {
  easterEggMessage.value = `在${site.name}发现了隐藏的历史故事！`
  easterEggReward.value = Math.floor(Math.random() * 50) + 10
  showEasterEgg.value = true
  
  emit('easter-egg-found', {
    site: site,
    reward: easterEggReward.value
  })
}

const handleExplore = (site) => {
  emit('explore', site)
}

const handleNavigate = (site) => {
  const url = `https://uri.amap.com/navigation?to=${site.longitude},${site.latitude},${encodeURIComponent(site.name)}&mode=car&policy=1&src=myapp&coordinate=gaode&callnative=1`
  window.open(url, '_blank')
}

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map) {
    map.destroy()
  }
  if (cityInfoTimer) {
    clearTimeout(cityInfoTimer)
  }
})
</script>

<style scoped>
.city-map {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 300px;
}

.city-selector {
  position: absolute;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  display: flex;
  gap: 8px;
  background: rgba(255, 255, 255, 0.95);
  padding: 6px;
  border-radius: 24px;
  box-shadow: 0 2px 12px rgba(45, 64, 89, 0.15);
}

.city-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  background: transparent;
  color: #8B9A9C;
}

.city-btn.active {
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  color: #F5F2EB;
}

.city-btn:hover:not(.active) {
  background: #E8ECEF;
}

.map-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 400px;
}

.map-controls {
  position: absolute;
  right: 16px;
  bottom: 180px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 100;
}

.city-info-card {
  position: absolute;
  top: 70px;
  left: 16px;
  background: rgba(245, 242, 235, 0.95);
  padding: 12px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(45, 64, 89, 0.15);
  z-index: 100;
}

.control-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: none;
  border-radius: 20px;
  background: rgba(245, 242, 235, 0.95);
  color: #2D4059;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(45, 64, 89, 0.15);
  transition: all 0.2s ease;
}

.control-btn:hover {
  background: #E8ECEF;
  transform: translateX(-2px);
}

.control-btn.active {
  background: linear-gradient(135deg, #4A7C59 0%, #5A8C69 100%);
  color: white;
}

.city-title {
  font-size: 20px;
  color: #2D4059;
  margin: 0 0 2px;
  letter-spacing: 0.1em;
}

.city-desc {
  font-size: 14px;
  color: #8B9A9C;
  margin: 0;
}

.site-card {
  position: absolute;
  bottom: 20px;
  left: 20px;
  right: 20px;
  background: rgba(245, 242, 235, 0.98);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(45, 64, 89, 0.2);
  overflow: hidden;
  z-index: 1000;
  display: flex;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  color: #2D4059;
  font-size: 18px;
  cursor: pointer;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.site-image {
  width: 140px;
  height: 160px;
  background-size: cover;
  background-position: center;
  position: relative;
  background-color: rgba(45, 64, 89, 0.08);
}

.site-image.no-image {
  display: flex;
  align-items: center;
  justify-content: center;
}

.site-image .image-placeholder {
  font-size: 48px;
  opacity: 0.5;
}

.site-type-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 10px;
  background: rgba(45, 64, 89, 0.85);
  color: #F5F2EB;
  font-size: 11px;
  border-radius: 12px;
}

.site-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.site-name {
  font-size: 18px;
  color: #2D4059;
  margin: 0 0 6px;
}

.site-address {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #8B9A9C;
  margin: 0 0 8px;
}

.site-desc {
  font-size: 13px;
  color: #5A6B6E;
  line-height: 1.5;
  margin: 0 0 12px;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.site-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
}

.tag {
  padding: 3px 8px;
  background: rgba(45, 64, 89, 0.08);
  color: #2D4059;
  font-size: 11px;
  border-radius: 10px;
}

.site-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px;
  border: 1px solid rgba(45, 64, 89, 0.15);
  border-radius: 10px;
  background: transparent;
  color: #2D4059;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: rgba(45, 64, 89, 0.05);
}

.action-btn.primary {
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  color: #F5F2EB;
  border: none;
}

.action-btn.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(45, 64, 89, 0.25);
}

.easter-egg-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.easter-egg-content {
  background: linear-gradient(135deg, #F5E6D3 0%, #E8D5C4 100%);
  border-radius: 20px;
  padding: 32px;
  max-width: 320px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: popIn 0.3s ease;
}

@keyframes popIn {
  0% { transform: scale(0.8); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

.easter-egg-icon {
  font-size: 48px;
  margin-bottom: 16px;
  animation: bounce 1s ease infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.easter-egg-title {
  font-size: 24px;
  color: #2D4059;
  margin: 0 0 12px;
}

.easter-egg-text {
  font-size: 14px;
  color: #5A6B6E;
  line-height: 1.6;
  margin: 0 0 20px;
}

.easter-egg-reward {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px;
  background: rgba(45, 64, 89, 0.1);
  border-radius: 12px;
  margin-bottom: 20px;
}

.reward-label {
  font-size: 12px;
  color: #8B9A9C;
}

.reward-value {
  font-size: 20px;
  font-weight: 600;
  color: #B8860B;
}

.easter-egg-btn {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  color: #F5F2EB;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.easter-egg-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(45, 64, 89, 0.3);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-slide-enter-active {
  animation: fadeSlideIn 0.5s ease;
}

.fade-slide-leave-active {
  animation: fadeSlideOut 1.5s ease;
}

@keyframes fadeSlideIn {
  0% {
    opacity: 0;
    transform: translateY(-20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeSlideOut {
  0% {
    opacity: 1;
    transform: translateY(0);
  }
  100% {
    opacity: 0;
    transform: translateY(-10px);
  }
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(2.5); opacity: 0; }
}
</style>
