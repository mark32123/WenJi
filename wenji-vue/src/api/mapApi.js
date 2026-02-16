import api from '@/utils/request.js';

export const mapApi = {
  // 获取地图初始数据
  getInitialMapData(params) {
    return api.get('/map/initial', {
      params: {
        lat: params.lat || 30.2672,
        lng: params.lng || 120.1528,
        zoom: params.zoom || 12
      }
    });
  },

  // 地图范围查询
  getMapRangeData(params) {
    return api.get('/map/range', {
      params: {
        sw_lat: params.sw_lat,
        sw_lng: params.sw_lng,
        ne_lat: params.ne_lat,
        ne_lng: params.ne_lng,
        zoom_level: params.zoom_level || 14
      }
    });
  },

  // 获取用户位置
  getUserLocation() {
    return new Promise((resolve) => {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            resolve({
              lat: position.coords.latitude,
              lng: position.coords.longitude,
              accuracy: position.coords.accuracy
            });
          },
          (error) => {
            console.error('获取位置失败:', error);
            // 定位失败时回退到中国中心点
            resolve({ lat: 35.8617, lng: 104.1954, accuracy: null });
          },
          {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 0
          }
        );
      } else {
        // 浏览器不支持定位时回退到中国中心点
        resolve({ lat: 35.8617, lng: 104.1954, accuracy: null });
      }
    });
  }
};
