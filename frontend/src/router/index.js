import { createRouter, createWebHistory } from 'vue-router'
import LoginRegisterVue from '@/views/LoginRegister.vue'
import HomeViewVue from '@/views/HomeView.vue'
import MapViewVue from '@/views/MapView.vue'
import ProfileViewVue from '@/views/ProfileView.vue'
import UserInfoVue from '@/views/UserInfo.vue'
import AccountSecurityVue from '@/views/AccountSecurity.vue'
import ModifyContactAndPasswordVue from '@/views/ModifyContactAndPassword.vue'
import AIGuideVue from '@/views/AIGuide.vue'
import HistoryQA from '@/views/HistoryQA.vue'
import CeramicArVue from '@/views/CeramicAr.vue'

import BeijingVue from '@/views/Beijing.vue'

const routes = [
  { path: '/', redirect: '/guide' },
  { path: '/guide', component: BeijingVue, meta: { public: true } },
  { path: '/login', component: LoginRegisterVue, meta: { public: true } },
  { path: '/register', component: LoginRegisterVue, meta: { public: true } },
  { path: '/user/home', component: HomeViewVue, meta: { public: true } },
  { path: '/user/map', component: MapViewVue, meta: { requiresAuth: true } },
  { path: '/user/ceramicAr', component: CeramicArVue, meta: { public: true } },
  { path: '/user/profile', component: ProfileViewVue, meta: { requiresAuth: true } },
  { path: '/user/userinfo', component: UserInfoVue, meta: { requiresAuth: true } },
  { path: '/user/accountsecurity', component: AccountSecurityVue, meta: { requiresAuth: true } },
  { path: '/user/modifycontactandpassword', component: ModifyContactAndPasswordVue, meta: { requiresAuth: true } },
  { path: '/user/aiguide', component: AIGuideVue, meta: { requiresAuth: true } },
  { path: '/user/history', component: HistoryQA, meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token');

    if (token) {
      next();
    } else {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
    }
  } else {
    next();
  }
});

export default router