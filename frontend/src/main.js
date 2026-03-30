import './assets/main.scss'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import { ElMessage } from 'element-plus'
import 'element-plus/es/components/message/style/css'

import App from './App.vue'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.config.globalProperties.$message = ElMessage
app.mount('#app')
