import { createRouter, createWebHistory } from 'vue-router'
import LoginRegisterVue from '@/views/LoginRegister.vue'
import MapComponentVue from '@/views/MapComponent.vue'
import ProfileViewVue from '@/views/ProfileView.vue'
import UserInfoVue from '@/views/UserInfo.vue'
import AccountSecurityVue from '@/views/AccountSecurity.vue'
import ModifyContactAndPasswordVue from '@/views/ModifyContactAndPassword.vue'
import AIGuideVue from '@/views/AIGuide.vue'
import HistoryQA from '@/views/HistoryQA.vue'
import CeramicArVue from '@/views/CeramicAr.vue'
import BeijingVue from '@/views/Beijing.vue' 
import ShopViewVue from '@/views/ShopView.vue'
import AllAchievementsViewVue from '@/views/AllAchievementsView.vue'
import AllBadgesViewVue from '@/views/AllBadgesView.vue'

const routes = [
  { path: '/', redirect: '/guide' },           
  { path: '/guide', component: BeijingVue },
  { path: '/login', component: LoginRegisterVue },
  { path: '/register', component: LoginRegisterVue },

  { path: '/user/map', component: MapComponentVue, meta: { requiresAuth: true } },
  { path: '/user/profile',component:ProfileViewVue , meta:{requiresAuth:true}},
  { path: '/user/userinfo',component:UserInfoVue , meta:{requiresAuth:true}},
  { path: '/user/accountsecurity',component:AccountSecurityVue , meta:{requiresAuth:true}},
  { path: '/user/modifycontactandpassword',component:ModifyContactAndPasswordVue , meta:{requiresAuth:true}},
  { path: '/user/aiguide',component:AIGuideVue , meta:{requiresAuth:true}},
  { path: '/user/history',component:HistoryQA , meta:{requiresAuth:true}},
  { path: '/user/ceramicAr', component: CeramicArVue, meta:{requiresAuth:true} },
  { path: '/user/shop', component: ShopViewVue, meta:{requiresAuth:true} },
  { path: '/user/all-achievements', component: AllAchievementsViewVue, meta:{requiresAuth:true} },
  { path: '/user/all-badges', component: AllBadgesViewVue, meta:{requiresAuth:true} }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})
/*
// 路由守卫 - 检查用户是否已登录
router.beforeEach((to, from, next) => {
  // 检查目标路由是否需要认证
  if (to.meta.requiresAuth) {
    // 检查是否存在 token
    const token = localStorage.getItem('token');
    
    if (token) {
      // Token 存在，允许访问
      next();
    } else {
      // Token 不存在，重定向到登录页
      next('/login');
    }
  } else {
    // 不需要认证的路由（如登录页），直接允许访问
    next();
  }
});
*/
export default router