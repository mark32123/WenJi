<template>
  <Teleport to="body">
    <transition name="modal">
      <div v-if="visible" class="login-modal-overlay" @click.self="handleClose">
        <div class="login-modal">
          <div class="modal-header">
            <h3 class="modal-title font-serif">{{ isRegister ? '注册账号' : '登录文迹' }}</h3>
            <button class="close-btn" @click="handleClose">×</button>
          </div>
          
          <p class="modal-hint">{{ isRegister ? '注册后可同步阅历并兑换奖励' : '登录后可同步阅历并兑换奖励' }}</p>
          
          <div class="modal-body">
            <div class="input-group">
              <label class="input-label">手机号</label>
              <input
                v-model="phone"
                type="tel"
                class="input-field"
                placeholder="请输入手机号"
                maxlength="11"
              />
            </div>
            
            <div class="input-group">
              <label class="input-label">用户名</label>
              <input
                v-model="username"
                type="text"
                class="input-field"
                placeholder="请输入用户名"
                maxlength="20"
              />
            </div>
            
            <div class="input-group">
              <label class="input-label">密码</label>
              <input
                v-model="password"
                type="password"
                class="input-field"
                placeholder="请输入密码"
                maxlength="20"
              />
            </div>
            
            <div v-if="isRegister" class="input-group">
              <label class="input-label">确认密码</label>
              <input
                v-model="rePassword"
                type="password"
                class="input-field"
                placeholder="请再次输入密码"
                maxlength="20"
              />
            </div>
            
            <div class="input-group">
              <label class="input-label">验证码</label>
              <div class="captcha-row">
                <input
                  v-model="captcha"
                  type="text"
                  class="input-field captcha-input"
                  placeholder="请输入验证码"
                  maxlength="4"
                />
                <div class="captcha-image" @click="refreshCaptcha" :title="'点击刷新'">
                  <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
                  <span v-else class="captcha-loading">加载中...</span>
                </div>
              </div>
            </div>
            
            <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
            
            <button
              class="login-btn"
              :disabled="!canSubmit || loading"
              @click="handleSubmit"
            >
              {{ loading ? (isRegister ? '注册中...' : '登录中...') : (isRegister ? '注册' : '登录') }}
            </button>
          </div>
          
          <div class="modal-footer">
            <button class="switch-btn" @click="toggleMode">
              {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
            </button>
            <button class="guest-btn" @click="handleClose">
              暂不登录，继续浏览
            </button>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useUserStore } from '@/stores/userStore'
import { userApi } from '@/api'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'success'])

const authStore = useAuthStore()
const userStore = useUserStore()

const phone = ref('')
const username = ref('')
const password = ref('')
const rePassword = ref('')
const captcha = ref('')
const captchaKey = ref('')
const captchaImage = ref('')
const loading = ref(false)
const errorMsg = ref('')
const isRegister = ref(false)

const isPhoneValid = computed(() => /^1[3-9]\d{9}$/.test(phone.value))
const isUsernameValid = computed(() => username.value.trim().length >= 2)
const isPasswordValid = computed(() => password.value.length >= 6)
const isCaptchaValid = computed(() => captcha.value.trim().length >= 4)

const canSubmit = computed(() => {
  if (!isPhoneValid.value || !isUsernameValid.value || !isPasswordValid.value || !isCaptchaValid.value) {
    return false
  }
  if (isRegister.value && password.value !== rePassword.value) {
    return false
  }
  return true
})

const refreshCaptcha = async () => {
  try {
    const result = await userApi.getCaptcha()
    if (result.success && result.data) {
      captchaKey.value = result.data.captchaKey
      captchaImage.value = result.data.captchaImage
    }
  } catch (e) {
    console.error('获取验证码失败:', e)
  }
}

const toggleMode = () => {
  isRegister.value = !isRegister.value
  errorMsg.value = ''
  rePassword.value = ''
}

const handleSubmit = async () => {
  if (!canSubmit.value || loading.value) return
  
  if (isRegister.value && password.value !== rePassword.value) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }
  
  loading.value = true
  errorMsg.value = ''
  
  const loginData = {
    phone: phone.value,
    username: username.value,
    password: password.value,
    captcha: captcha.value,
    captchaKey: captchaKey.value
  }
  
  if (isRegister.value) {
    loginData.rePassword = rePassword.value
  }
  
  const result = await authStore.login(loginData)
  
  loading.value = false
  
  if (result.success) {
    await userStore.fetchUserInfo()
    emit('success')
    handleClose()
  } else {
    errorMsg.value = result.error || (isRegister.value ? '注册失败' : '登录失败')
    refreshCaptcha()
  }
}

const handleClose = () => {
  authStore.closeLoginModal()
  emit('close')
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    refreshCaptcha()
  } else {
    phone.value = ''
    username.value = ''
    password.value = ''
    rePassword.value = ''
    captcha.value = ''
    errorMsg.value = ''
    isRegister.value = false
  }
})
</script>

<style scoped>
.login-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.login-modal {
  width: 100%;
  max-width: 428px;
  background: #F5F2EB;
  border-radius: 20px 20px 0 0;
  padding: 24px 20px;
  padding-bottom: calc(env(safe-area-inset-bottom) + 24px);
  animation: slideUp 0.3s ease;
  max-height: 90vh;
  overflow-y: auto;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.modal-title {
  font-size: 20px;
  color: #2D4059;
  margin: 0;
  letter-spacing: 0.1em;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 24px;
  color: #8B9A9C;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-hint {
  font-size: 13px;
  color: #8B9A9C;
  margin: 0 0 20px 0;
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-label {
  font-size: 13px;
  color: #2D4059;
}

.input-field {
  width: 100%;
  height: 48px;
  border: 1px solid rgba(45, 64, 89, 0.15);
  border-radius: 8px;
  padding: 0 16px;
  font-size: 16px;
  background: #FFF;
  color: #2D4059;
  outline: none;
  transition: border-color 0.3s ease;
}

.input-field:focus {
  border-color: #A8DADC;
}

.input-field::placeholder {
  color: #8B9A9C;
}

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 100px;
  height: 48px;
  border: 1px solid rgba(45, 64, 89, 0.15);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFF;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.captcha-loading {
  font-size: 12px;
  color: #8B9A9C;
}

.error-msg {
  font-size: 12px;
  color: #C43C3C;
  margin: 0;
  padding: 8px 0;
}

.login-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #2D4059 0%, #1A1A1A 100%);
  border: none;
  border-radius: 8px;
  color: #F5F2EB;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
}

.login-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.login-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modal-footer {
  margin-top: 20px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.switch-btn {
  background: transparent;
  border: none;
  color: #A8DADC;
  font-size: 14px;
  cursor: pointer;
  padding: 8px 16px;
}

.switch-btn:hover {
  color: #2D4059;
}

.guest-btn {
  background: transparent;
  border: none;
  color: #8B9A9C;
  font-size: 13px;
  cursor: pointer;
  padding: 8px 16px;
}

.guest-btn:hover {
  color: #2D4059;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .login-modal,
.modal-leave-active .login-modal {
  transition: transform 0.3s ease;
}

.modal-enter-from .login-modal,
.modal-leave-to .login-modal {
  transform: translateY(100%);
}
</style>
