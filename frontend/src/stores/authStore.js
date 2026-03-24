import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api'

const generateUUID = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

export const useAuthStore = defineStore('auth', () => {
  const guestId = ref(null)
  const isLoggedIn = ref(false)
  const user = ref(null)
  const token = ref(null)
  const showLoginModal = ref(false)
  const pendingAction = ref(null)

  const isGuest = computed(() => !isLoggedIn.value && guestId.value)
  
  const userType = computed(() => isLoggedIn.value ? 'USER' : 'GUEST')
  
  const userId = computed(() => {
    if (isLoggedIn.value && user.value) {
      return user.value.userId || user.value.id
    }
    return guestId.value
  })

  const initGuest = () => {
    const savedGuestId = localStorage.getItem('wenji_guest_id')
    if (savedGuestId) {
      guestId.value = savedGuestId
    } else {
      const newGuestId = `guest_${generateUUID()}`
      guestId.value = newGuestId
      localStorage.setItem('wenji_guest_id', newGuestId)
    }
  }

  const getAuthHeaders = () => {
    const headers = {
      'X-User-Type': userType.value,
      'X-User-Id': userId.value
    }
    if (token.value) {
      headers['Authorization'] = `Bearer ${token.value}`
    }
    return headers
  }

  const requireLogin = (action = null) => {
    if (isLoggedIn.value) {
      return true
    }
    pendingAction.value = action
    showLoginModal.value = true
    return false
  }

  const login = async (loginData) => {
    try {
      const result = await userApi.login(loginData)
      
      if (result.success && result.data) {
        isLoggedIn.value = true
        user.value = {
          userId: result.data.userId
        }
        token.value = result.data.accessToken
        
        localStorage.setItem('wenji_auth', JSON.stringify({
          isLoggedIn: true,
          user: user.value,
          token: token.value,
          lastLoginTime: result.data.lastLoginTime
        }))
        
        showLoginModal.value = false
        
        if (pendingAction.value) {
          pendingAction.value()
          pendingAction.value = null
        }
        
        return { success: true }
      } else {
        return { success: false, error: result.message || '登录失败' }
      }
    } catch (error) {
      console.error('Login failed:', error)
      return { success: false, error: error.message || '网络错误' }
    }
  }

  const logout = async () => {
    try {
      if (token.value) {
        await userApi.logout()
      }
    } catch (error) {
      console.warn('Logout API call failed:', error)
    } finally {
      isLoggedIn.value = false
      user.value = null
      token.value = null
      localStorage.removeItem('wenji_auth')
      initGuest()
    }
  }

  const loadFromStorage = () => {
    initGuest()
    
    try {
      const savedAuth = localStorage.getItem('wenji_auth')
      if (savedAuth) {
        const data = JSON.parse(savedAuth)
        if (data.isLoggedIn && data.user && data.token) {
          isLoggedIn.value = true
          user.value = data.user
          token.value = data.token
        }
      }
    } catch (e) {
      console.warn('加载认证数据失败:', e)
    }
  }

  const closeLoginModal = () => {
    showLoginModal.value = false
    pendingAction.value = null
  }

  return {
    guestId,
    isLoggedIn,
    isGuest,
    user,
    token,
    userType,
    userId,
    showLoginModal,
    pendingAction,
    initGuest,
    getAuthHeaders,
    requireLogin,
    login,
    logout,
    loadFromStorage,
    closeLoginModal
  }
})
