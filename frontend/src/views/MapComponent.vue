<template>
  <!-- 主地图区域 - 修正容器样式 -->
  <div ref="mapContainer" class="map-root-container">
    <div class="map" ref="mapElement"></div>
    <!-- 加载动画 -->
    <div v-if="loading" class="map-loading-overlay">
      <div class="map-loading-content">
        <div class="map-spinner">
          <svg class="map-spinner-svg" viewBox="0 0 50 50">
            <circle class="map-spinner-circle" cx="25" cy="25" r="20" fill="none" stroke="#e0e0e0" stroke-width="4"/>
            <circle class="map-spinner-arc" cx="25" cy="25" r="20" fill="none" stroke="#FF6B8B" stroke-width="4" stroke-linecap="round"/>
            <circle class="map-spinner-dot" cx="5" cy="25" r="3" fill="#FF6B8B"/>
          </svg>
        </div>
        <div class="map-loading-text">正在加载中国非遗地图...</div>
      </div>
    </div>

    <!-- 缩放按钮 -->
    <button
      v-if="!isLocating"
      class="locate-btn"
      @click="locateUser"
      title="定位到我的位置"
    >
    📍
    </button>

    <button
      v-else
      class="locate-btn locating"
      disabled
    >
    🔍
    </button>
    <!-- 比例尺 -->
    <div class="map-scale-control">
      <div class="map-scale-bar"></div>
      <div class="map-scale-text">1000 km</div>
    </div>
    <!-- 底部信息抽屉 -->
    <transition name="slide-up">
      <div v-if="selectedSite" class="map-bottom-drawer">
        <div class="drawer-content">
          <div class="drawer-header">
            <h3>{{ selectedSite.name }}</h3>
            <button class="close-btn" @click="clearSelection">×</button>
          </div>
          <div class="drawer-body">
            <p><strong>类型：</strong>{{ getSiteTypeText(selectedSite.type) }}</p>
            <p><strong>状态：</strong>{{ getStatusText(selectedSite.status) }}</p>
            <p><strong>热度：</strong>{{ Math.round((selectedSite.heat_level || 0) * 100) }}%</p>
            <p><strong>所在省份：</strong>{{ selectedSite.province }}</p>
          </div>
          <button class="ar-button" @click="startAR">
            👓 AR虚拟体验
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { CHINA_NON_HERITAGE_SITES, MAP_CONFIG, PROVINCE_DATA } from '@/assets/data/heritage-sites';
import { mapApi } from '@/api/mapApi.js';
import Layout from '@/components/Layout.vue'
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
const router = useRouter();
// 定义事件
const emit = defineEmits(['site-clicked']);

// 响应式数据
const mapContainer = ref(null);
const mapElement = ref(null);
const map = ref(null);
const loading = ref(true);
const userLocation = ref(null);
const sites = ref([]);
const provinces = ref([]);
const selectedSite = ref(null);
const showProvinceBorders = ref(true);
const showHeritageSites = ref(true);
const siteMarkers = ref([]);
const clusterLayers = ref([]);
const selectedProvince = ref(null); // 添加这行
// 初始化地图
const initMap = async () => {
  if (!mapElement.value) return;

  try {
    // 1. 获取用户位置
    userLocation.value = await mapApi.getUserLocation();
    const center = [userLocation.value.lat, userLocation.value.lng];

    // 2. 创建地图实例
    map.value = L.map(mapElement.value).setView(center, MAP_CONFIG.zoom);

    // 3. 添加底图
    L.tileLayer('https://{s}.basemaps.cartocdn.com/light_nolabels/{z}/{x}/{y}{r}.png', {
      attribution: '© OpenStreetMap contributors, © CARTO',
      maxZoom: MAP_CONFIG.maxZoom,
      minZoom: MAP_CONFIG.minZoom
    }).addTo(map.value);

    // 4. 调用接口
    let initialData = null;
    try {
      initialData = await mapApi.getInitialMapData({
        lat: center[0],
        lng: center[1],
        zoom: MAP_CONFIG.zoom
      });
    } catch (apiError) {
      console.warn('接口调用失败，使用本地数据:', apiError);
      // 接口失败时用本地数据
      initialData = {
        nearby_sites: CHINA_NON_HERITAGE_SITES,
        unlock_status: { provinces: PROVINCE_DATA }
      };
    }

    // 5. 处理景点数据
    sites.value = initialData.nearby_sites || CHINA_NON_HERITAGE_SITES;
    // 处理省份数据
    provinces.value = processProvinceData(initialData.unlock_status?.provinces || PROVINCE_DATA);

    // 6. 渲染景点
    if (showHeritageSites.value) {
      addSiteMarkers();
    }

    // 7. 监听地图移动
    map.value.on('moveend', handleMapMoveEnd);

  } catch (error) {
    console.error('初始化地图失败:', error);
    // 直接用本地数据初始化
    map.value = L.map(mapElement.value).setView(MAP_CONFIG.center, MAP_CONFIG.zoom);
    L.tileLayer('https://{s}.basemaps.cartocdn.com/light_nolabels/{z}/{x}/{y}{r}.png').addTo(map.value);
    sites.value = CHINA_NON_HERITAGE_SITES;
    provinces.value = processProvinceData(PROVINCE_DATA);
    addSiteMarkers();
  } finally {
    loading.value = false;
  }
};

// 处理省份数据
const processProvinceData = (apiProvinces) => {
  return apiProvinces.map(province => ({
    name: province.name,
    code: province.code,
    center: province.center || MAP_CONFIG.center,
    color: getProvinceColor(province.code),
    // 接口返回布尔值，本地返回数字，统一处理
    unlocked: province.unlocked === true ? 1 : province.unlocked || 0,
    // 接口返回0-1比例，本地返回百分比，统一处理
    progress: typeof province.progress === 'number' ?
      (province.progress > 1 ? province.progress : province.progress * 100) : 0,
    siteCount: province.total_sites || 0,
    visited: province.visited_sites || 0
  }));
};

// 获取省份颜色
const getProvinceColor = (provinceCode) => {
  const colors = [
    '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7',
    '#DDA0DD', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9',
    '#F8C471', '#82E0AA', '#F1948A', '#D7BDE2', '#F9E79F'
  ];
  const index = parseInt(provinceCode.substring(0, 2), 10) % colors.length;
  return colors[index];
};

// 地图移动结束处理
const handleMapMoveEnd = async () => {
  if (!map.value) return;
  loading.value = true;

  const bounds = map.value.getBounds();
  try {
    // 尝试调用范围接口
    //暂时注释
    // const rangeData = await mapApi.getMapRangeData({
    //   sw_lat: bounds.getSouthWest().lat,
    //   sw_lng: bounds.getSouthWest().lng,
    //   ne_lat: bounds.getNorthEast().lat,
    //   ne_lng: bounds.getNorthEast().lng,
    //   zoom_level: map.value.getZoom()
    // });
    // // 接口返回数据则更新
    // if (rangeData.sites) {
    //   sites.value = rangeData.sites;
    // }
    const filteredSites = CHINA_NON_HERITAGE_SITES.filter(site =>
      bounds.contains([site.lat, site.lng])
    );
    sites.value = filteredSites;
  } catch (apiError) {
    console.warn('范围接口调用失败，使用本地数据:', apiError);
    // 接口失败时筛选本地数据
    sites.value = CHINA_NON_HERITAGE_SITES.filter(site =>
      bounds.contains([site.lat, site.lng])
    );
  }

  // 更新可见景点
  updateVisibleSites(sites.value);
  loading.value = false;
};

// 更新可见景点
const updateVisibleSites = (visibleSites) => {
  removeAllSiteMarkers();
  removeAllClusters();

  if (showHeritageSites.value && visibleSites.length) {
    visibleSites.forEach(site => {
      const marker = createSiteMarker(site);
      if (marker) {
        marker.addTo(map.value);
        siteMarkers.value.push(marker);
      }
    });
  }
};

// 移除所有景点标记
const removeAllSiteMarkers = () => {
  siteMarkers.value.forEach(marker => {
    if (map.value && marker) map.value.removeLayer(marker);
  });
  siteMarkers.value = [];
};

// 移除所有聚合点
const removeAllClusters = () => {
  clusterLayers.value.forEach(layer => {
    if (map.value && layer) map.value.removeLayer(layer);
  });
  clusterLayers.value = [];
};

// 添加所有景点标记
const addSiteMarkers = () => {
  if (!map.value) return;
  const bounds = map.value.getBounds();
  sites.value.forEach(site => {
    if (bounds.contains([site.lat, site.lng])) {
      const marker = createSiteMarker(site);
      if (marker) {
        marker.addTo(map.value);
        siteMarkers.value.push(marker);
      }
    }
  });
};

// 创建景点标记
const createSiteMarker = (site) => {
  let color, icon, size = 28;

  switch(site.status) {
    case 'unlocked':
      color = '#4CAF50';
      icon = getSiteIcon(site.type);
      break;
    case 'visited':
      color = '#2196F3';
      icon = getSiteIcon(site.type);
      size = 32;
      break;
    case 'locked':
    default:
      color = '#FF9800';
      icon = '🔒';
      break;
  }

  const markerHtml = `
    <div style="
      background: ${color};
      width: ${size}px;
      height: ${size}px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: ${size * 0.5}px;
      border: 3px solid white;
      box-shadow: 0 2px 8px rgba(0,0,0,0.3);
      cursor: pointer;
      transition: transform 0.2s ease;
    " onmouseover="this.style.transform='scale(1.1)'" onmouseout="this.style.transform='scale(1)'">
      ${icon}
    </div>
  `;

  const customIcon = L.divIcon({
    html: markerHtml,
    className: 'map-heritage-marker',
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2]
  });

  const marker = L.marker([site.lat, site.lng], {
    icon: customIcon,
    title: site.name
  });

marker.on('click', (e) => {
  L.DomEvent.stopPropagation(e);
  selectedSite.value = site;
  const province = provinces.value.find(p => p.name === site.province);
  if (province) selectedProvince.value = province; // 这里之前引用了未定义的 selectedProvince
});

  return marker;
};

// 获取景点类型图标
const getSiteIcon = (type) => {
  const iconMap = {
    handicraft: '🏺',
    museum: '🏛️',
    archaeology: '🏺',
    architecture: '🏯',
    food_craft: '🍵',
    performance: '🎭',
    default: '📍'
  };
  return iconMap[type] || iconMap.default;
};

// 显示景点弹窗
const showSitePopup = (marker, site) => {
  const popupContent = `
    <div style="padding: 16px; max-width: 280px;">
      <h3 style="margin: 0 0 12px 0; color: #333; font-size: 18px;">${site.name}</h3>
      <div style="color: #666; font-size: 14px;">
        <div><strong>类型:</strong> ${getSiteTypeText(site.type)}</div>
        <div><strong>类别:</strong> ${site.category || '未知'}</div>
        <div><strong>省份:</strong> ${site.province}</div>
        <div><strong>热度:</strong> ${Math.round((site.heat_level || 0) * 100)}%</div>
        <div><strong>状态:</strong> ${getStatusText(site.status)}</div>
        ${site.distance ? `<div><strong>距离:</strong> ${site.distance} 米</div>` : ''}
      </div>
      <button onclick="window.mapViewSiteDetail('${site.site_id}', ${JSON.stringify(site).replace(/'/g, '&quot;')})"
        style="
          width: 100%;
          padding: 10px;
          margin-top: 12px;
          background: #667eea;
          color: white;
          border: none;
          border-radius: 8px;
          cursor: pointer;
        ">
        查看详情
      </button>
    </div>
  `;

  marker.bindPopup(popupContent).openPopup();
};

// 修改全局函数来发射事件
window.mapViewSiteDetail = (siteId, siteData) => {
  console.log('查看景点详情:', siteId);
  // 发射事件给父组件
  emit('site-clicked', JSON.parse(siteData.replace(/&quot;/g, '"')));
};

// 辅助函数
const getStatusText = (status) => {
  const map = { 'unlocked': '已解锁', 'locked': '未解锁', 'visited': '已访问' };
  return map[status] || status;
};

const getSiteTypeText = (type) => {
  const map = {
    'handicraft': '手工艺', 'museum': '博物馆', 'archaeology': '考古遗址',
    'architecture': '传统建筑', 'food_craft': '食品工艺', 'performance': '表演艺术'
  };
  return map[type] || type;
};

// 控制功能
const locateUser = async () => {
  try {
    const newLocation = await mapApi.getUserLocation();
    userLocation.value = newLocation;
    if (userLocation.value && map.value) {
      map.value.setView([userLocation.value.lat, userLocation.value.lng], 8);
      const userIcon = L.divIcon({
        html: '<div style="background: #FF4081; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white;"></div>',
        className: 'map-user-location',
        iconSize: [20, 20],
        iconAnchor: [10, 10]
      });
      L.marker([userLocation.value.lat, userLocation.value.lng], { icon: userIcon })
        .addTo(map.value).bindPopup('我的位置').openPopup();
    }
  } catch (error) {
    console.error('定位失败:', error);
    const defaultCenter = MAP_CONFIG.center;
    map.value.setView(defaultCenter, 8);
    L.marker(defaultCenter, {
      icon: L.divIcon({
        html: '<div style="background: #FF4081; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white;"></div>',
        className: 'map-user-location',
        iconSize: [20, 20],
        iconAnchor: [10, 10]
      })
    }).addTo(map.value).bindPopup('定位失败，已定位到中国中心点').openPopup();
  }
};

const zoomIn = () => map.value?.zoomIn();
const zoomOut = () => map.value?.zoomOut();

const toggleProvinceBorders = () => {
  console.log('省份边界显示状态:', showProvinceBorders.value);
};

const toggleHeritageSites = () => {
  if (showHeritageSites.value) {
    addSiteMarkers();
  } else {
    removeAllSiteMarkers();
  }
};

const viewProvinceDetails = () => {
  if (selectedProvince.value) {
    console.log('查看省份详情:', selectedProvince.value);
  }
};

const startAR = () => {
  if (!selectedSite.value) return;
  router.push('/user/ceramicAr');
  ElMessage.info('特定地点 AR 暂未开放，正在跳转到AR页面');
  
  clearSelection();
};
const clearSelection=()=>{
  selectedSite.value = null;
}
// 生命周期
onMounted(async () => {
  // 确保 DOM 已渲染后再初始化地图
  await nextTick();
  initMap();
});

onUnmounted(() => {
  if (map.value) {
    map.value.off('moveend', handleMapMoveEnd);
    map.value.remove();
  }
  removeAllClusters();
  delete window.mapViewSiteDetail;
});
</script>

<style scoped>
/* 地图容器 */
.map-root-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.map {
  width: 100%;
  height: 100%;
  background: #e8f4f8;
}

/* 加载动画 */
.map-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.map-loading-content {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.map-spinner {
  margin: 0 auto 25px;
  width: 70px;
  height: 70px;
}

.map-spinner-svg {
  width: 100%;
  height: 100%;
  animation: map-spinner-rotate 2s linear infinite;
}

.map-spinner-circle {
  stroke: #e8f4f8;
  stroke-width: 4;
  fill: none;
}

.map-spinner-arc {
  stroke: #667eea;
  stroke-width: 4;
  stroke-linecap: round;
  fill: none;
  stroke-dasharray: 1, 200;
  stroke-dashoffset: 0;
  animation: map-spinner-dash 1.5s ease-in-out infinite;
}

.map-spinner-dot {
  fill: #667eea;
  animation: map-spinner-pulse 2s ease-in-out infinite;
}

.map-loading-text {
  color: #4a5568;
  font-size: 16px;
  font-weight: 500;
}

@keyframes map-spinner-rotate {
  100% { transform: rotate(360deg); }
}

@keyframes map-spinner-dash {
  0% {
    stroke-dasharray: 1, 200;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 89, 200;
    stroke-dashoffset: -35px;
  }
  100% {
    stroke-dasharray: 89, 200;
    stroke-dashoffset: -124px;
  }
}

@keyframes map-spinner-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.1); }
}

/* 控制面板 */
.map-control-panel {
  position: absolute;
  top: 20px;
  right: 20px;
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  min-width: 220px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}
/* —— 定位按钮 —— */
.locate-btn {
  position: absolute;
  bottom: 80px; /* 避开底部抽屉区域 */
  right: 16px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  font-size: 20px;
  color: #a68a64;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(4px);
  transition: all 0.2s;
  z-index: 1000;
}

.locate-btn:hover:not(:disabled) {
  background: white;
  transform: scale(1.1);
}

.locate-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.map-control-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.map-zoom-only {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.map-zoom-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: white;
  font-size: 18px;
  cursor: pointer;
  color: #4a5568;
}
.map-zoom-btn:first-child {
  border-bottom: 1px solid #eee;
}

/* 图层控制 */
.map-layer-title {
  font-size: 14px;
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 8px;
}

.map-layer-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.map-layer-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

/* 图例 */
.map-legend {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.map-legend-title {
  font-size: 14px;
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 16px;
}

.map-legend-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.map-legend-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.map-legend-color {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 3px solid white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}

.map-legend-unlocked { background: #4CAF50; }
.map-legend-locked { background: #FF9800; }
.map-legend-visited { background: #2196F3; }
.map-legend-selected { background: #FF4081; }

.map-legend-text {
  font-size: 13px;
  color: #4a5568;
}

/* 省份信息 */
.map-province-info {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.map-province-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 12px;
}

.map-province-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.map-stat-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.map-stat-label {
  color: #718096;
}

.map-stat-value {
  color: #2d3748;
  font-weight: 600;
}

.map-view-btn {
  width: 100%;
  padding: 10px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s ease;
}

.map-view-btn:hover {
  background: #5a67d8;
}

/* 比例尺 */
.map-scale-control {
  position: absolute;
  bottom: 20px;
  left: 20px;
  background: white;
  padding: 8px 12px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  font-size: 12px;
  color: #4a5568;
}

@media (max-width: 768px) {
  .map-control-panel {
    top: 10px;
    right: 10px;
    left: 10px;
    padding: 16px;
  }

  .map-scale-control {
    bottom: 10px;
    left: 10px;
  }
}

:deep(.map-heritage-marker) {
  pointer-events: auto !important;
}

:deep(.leaflet-popup-content-wrapper) {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}

/* —— 底部抽屉动画 —— */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94), opacity 0.2s;
}
.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

/* —— 抽屉容器 —— */
.map-bottom-drawer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1001;
  padding: 0 16px 24px;
  pointer-events: none;
}

.map-bottom-drawer > * {
  pointer-events: auto;
}

.drawer-content {
  background: white;
  border-radius: 16px 16px 0 0;
  padding: 20px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15);
  max-width: 500px;
  margin: 0 auto;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.drawer-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #3a3530;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #8c7b6b;
  width: 30px;
  height: 30px;
  cursor: pointer;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background-color: #f0f0f0;
}

.drawer-body p {
  font-size: 14px;
  color: #5a524a;
  margin: 8px 0;
}

.ar-button {
  width: 100%;
  background: linear-gradient(135deg, #a68a64, #8b7355);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 14px;
  font-size: 16px;
  font-weight: 600;
  font-family: "Noto Serif SC", serif;
  cursor: pointer;
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: transform 0.2s;
}

.ar-button:hover {
  transform: scale(1.02);
}
</style>