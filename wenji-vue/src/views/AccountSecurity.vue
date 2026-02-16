<template>
  <Layout title="账户与安全" :showBack="true">
    <!-- 账户信息（包括联系方式和登录凭证） -->
    <div class="security-item" @click="handleNavigate('account')">
      <div class="item-header">
        <div class="item-info">
          <div class="item-title">账户信息</div>
          <div class="item-desc">{{ phoneDisplay }} | 建议定期更新以保障安全</div>
        </div>
        <span class="arrow">→</span>
      </div>
    </div>

    <!-- 退出当前设备 -->
    <div class="security-item" @click="handleLogout">
      <div class="item-header">
        <div class="item-info">
          <div class="item-title">退出当前设备</div>
          <div class="item-desc">退出后需重新登录</div>
        </div>
        <span class="arrow">→</span>
      </div>
    </div>

    <!-- 注销账号 -->
    <div class="security-item" @click="handleDeleteAccount">
      <div class="item-header">
        <div class="item-info">
          <div class="item-title delete-text">注销账号</div>
          <div class="item-desc delete-text">永久删除所有文迹记录</div>
        </div>
        <span class="arrow delete-text">→</span>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import Layout from '@/components/Layout.vue'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { deleteUserAccount } from '@/api/user' // 导入注销账号API
import { ElMessage, ElMessageBox } from 'element-plus' // 导入Element Plus的消息框
import { getCurrentUserInfo } from '@/api/user'

// 响应式状态
const phoneDisplay = ref('加载中...')
const router = useRouter()

// ========== 生命周期：获取用户信息 ==========
onMounted(() => {
  fetchSecurityInfo()
})

// ========== 方法 ==========
function handleGoBack() {
  router.go(-1)
}

function handleNavigate(type) {
  if (type === 'account') {
    router.push('/user/modifycontactandpassword')
  }
}

async function handleLogout() {
  const shouldLogout = confirmLogout()
  if (!shouldLogout) return

  try {
    clearAuthState()
    router.push('/login')
  } catch (error) {
    ElMessage.error('注销过程中发生错误，请联系客服')
    console.error('登出失败', error)
  }
}

async function handleDeleteAccount() {
  try {
    // 显示确认对话框，带有更明显的警告样式
    const confirmResult = await ElMessageBox.confirm(
      '注销后所有数据将无法恢复，包括非遗打卡记录、兑换话费等。此操作不可逆，确定继续？',
      '危险操作确认',
      {
        confirmButtonText: '确认注销',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'confirm-delete-btn', // 自定义确认按钮样式
        dangerouslyUseHTMLString: true,
        distinguishCancelAndClose: true
      }
    )

    if (confirmResult === 'confirm') {
      // 调用后端注销账号接口
      const response = await deleteUserAccount()
      
      if (response.code === 1) {
        ElMessage.success('账号已成功注销')
        // 清除所有本地数据
        clearAllData()
        // 跳转到首页或登录页
        router.push('/')
      } else {
        ElMessage.error(response.msg || '注销失败，请稍后重试')
      }
    }
    // 如果用户点击取消，不做任何操作
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      // 用户取消操作
      console.log('用户取消注销操作')
    } else {
      console.error('注销失败', error)
      ElMessage.error('注销过程中发生错误，请联系客服')
    }
  }
}



async function fetchSecurityInfo() {
  try {
    const response = await getCurrentUserInfo()
    if (response.code === 1) {
      const phone = response.data.phone || '未绑定'; // 从后端获取真实电话号码
      phoneDisplay.value = maskPhone(phone)
    } else {
      phoneDisplay.value = '获取失败'
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    phoneDisplay.value = '获取失败'
  }
}

function maskPhone(phone) {
  if (!phone || phone.length < 11) return phone || '未绑定'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

function confirmLogout() {
  return window.confirm('确定要退出当前设备吗？')
}

// ========== 数据清除函数 ==========
function clearAuthState() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('chatHistory')
  localStorage.removeItem('currentChatId')
  localStorage.removeItem('userId')
  // 如果使用 Pinia/Vuex，也应清空用户状态
}

function clearAllData() {
  localStorage.clear()
  sessionStorage.clear()
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  background-color: #f8f5f0;
  font-family: 'Noto Serif SC', serif;
  color: #3a3530;
  line-height: 1.6;
  padding: 30px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.page {
  width: 100%;
  max-width: 450px;
  background: white;
  padding: 28px 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  margin: auto;
}

.back-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #a68a64;
  cursor: pointer;
  margin-right: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
}

.security-item {
  padding: 14px 0;
  border-bottom: 1px solid #efeae5;
  cursor: pointer;
}

.security-item:last-child {
  border-bottom: none;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-info {
  flex: 1;
  text-align: left;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #3a3530;
}

.item-desc {
  font-size: 14px;
  color: #8c7b6b;
  margin-top: 4px;
}

.arrow {
  color: #a68a64;
  font-size: 18px;
  margin-left: 8px;
}

.delete-text {
  color: #c44536 !important;
}

/* 自定义确认注销按钮样式 */
:deep(.confirm-delete-btn) {
  background-color: #c44536 !important;
  border-color: #c44536 !important;
}
</style>