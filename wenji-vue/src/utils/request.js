// src/utils/request.js
import axios from 'axios';
import { ElMessage } from 'element-plus';

// 定义一个变量,记录公共的前缀  ,  baseURL
const baseURL = '/api';
// const baseURL = 'http://8.134.126.231:8080';
const instance = axios.create({ baseURL });

// 解析 JWT token 获取过期时间
const parseJwt = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
};

// 检查 token 是否即将过期（5分钟内）
const isTokenExpiringSoon = (token) => {
  if (!token) return true;
  
  const decoded = parseJwt(token);
  if (!decoded || !decoded.exp) return true;
  
  const now = Date.now() / 1000;
  const buffer = 5 * 60; // 5分钟缓冲时间
  
  return decoded.exp - now < buffer;
};

// 请求拦截器 - 添加认证头
instance.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token
    let token = localStorage.getItem('token');
    
    // 检查 token 是否即将过期，如果是则先刷新
    if (token && isTokenExpiringSoon(token)) {
      // 在这里调用刷新接口
      // 注意：你需要根据实际后端接口调整
      refreshToken().catch(err => {
        console.error('Auto refresh failed:', err);
      });
    }
    
    // 更严格的 token 检查
    if (token && token !== 'undefined' && token !== 'null' && token.trim() !== '') {
      // 设置 Authorization 头，注意格式：Bearer <token>
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    // 特殊处理 FormData 请求 - 删除 Content-Type 让浏览器自动设置
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type'];
    }
    
    return config;
  },
  error => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 刷新 token 的函数（需要根据你的后端接口调整）
const refreshToken = async () => {
  try {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }
    
    const response = await axios.post(`${baseURL}/auth/refresh`, {
      refreshToken
    });
    
    if (response.data.code === 1 || response.data.code === '200') {
      const newToken = response.data.data.accessToken || response.data.accessToken;
      localStorage.setItem('token', newToken);
      return newToken;
    } else {
      throw new Error('Failed to refresh token');
    }
  } catch (error) {
    console.error('Token refresh failed:', error);
    // 刷新失败，清除所有 token 并跳转到登录页
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    window.location.href = '/login';
    return null;
  }
};

// 响应拦截器 - 统一处理
instance.interceptors.response.use(
  result => {
    // 判断业务状态码
    if (result.data.code === 1 || result.data.code === '200') {
      // 显示成功信息
      return result.data;
    }
    
    // 显示错误信息
    const errorMessage = result.data?.msg || '服务异常';
    ElMessage.error(errorMessage);
    // 返回 rejected promise
    return Promise.reject(result.data);
  },
  err => {
    const originalRequest = err.config;

    // 网络错误或其他异常
    console.error('网络错误:', err);
    
    // 检查是否是 401 错误且不是刷新 token 的请求
    if (err.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      return refreshToken()
        .then(newToken => {
          if (newToken) {
            originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
            return instance(originalRequest);
          } else {
            return Promise.reject(err);
          }
        })
        .catch(refreshErr => {
          console.error('Refresh token failed:', refreshErr);
          return Promise.reject(refreshErr);
        });
    }
    
    // 其他错误处理
    alert(err.response?.data?.msg || '网络连接异常');
    return Promise.reject(err);
  }
);

export default instance;