const BASE_URL = '/api'

const TOKEN_KEY = 'wenji_auth'

const getToken = () => {
  try {
    const auth = localStorage.getItem(TOKEN_KEY)
    if (!auth) {
      return null
    }
    
    let data
    try {
      data = JSON.parse(auth)
    } catch (parseError) {
      return auth
    }
    
    if (data && data.token) {
      return data.token
    } else if (typeof data === 'string') {
      return data
    }
    
    return null
  } catch (e) {
    return null
  }
}

const getGuestId = () => {
  return localStorage.getItem('wenji_guest_id')
}

const request = async (url, options = {}, skipAuth = false) => {
  const token = getToken()
  const guestId = getGuestId()
  
  const defaultHeaders = {
    'Content-Type': 'application/json',
  }
  
  if (!skipAuth) {
    if (token) {
      defaultHeaders['Authorization'] = `Bearer ${token}`
    }
    
    if (!token && guestId) {
      defaultHeaders['X-User-Type'] = 'GUEST'
      defaultHeaders['X-User-Id'] = guestId
    }
  }
  
  const config = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  }
  
  const fullUrl = url.startsWith('http') ? url : `${BASE_URL}${url}`
  
  try {
    const response = await fetch(fullUrl, config)
    
    let data
    try {
      const text = await response.text()
      if (text.trim()) {
        data = JSON.parse(text)
      } else {
        data = { code: 0, msg: '服务器返回空响应' }
      }
    } catch (parseError) {
      console.error('请求失败: 响应格式错误')
      data = { code: 0, msg: '服务器响应格式错误' }
    }
    
    if (!response.ok) {
      return {
        success: false,
        code: response.status,
        message: data.message || data.msg || '请求失败',
        data: null
      }
    }
    
    return {
      success: data.code === 200 || data.code === 1,
      code: data.code,
      message: data.message || data.msg || '',
      data: data.data
    }
  } catch (error) {
    console.error('网络错误:', error.message)
    return {
      success: false,
      code: -1,
      message: error.message || '网络错误',
      data: null
    }
  }
}

export const api = {
  get(url, params = {}, options = {}) {
    const queryString = Object.keys(params)
      .filter(key => params[key] !== undefined && params[key] !== null)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&')
    
    const fullUrl = queryString ? `${url}?${queryString}` : url
    return request(fullUrl, { method: 'GET' }, options.skipAuth)
  },
  
  post(url, data = {}) {
    return request(url, {
      method: 'POST',
      body: JSON.stringify(data)
    })
  },
  
  put(url, data = {}) {
    return request(url, {
      method: 'PUT',
      body: JSON.stringify(data)
    })
  },
  
  delete(url) {
    return request(url, { method: 'DELETE' })
  }
}

export const userApi = {
  getCaptcha() {
    return api.get('/user/captcha')
  },
  
  login(data) {
    return api.post('/user/login', data)
  },
  
  logout() {
    return api.post('/user/logout')
  },
  
  getCurrentUser() {
    return api.get('/user/currentUserInfo')
  },
  
  updateUserInfo(data) {
    return api.put('/user/updateUserInfo', data)
  },
  
  deleteUser() {
    return api.delete('/user/deleteUserInfo')
  }
}

export const badgeApi = {
  getUserBadges() {
    return api.get('/badge/myBadges')
  }
}

export const fileApi = {
  async upload(file) {
    const formData = new FormData()
    formData.append('file', file)
    
    const token = getToken()
    const headers = {}
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    
    try {
      const response = await fetch('/api/common/upload', {
        method: 'POST',
        headers,
        body: formData
      })
      
      const text = await response.text()
      let data
      try {
        data = JSON.parse(text)
      } catch (e) {
        return { success: false, message: '服务器响应格式错误' }
      }
      
      return {
        success: data.code === 1,
        code: data.code,
        message: data.msg || '',
        url: data.data
      }
    } catch (error) {
      return { success: false, message: error.message || '网络错误' }
    }
  }
}

export const heritageApi = {
  getNearbySites(lng, lat) {
    return api.get('/map/initial', { lng, lat }, { skipAuth: true })
  },
  
  getHeritageDetail(id) {
    return api.get(`/map/${id}`, {}, { skipAuth: true })
  }
}

export const aiApi = {
  chat(prompt, chatId, images) {
    const formData = new FormData()
    formData.append('prompt', prompt)
    if (chatId) {
      formData.append('chatId', chatId)
    }
    if (images && images.length > 0) {
      images.forEach((image) => {
        formData.append('images', image)
      })
    }
    
    return fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${getToken()}`
      },
      body: formData
    })
  },
  
  getChatIds(type = 'chat') {
    return api.get(`/ai/history/${type}/list`)
  },
  
  getChatHistory(type, sessionId) {
    return api.get(`/ai/history/${type}/${sessionId}`)
  },
  
  deleteSession(sessionId) {
    return api.delete(`/ai/history/session/${sessionId}`)
  }
}
