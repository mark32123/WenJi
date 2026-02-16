<template>
  <Layout title="修改联络方式与通行密令" :showBack="true">
    <div class="page">
      <!-- 修改联络方式 -->
      <section class="section">
        <h2 class="section-title">联络方式</h2>
        <div class="form-group">
          <label class="form-label">当前联络方式</label>
          <input
            type="text"
            class="form-input"
            :value="currentPhone"
            readonly
          />
        </div>
        <div class="form-group">
          <label class="form-label">新联络方式</label>
          <input
            v-model="newPhone"
            type="tel"
            class="form-input"
            placeholder="请输入新的手机号"
          />
        </div>
        <button class="submit-btn" @click="updateContactMethod">
          确认修改
        </button>
        <p class="tip">💡 修改后，登录、重要操作将使用新联络方式接收验证。</p>
      </section>

      <!-- 修改通行密令 -->
      <section class="section">
        <h2 class="section-title">通行密令</h2>
        <div class="form-group">
          <label class="form-label">当前密令</label>
          <input
            v-model="oldPassword"
            type="password"
            class="form-input"
            placeholder="请输入当前通行密令"
          />
        </div>
        <div class="form-group">
          <label class="form-label">新密令</label>
          <input
            v-model="newPassword"
            type="password"
            class="form-input"
            placeholder="不少于6位，建议含字母与数字"
          />
        </div>
        <div class="form-group">
          <label class="form-label">确认新密令</label>
          <input
            v-model="rePassword"
            type="password"
            class="form-input"
            placeholder="请再次输入新密令"
          />
        </div>
        <button class="submit-btn" @click="updatePasswordMethod">
          确认修改
        </button>
        <p class="tip">💡 建议定期更新密令，并勿与他人共享。</p>
      </section>
    </div>
  </Layout>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import Layout from '../components/Layout.vue'
import { ElMessage } from 'element-plus'
import { updatePassword, getCurrentUserInfo, updateContact } from '@/api/user'

// 当前手机号（可从用户信息接口获取）
const currentPhone = ref('')

// 联络方式表单数据
const newPhone = ref('')

// 通行密令表单数据
const oldPassword = ref('')
const newPassword = ref('')
const rePassword = ref('')

// 返回上一页
function goBack() {
  history.back()
}

// 提交联络方式修改
async function updateContactMethod() {
  if (!newPhone.value) {
    ElMessage.error('请输入新的手机号')
    return
  }
  
  // 验证手机号格式
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(newPhone.value)) {
    ElMessage.error('请输入正确的手机号格式')
    return
  }
  
  try{
    await updateContact(newPhone.value)
    ElMessage.success('更新成功')
    // 更新当前显示的手机号
    currentPhone.value = newPhone.value
    // 清空新手机号输入框
    newPhone.value = ''
    goBack()
  }catch(error){
    console.error('更新失败：', error)
    ElMessage.error('更新失败')
  }
}

// 提交通行密令修改
async function updatePasswordMethod() {
  const { oldPassword: old, newPassword: nw, rePassword: re } = {
    oldPassword: oldPassword.value,
    newPassword: newPassword.value,
    rePassword: rePassword.value
  }

  if (!old) {
    ElMessage.error('请输入当前通行密令')
    return
  }
  if (!nw) {
    ElMessage.error('请输入新密令')
    return
  }
  if (nw.length < 6) {
    ElMessage.error('新密令长度至少6位')
    return
  }
  if (nw !== re) {
    ElMessage.error('两次输入的新密令不一致')
    return
  }

  try{
    await updatePassword(old, nw, re)
    ElMessage.success('更新成功')
    // 成功后清空字段
    oldPassword.value = ''
    newPassword.value = ''
    rePassword.value = ''
  }catch(error){
    console.error('更新密码失败：', error)
    ElMessage.error('更新密码失败')
  }
}

onMounted(async()=>{
  try{
    const response = await getCurrentUserInfo()
    console.log('获取用户信息响应:', response)  // 调试信息
    
    
    if(response && response.data && response.data.phone) {
      currentPhone.value = response.data.phone
      console.log('设置当前手机号:', response.data.phone)
    } else {
      console.error('响应中未找到 phone 字段:', response)
      ElMessage.error('获取用户信息失败：未找到手机号')
    }
  }catch(error){
    console.error('获取用户信息失败：', error)
    ElMessage.error('获取用户信息失败')
  }
})
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
  max-width: 390px;
  background: white;
  padding: 28px 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
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
  text-align: center;
  flex: 1;
}

.section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #efeae5;
}

.section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #3a3530;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title::before {
  content: '•';
  color: #a68a64;
  font-size: 24px;
}

.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 15px;
  margin-bottom: 6px;
  color: #5a524a;
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #c4b8a8;
  border-radius: 8px;
  background: #faf8f5;
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  color: #3a3530;
}

.form-input:focus {
  outline: none;
  border-color: #a68a64;
}

.submit-btn {
  width: 100%;
  background: #a68a64;
  color: white;
  border: none;
  border-radius: 10px;
  padding: 14px;
  font-size: 17px;
  font-weight: 600;
  font-family: 'Noto Serif SC', serif;
  cursor: pointer;
  margin-top: 8px;
}

.tip {
  font-size: 13px;
  color: #8c7b6b;
  margin-top: 8px;
  line-height: 1.5;
}

</style>