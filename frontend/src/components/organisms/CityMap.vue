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
      <button class="control-btn" @click="checkNearbyEasterEggs">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
        </svg>
        <span>探索彩蛋</span>
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
        <div class="site-image" :style="{ backgroundImage: `url(${selectedSite.image})` }">
          <div class="site-type-tag">{{ selectedSite.type }}</div>
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
            <span v-for="tag in selectedSite.tags" :key="tag" class="tag">{{ tag }}</span>
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
import { ref, onMounted, onUnmounted } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'

const emit = defineEmits(['site-select', 'explore', 'easter-egg-found'])

const mapContainer = ref(null)
const selectedSite = ref(null)
const currentCity = ref(null)
const isLocating = ref(false)
const showEasterEgg = ref(false)
const showCityInfo = ref(false)
const easterEggMessage = ref('')
const easterEggReward = ref(0)

let map = null
let AMap = null
let markers = []
let userMarker = null
let cityInfoTimer = null

const AMAP_KEY = '774b31a8e76ec13cb1e73c4686e5de0e'

const cities = [
  {
    id: 'qingyuan',
    name: '清远',
    description: '山水清远，岭南明珠',
    center: [113.0562, 23.6817],
    zoom: 12,
    sites: [
      { id: 'qy1', name: '清远市博物馆', type: '博物馆', dynasty: '现代', lat: 23.7066, lng: 113.0541, address: '清远市清城区银泉北路', description: '展示清远历史文化，收藏大量珍贵文物', image: '/images/beijing1.jpg', tags: ['历史文物', '岭南文化', '免费开放'], easterEgg: '清远博物馆藏着一件千年古镜，传说能照见前世今生...' },
      { id: 'qy2', name: '飞来寺', type: '古建筑', dynasty: '南朝梁', lat: 23.6892, lng: 113.1234, address: '清远市清城区飞来峡', description: '始建于梁武帝时期，千年古刹，岭南名寺', image: '/images/beijing2.jpg', tags: ['佛教文化', '古建筑', '祈福'], easterEgg: '飞来寺的钟声据说能传到三里之外，每逢初一十五格外灵验...' },
      { id: 'qy3', name: '连州地下河', type: '自然景观', dynasty: '自然', lat: 24.7806, lng: 112.3814, address: '清远市连州市东陂镇', description: '典型的亚热带喀斯特地貌溶洞，被誉为"广东地下第一河"', image: '/images/beijing3.jpg', tags: ['溶洞奇观', '地下河', '自然遗产'], easterEgg: '地下河深处有一处"龙宫"，据说曾有蛟龙在此栖息...' },
      { id: 'qy4', name: '英德宝晶宫', type: '自然景观', dynasty: '自然', lat: 24.1856, lng: 113.4012, address: '清远市英德市燕子岩', description: '集溶洞、湖泊、山林于一体的综合性景区', image: '/images/beijing4.jpg', tags: ['溶洞', '湖泊', '避暑胜地'], easterEgg: '宝晶宫的石笋千姿百态，其中一根形似观音，香火鼎盛...' },
      { id: 'qy5', name: '瑶族文化博物馆', type: '博物馆', dynasty: '现代', lat: 24.1706, lng: 112.0876, address: '清远市连南瑶族自治县', description: '展示瑶族历史文化，体验瑶族风情', image: '/images/qinghuaci.jpg', tags: ['瑶族文化', '民族风情', '非遗'], easterEgg: '瑶族的盘王节是国家级非遗，每年农历十月十六举行盛大庆典...' }
    ]
  },
  {
    id: 'beijing',
    name: '北京',
    description: '六朝古都，文化名城',
    center: [116.4074, 39.9042],
    zoom: 12,
    sites: [
      { id: 'bj1', name: '故宫博物院', type: '博物馆', dynasty: '明清', lat: 39.9163, lng: 116.3972, address: '北京市东城区景山前街4号', description: '中国最大的古代文化艺术博物馆，世界五大宫殿之首', image: '/images/beijing1.jpg', tags: ['世界遗产', '皇家宫殿', '珍贵文物'], easterEgg: '故宫有9999间半房屋，传说少半间是为了不僭越天帝...' },
      { id: 'bj2', name: '中国国家博物馆', type: '博物馆', dynasty: '现代', lat: 39.9054, lng: 116.3976, address: '北京市东城区东长安街16号', description: '世界上单体建筑面积最大的博物馆，藏品143万余件', image: '/images/beijing2.jpg', tags: ['国家级', '历史文物', '免费参观'], easterEgg: '国博的司母戊鼎是迄今出土最重的青铜器，重达832.84公斤...' },
      { id: 'bj3', name: '天坛', type: '古建筑', dynasty: '明清', lat: 39.8822, lng: 116.4066, address: '北京市东城区天坛内东里7号', description: '明清两代皇帝祭天的场所，世界文化遗产', image: '/images/beijing3.jpg', tags: ['世界遗产', '皇家祭祀', '古建筑'], easterEgg: '天坛的回音壁可以传声，站在两端轻声说话，对方能清晰听到...' },
      { id: 'bj4', name: '颐和园', type: '皇家园林', dynasty: '清代', lat: 39.9999, lng: 116.2755, address: '北京市海淀区新建宫门路19号', description: '中国现存规模最大的皇家园林，世界文化遗产', image: '/images/beijing4.jpg', tags: ['世界遗产', '皇家园林', '山水景观'], easterEgg: '颐和园的长廊有728米，上面绘有14000多幅彩画，没有一幅重复...' },
      { id: 'bj5', name: '首都博物馆', type: '博物馆', dynasty: '现代', lat: 39.9073, lng: 116.3516, address: '北京市西城区复兴门外大街16号', description: '展示北京历史文化的综合性博物馆', image: '/images/qinghuaci.jpg', tags: ['北京历史', '民俗文化', '免费开放'], easterEgg: '首博的镇馆之宝是乾隆御制碑，记载了北京城的建城历史...' },
      { id: 'bj6', name: '雍和宫', type: '古建筑', dynasty: '清代', lat: 39.9474, lng: 116.4175, address: '北京市东城区雍和宫大街12号', description: '北京最大的藏传佛教寺院，曾是雍正皇帝府邸', image: '/images/fencaici.jpg', tags: ['藏传佛教', '皇家寺庙', '祈福'], easterEgg: '雍和宫是雍正皇帝的出生地，也是乾隆皇帝的出生地，出了两位皇帝...' }
    ]
  }
]

const easterEggMessages = [
  '你发现了一个隐藏的历史故事！',
  '恭喜你解锁了这片土地的秘密！',
  '古老的传说在此刻被你唤醒！',
  '你找到了时光留下的印记！',
  '历史的长河中，你拾起了一颗明珠！'
]

const getMarkerColor = (type) => {
  const colors = {
    '博物馆': '#2D4059',
    '古建筑': '#8B4513',
    '自然景观': '#4A7C59',
    '皇家园林': '#B8860B'
  }
  return colors[type] || '#2D4059'
}

const initMap = async () => {
  try {
    AMap = await AMapLoader.load({
      key: AMAP_KEY,
      version: '2.0',
      plugins: ['AMap.Scale', 'AMap.ToolBar', 'AMap.Geolocation', 'AMap.Marker']
    })
    
    currentCity.value = cities[0]
    
    map = new AMap.Map(mapContainer.value, {
      zoom: currentCity.value.zoom,
      center: currentCity.value.center,
      mapStyle: 'amap://styles/whitesmoke',
      viewMode: '2D'
    })
    
    map.addControl(new AMap.Scale())
    
    addMarkers(currentCity.value.sites)
    
    showCityInfo.value = true
    cityInfoTimer = setTimeout(() => {
      showCityInfo.value = false
    }, 3000)
  } catch (error) {
    console.error('地图加载失败:', error)
  }
}

const addMarkers = (sites) => {
  markers.forEach(marker => map.remove(marker))
  markers = []
  
  sites.forEach(site => {
    const color = getMarkerColor(site.type)
    
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
    
    const marker = new AMap.Marker({
      position: [site.lng, site.lat],
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

const switchCity = (city) => {
  currentCity.value = city
  selectedSite.value = null
  map.setZoomAndCenter(city.zoom, city.center)
  addMarkers(city.sites)
  
  showCityInfo.value = true
  if (cityInfoTimer) clearTimeout(cityInfoTimer)
  cityInfoTimer = setTimeout(() => {
    showCityInfo.value = false
  }, 3000)
}

const locateUser = () => {
  isLocating.value = true
  
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { longitude: lng, latitude: lat } = position.coords
        showUserLocation(lng, lat)
        isLocating.value = false
      },
      (error) => {
        console.log('浏览器定位失败:', error.message)
        aMapLocate()
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    )
  } else {
    aMapLocate()
  }
}

const aMapLocate = () => {
  if (!AMap) {
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
        simulateLocation()
      }
    })
  })
}

const simulateLocation = () => {
  isLocating.value = false
  const city = currentCity.value
  const randomOffset = () => (Math.random() - 0.5) * 0.02
  const simLng = city.center[0] + randomOffset()
  const simLat = city.center[1] + randomOffset()
  showUserLocation(simLng, simLat)
  
  easterEggMessage.value = '已为您定位到' + city.name + '市中心区域'
  easterEggReward.value = 5
  showEasterEgg.value = true
}

const showUserLocation = (lng, lat) => {
  if (userMarker) {
    map.remove(userMarker)
  }
  
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
  const nearbySites = currentCity.value.sites.filter(site => {
    const distance = calculateDistance(lat, lng, site.lat, site.lng)
    return distance < 2
  })
  
  if (nearbySites.length > 0) {
    const randomSite = nearbySites[Math.floor(Math.random() * nearbySites.length)]
    triggerEasterEgg(randomSite)
  } else {
    const randomSite = currentCity.value.sites[Math.floor(Math.random() * currentCity.value.sites.length)]
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

const checkNearbyEasterEggs = () => {
  const randomSite = currentCity.value.sites[Math.floor(Math.random() * currentCity.value.sites.length)]
  triggerEasterEgg(randomSite)
}

const triggerEasterEgg = (site) => {
  easterEggMessage.value = site.easterEgg || easterEggMessages[Math.floor(Math.random() * easterEggMessages.length)]
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
  const url = `https://uri.amap.com/navigation?to=${site.lng},${site.lat},${encodeURIComponent(site.name)}&mode=car&policy=1&src=myapp&coordinate=gaode&callnative=1`
  window.open(url, '_blank')
}

onMounted(() => {
  window._AMapSecurityConfig = {
    securityJsCode: '',
  }
  initMap()
})

onUnmounted(() => {
  if (map) {
    map.destroy()
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
