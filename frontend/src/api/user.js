//导入request.js工具
import request from '@/utils/request'


// 从 token 解析用户 ID 的辅助函数
function getUserIdFromToken() {
  const token = localStorage.getItem('token')
  if (token) {
    try {
      // 解析 JWT token
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.userId
    } catch (error) {
      console.error('解析 token 失败:', error)
      return null
    }
  }
  return null
}
//调用注册接口的函数
export const userRegisterService = (registerData)=>{
    return request.post('/user/register',registerData);
}
//调用登录接口的函数
export const userLoginService = (loginData)=>{
    return request.post('/user/login',loginData);
}

//调用AI聊天接口的函数
export const chatService = (chatData)=>{
    return request.post('/ai/chat',chatData);
}
// 调用AI历史接口的函数 - 添加类型参数，默认为 'chat'
export const chatHistoryService = (type = 'chat') => {
    return request.get(`/ai/history/${type}`);
}

// 获取单个会话历史的函数
export const getSingleChatHistory = (type, chatId) => {
    return request.get(`/ai/history/${type}/${chatId}`)
}

// 调用更改密码的函数 - 使用当前登录用户（如果后端需要直接参数）
export const updatePassword = (oldPassword, newPassword, rePassword) => {
  const userId = getUserIdFromToken();
  if (!userId) {
    throw new Error('未找到用户ID');
  }
  
  // 发送原始数据而不是对象
  return request.put(`/user/updatePassword/${userId}`, {
    oldPassword: oldPassword,
    newPassword: newPassword,
    rePassword: rePassword
  });
}

//调用获取用户信息的函数 - 使用当前登录用户
export const getCurrentUserInfo = () => {
  const userId = getUserIdFromToken();
  if (!userId) {
    throw new Error('未找到用户ID');
  }
  return request.get(`/user/info/${userId}`);
}

// 添加更新联系方式的函数 - 使用当前登录用户
export const updateContact = (phone) => {
  const userId = getUserIdFromToken();
  if (!userId) {
    throw new Error('未找到用户ID');
  }
  return request.put(`/user/update/${userId}`, {
    phone
  });
}

// 更新用户信息
export function updateUserInfo(data) {
  const userId = getUserIdFromToken();
  if (!userId) {
    throw new Error('未找到用户ID');
  }
  return request({
    url: `/user/updateUserInfo/${userId}`,  //使用模板字符串
    method: 'put', 
    data
  })
}

//删除用户信息
export function deleteUserAccount() {
  const userId = getUserIdFromToken();
  if (!userId) {
    throw new Error('未找到用户ID');
  }
  return request({
    url: `/user/deleteUserInfo/${userId}`,  //使用模板字符串
    method: 'delete' 
  })
}
