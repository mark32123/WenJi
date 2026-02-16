// src/config/map.config.js

export const MAP_CONFIG = {
  // 地图中心（中国大致中心）
  center: [35.8617, 104.1954],
  
  // 默认缩放级别
  zoom: 5,
  
  // 最小/最大缩放
  minZoom: 3,
  maxZoom: 18,
  
  // 瓦片图层 URL（使用免费、无 key 的服务）
  tileUrl: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
  
  // 版权信息
  attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
};