<template>
  <Teleport to="body">
    <transition name="modal">
      <div v-if="visible" class="edit-modal-overlay" @click.self="handleClose">
        <div class="edit-modal">
          <div class="modal-header">
            <h3 class="modal-title font-serif">编辑资料</h3>
            <button class="close-btn" @click="handleClose">×</button>
          </div>
          
          <div class="modal-body">
            <div class="avatar-section">
              <div class="avatar-preview" @click="triggerAvatarUpload">
                <img v-if="formData.avatarUrl" :src="formData.avatarUrl" alt="头像" />
                <div v-else class="avatar-placeholder">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                </div>
                <div class="avatar-edit-hint">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                    <circle cx="12" cy="13" r="4"/>
                  </svg>
                </div>
              </div>
              <input
                ref="avatarInput"
                type="file"
                accept="image/*"
                class="hidden-input"
                @change="handleAvatarChange"
              />
            </div>
            
            <div class="input-group">
              <label class="input-label">用户名</label>
              <input
                v-model="formData.username"
                type="text"
                class="input-field"
                placeholder="请输入用户名"
                maxlength="20"
              />
            </div>
            
            <div class="input-group">
              <label class="input-label">手机号</label>
              <input
                v-model="formData.phone"
                type="tel"
                class="input-field"
                placeholder="请输入手机号"
                maxlength="11"
              />
            </div>
            
            <div class="input-group">
              <label class="input-label">邮箱</label>
              <input
                v-model="formData.email"
                type="email"
                class="input-field"
                placeholder="请输入邮箱"
              />
            </div>
            
            <div class="input-group">
              <label class="input-label">性别</label>
              <div class="gender-options">
                <button
                  class="gender-btn"
                  :class="{ active: formData.gender === 1 }"
                  @click="formData.gender = 1"
                >
                  男
                </button>
                <button
                  class="gender-btn"
                  :class="{ active: formData.gender === 2 }"
                  @click="formData.gender = 2"
                >
                  女
                </button>
                <button
                  class="gender-btn"
                  :class="{ active: formData.gender === 0 || formData.gender === null }"
                  @click="formData.gender = 0"
                >
                  保密
                </button>
              </div>
            </div>
            
            <div class="input-group">
              <label class="input-label">所在地</label>
              <input
                v-model="formData.location"
                type="text"
                class="input-field"
                placeholder="请输入所在地"
                maxlength="50"
              />
            </div>
            
            <div class="input-group">
              <label class="input-label">真实姓名</label>
              <input
                v-model="formData.realName"
                type="text"
                class="input-field"
                placeholder="请输入真实姓名（选填）"
                maxlength="20"
              />
            </div>
            
            <div class="input-group">
              <label class="input-label">生日</label>
              <input
                v-model="formData.birthday"
                type="date"
                class="input-field"
              />
            </div>
            
            <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
            <p v-if="successMsg" class="success-msg">{{ successMsg }}</p>
          </div>
          
          <div class="modal-footer">
            <button class="cancel-btn" @click="handleClose">取消</button>
            <button
              class="save-btn"
              :disabled="loading || !hasChanges"
              @click="handleSave"
            >
              {{ loading ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, reactive } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { fileApi } from '@/api'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'success'])

const userStore = useUserStore()
const avatarInput = ref(null)
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const avatarFile = ref(null)

const formData = reactive({
  username: '',
  phone: '',
  email: '',
  gender: null,
  location: '',
  realName: '',
  birthday: '',
  avatarUrl: ''
})

const originalData = ref({})

const hasChanges = computed(() => {
  return JSON.stringify(formData) !== JSON.stringify(originalData.value) || avatarFile.value !== null
})

const triggerAvatarUpload = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = (event) => {
  const file = event.target.files?.[0]
  if (file) {
    if (file.size > 5 * 1024 * 1024) {
      errorMsg.value = '图片大小不能超过5MB'
      return
    }
    
    avatarFile.value = file
    
    const reader = new FileReader()
    reader.onload = (e) => {
      formData.avatarUrl = e.target?.result
    }
    reader.readAsDataURL(file)
  }
}

const handleSave = async () => {
  if (loading.value || !hasChanges.value) return
  
  loading.value = true
  errorMsg.value = ''
  successMsg.value = ''
  
  const updateData = {
    username: formData.username
  }
  
  if (formData.phone) updateData.phone = formData.phone
  if (formData.email) updateData.email = formData.email
  if (formData.gender !== null) updateData.gender = formData.gender
  if (formData.location) updateData.location = formData.location
  if (formData.realName) updateData.realName = formData.realName
  if (formData.birthday) updateData.birthday = formData.birthday
  
  if (avatarFile.value) {
    const uploadResult = await fileApi.upload(avatarFile.value)
    if (uploadResult.success && uploadResult.url) {
      updateData.avatarUrl = uploadResult.url
      formData.avatarUrl = uploadResult.url
    } else {
      errorMsg.value = '头像上传失败：' + (uploadResult.message || '未知错误')
      loading.value = false
      return
    }
  } else if (formData.avatarUrl && !formData.avatarUrl.startsWith('data:')) {
    updateData.avatarUrl = formData.avatarUrl
  }
  
  const result = await userStore.updateUserInfo(updateData)
  
  loading.value = false
  
  if (result.success) {
    successMsg.value = '保存成功'
    avatarFile.value = null
    originalData.value = { ...formData }
    setTimeout(() => {
      emit('success')
      handleClose()
    }, 1000)
  } else {
    errorMsg.value = result.error || '保存失败'
  }
}

const handleClose = () => {
  emit('close')
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    formData.username = userStore.username || ''
    formData.phone = userStore.phone || ''
    formData.email = userStore.email || ''
    formData.gender = userStore.gender
    formData.location = userStore.location || ''
    formData.realName = userStore.realName || ''
    formData.birthday = userStore.birthday || ''
    formData.avatarUrl = userStore.avatar || ''
    
    originalData.value = { ...formData }
    errorMsg.value = ''
    successMsg.value = ''
  }
})
</script>

<style scoped>
.edit-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.edit-modal {
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
  margin-bottom: 20px;
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

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

.avatar-preview {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #A8DADC 0%, #7FB3B5 100%);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  color: rgba(255, 255, 255, 0.8);
}

.avatar-edit-hint {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 24px;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFF;
}

.hidden-input {
  display: none;
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
  height: 44px;
  border: 1px solid rgba(45, 64, 89, 0.15);
  border-radius: 8px;
  padding: 0 12px;
  font-size: 15px;
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

.gender-options {
  display: flex;
  gap: 12px;
}

.gender-btn {
  flex: 1;
  height: 44px;
  border: 1px solid rgba(45, 64, 89, 0.15);
  border-radius: 8px;
  background: #FFF;
  color: #8B9A9C;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.gender-btn.active {
  background: #2D4059;
  border-color: #2D4059;
  color: #FFF;
}

.error-msg {
  font-size: 12px;
  color: #C43C3C;
  margin: 0;
  padding: 8px 0;
}

.success-msg {
  font-size: 12px;
  color: #4CAF50;
  margin: 0;
  padding: 8px 0;
}

.modal-footer {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.cancel-btn {
  flex: 1;
  height: 48px;
  border: 1px solid rgba(45, 64, 89, 0.2);
  border-radius: 8px;
  background: transparent;
  color: #8B9A9C;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: rgba(45, 64, 89, 0.05);
}

.save-btn {
  flex: 1;
  height: 48px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #2D4059 0%, #1A1A1A 100%);
  color: #F5F2EB;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.save-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.save-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .edit-modal,
.modal-leave-active .edit-modal {
  transition: transform 0.3s ease;
}

.modal-enter-from .edit-modal,
.modal-leave-to .edit-modal {
  transform: translateY(100%);
}
</style>
