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
    console.log('=== 请求拦截器 ===');
    console.log('请求 URL:', config.url);
    console.log('请求方法:', config.method);
    console.log('完整配置:', config);
    
    // 从 localStorage 获取 token
    let token = localStorage.getItem('token');
    console.log('当前 token:', token ? token.substring(0, 20) + '...' : 'null');
    
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
      console.log('✓ 已添加 Authorization 头');
    } else {
      console.warn('⚠ Token 无效，未添加 Authorization 头');
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
    console.log('=== 收到响应 ===');
    console.log('完整响应:', result);
    console.log('result.data:', result.data);
    console.log('result.data.code:', result.data?.code);
    
    // 判断业务状态码
    if (result.data.code === 1 || result.data.code === '200') {
      // 显示成功信息
      console.log('✓ 业务执行成功，返回数据');
      return result.data;
    }
    
    // 业务失败
    console.error('✗ 业务失败，code:', result.data.code, 'msg:', result.data?.msg);
    const errorMessage = result.data?.msg || '服务异常';
    ElMessage.error(errorMessage);
    // 返回 rejected promise，传递一个 Error 对象
    const error = new Error(errorMessage);
    error.code = result.data.code;
    error.data = result.data.data;
    return Promise.reject(error);
  },
  err => {
    const originalRequest = err.config;

    // 网络错误或其他异常
    console.error('=== 网络错误或异常 ===');
    console.error('错误类型:', typeof err);
    console.error('错误对象:', err);
    console.error('err.response:', err.response);
    console.error('err.message:', err.message);
    
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
    const errorMsg = err.response?.data?.msg || err.message || '网络连接异常';
    console.error('请求失败:', errorMsg);
    ElMessage.error(errorMsg);
    
    // 确保返回的是一个 Error 对象
    if (typeof err === 'string') {
      const error = new Error(err);
      error.response = err.response;
      return Promise.reject(error);
    }
    return Promise.reject(err);
  }
);

export default instance;