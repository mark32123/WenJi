<script setup>
import { useRouter } from 'vue-router'

import { ref ,reactive, onMounted} from 'vue'


const router = useRouter();
const registerFormRef=ref();
const loginFormRef=ref();
const isRegister= ref(false);
//定义数据模型
const registerData=ref({
    username:'',
    password:'',
    phone:'',
    rePassword:''
})

//根据当前URL自动设置模式
onMounted(()=>{
    isRegister.value = router.path === '/register'
})
// 切换登录/注册模式
const toggleMode = () => {
    isRegister.value = !isRegister.value;
    router.push(isRegister.value ? '/register' : '/login');
}


//校验密码函数
const checkRePassword=(rule,value,callback)=>{
    if(value===''){
        callback(new Error('请再次输入密码'))
    }
    else if(value!==registerData.value.password){
        callback(new Error('请确保两次输入密码相同'));
    }else{
        callback()
    }
}

//定义登录校验规则
const loginRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 16, message: '用户名长度为1-16个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
})

//定义注册校验规则
const registerRules = reactive({
  username: loginRules.username,
  password: loginRules.password,
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' } // 建议加格式校验
  ],
  rePassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: checkRePassword, trigger: 'blur' }
  ]
})



import {userRegisterService,userLoginService}from '@/api/user.js'
import { ElMessage } from 'element-plus';
//调用后台接口完成注册
//异步方法
const register=async()=>{
  //   try {
     // 触发表单校验
    await registerFormRef.value.validate()
    
    let result = await userRegisterService(registerData.value);
    if (result.code === 1) {
      ElMessage.success(result.msg || '注册成功');
      isRegister.value = false; // 注册成功后切换回登录
    } else {
      ElMessage.error(result.msg || '注册失败');
      console.log('请求失败，原因：', result.msg);
    }
  // } catch (error) {
  //   // 校验失败时会 reject，可不处理，Element Plus 已显示错误
  //   return;
  // }
  return result;
}

//绑定数据，复用
//表单数据校验
//登录函数
// const login=async()=>{
//   //调用接口，完成登录
//   try{
//     //触发表单校验
//     await loginFormRef.value.validate();
//     const result = await userLoginService({
//       username: registerData.value.username,
//       password: registerData.value.password
//     })
//       // 不传 phone/rePassword
//       if(result.code===1){
//         ElMessage.success(result.msg?result.msg:'登录成功');
//         // //登录成功，跳转到首页
//         localStorage.setItem('token',result.data.token);
//         router.push('/user/home');
//     }else{
//         ElMessage.error('登录失败');
//         console.log('请求失败，原因：',result.msg);
//     }
//   }catch(err){
//       // 校验失败时会 reject，可不处理，Element Plus 已显示错误
//   }
    
    
// }
// 在你的 login 函数中
// 在你的 login 函数中
// 在你的 login 函数中
const login=async()=>{
  //调用接口，完成登录
  try{
    //触发表单校验
    await loginFormRef.value.validate();
    const result = await userLoginService({
      username: registerData.value.username,
      password: registerData.value.password
    })
    
    console.log('Login response:', result); // 这行可以保留用于调试
    
    // 不传 phone/rePassword
    if(result.code===1){
      ElMessage.success(result.msg?result.msg:'登录成功');
      
      // 从响应中正确提取 token - 后端返回的是 accessToken
      const token = result.data?.accessToken;
      
      if(token && typeof token === 'string' && token.trim() !== '') {
        localStorage.setItem('token', token);
        console.log('Token set successfully:', token.substring(0, 20) + '...');
        router.push('/user/home');
      } else {
        console.error('No valid token found in response');
        ElMessage.error('登录失败：服务器未返回有效认证令牌');
        return;
      }
    } else {
      ElMessage.error('登录失败');
      console.log('请求失败，原因：',result.msg);
    }
  } catch(err) {
    // 校验失败时会 reject，可不处理，Element Plus 已显示错误
    console.error('Login error:', err);
  }
};
</script>


<template>
 <div class="login-register-container">
    <div class="app-name">文迹</div>

    <!-- 登录表单 -->
    <el-form v-if="!isRegister"  ref="loginFormRef" :model="registerData" :rules="loginRules" class="form-container" label-position="top" hide-required-asterisk>
      <el-form-item prop="username" label="用户名">
        <el-input
          v-model="registerData.username"
          placeholder="请输入用户名"
          maxlength="20"
          clearable
        />
      </el-form-item>

      <el-form-item prop="password" label="密码">
        <el-input
          v-model="registerData.password"
          type="password"
          placeholder="请输入密码"
          show-password
        />
      </el-form-item>

      <el-button type="primary" class="submit-btn" @click="login" >登录 </el-button>

      <div @click="toggleMode" class="toggle-link">切换到注册</div>
    </el-form>

    <!-- 注册表单 -->
    <el-form ref="registerFormRef" v-else :model="registerData" :rules="registerRules" class="form-container" label-position="top" hide-required-asterisk>
      <el-form-item prop="username" label="用户名">
        <el-input
          v-model="registerData.username"
          placeholder="请输入用户名"
          maxlength="20"
          clearable
        />
      </el-form-item>

      <el-form-item prop="password" label="密码">
        <el-input
          v-model="registerData.password"
          type="password"
          placeholder="请输入密码"
          show-password
        />
      </el-form-item>

      <el-form-item prop="rePassword" label="确认密码">
        <el-input
          v-model="registerData.rePassword"
          type="password"
          placeholder="请再次输入密码"
          show-password
        />
      </el-form-item>

      <el-form-item prop="phone" label="手机号">
        <el-input
          v-model="registerData.phone"
          type="tel"
          placeholder="用于账号安全验证"
          maxlength="11"
        />
      </el-form-item>

      <el-button
        type="primary"
        class="submit-btn"
        @click="register"
      >注册</el-button>

      <div @click="toggleMode" class="toggle-link">已有账号？去登录</div>
    </el-form>

    <div class="divider"></div>
    <div class="footer-quote">步履所至，皆成文迹</div>
  </div>
</template>

<style scoped>

.login-register-container {
  background-color: #F8F5F0;
  font-family: "Noto Serif SC", serif;
  color: #3A3530;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px;
}

.app-name {
  font-weight: 700;
  font-size: 32px;
  letter-spacing: 2px;
  text-align: center;
  position: relative;
  margin-bottom: 30px;
}

.app-name::after {
  content: "迹";
  position: absolute;
  top: -6px;
  right: -24px;
  font-size: 12px;
  color: #A68A64;
  font-family: "Kaiti SC", serif;
  transform: rotate(8deg);
  opacity: 0.8;
}

.form-container {
  width: 100%;
  max-width: 300px;
}

/* 覆盖 el-form-item 样式 */
:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-form-item__label) {
  font-size: 14px;
  color: #8C7B6B;
  padding: 0 0 6px 0;
  line-height: 1;
}

/* 覆盖 input 样式：去掉默认边框，改为底部线 */
:deep(.el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  border: none !important;
  padding: 0 !important;
}

:deep(.el-input__inner) {
  border: none;
  border-bottom: 1px solid #C4B8A8;
  border-radius: 0;
  padding: 12px 0;
  font-size: 16px;
  color: #3A3530;
  background: transparent;
}

:deep(.el-input__inner:focus) {
  border-bottom-color: #A68A64;
  outline: none;
}

/* 按钮样式覆盖 */
.submit-btn {
  background-color: #A68A64 !important;
  border-color: #A68A64 !important;
  color: white;
  width: 100%;
  height: 52px;
  font-size: 18px;
  font-weight: 500;
  font-family: "Noto Serif SC", serif;
  border-radius: 8px;
  margin-top: 10px;
}

.submit-btn:hover {
  background-color: #957a5a !important;
  border-color: #957a5a !important;
}

.toggle-link {
  margin-top: 20px;
  color: #8C7B6B;
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
  text-align: center;
}

.toggle-link:hover {
  color: #A68A64;
}

.divider {
  width: 80%;
  height: 0.5px;
  background-color: #C4B8A8;
  margin: 12px auto;
}

.footer-quote {
  font-family: "Kaiti SC", "霞鹜文楷 GB", serif;
  font-size: 12px;
  color: #C4B8A8;
  margin: 90px;
  text-align: center;
}

/* 错误提示样式（可选优化） */
:deep(.el-form-item__error) {
  color: #A68A64;
  font-size: 12px;
  margin-top: 4px;
}

/* ========== 去掉 el-input 聚焦时的蓝框和边框动画 ========== */

:deep(.el-input__inner) {
  border: none;
  border-bottom: 1px solid #C4B8A8;
  background: transparent;
  outline: none !important; /* 关键：移除默认 focus outline */
  box-shadow: none !important; /* 移除默认阴影 */
}

/* 确保聚焦时不出现蓝色边框 */
:deep(.el-input__inner:focus) {
  border-bottom-color: #A68A64;
  outline: none !important;
  box-shadow: none !important;
}
</style>