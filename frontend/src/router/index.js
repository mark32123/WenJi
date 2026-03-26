import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/welcome',
    name: 'welcome',
    component: () => import('@/views/WelcomeScreen.vue'),
    meta: {
      title: '文迹',
      isSplash: true
    }
  },
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
    meta: {
      title: '山河卷 - 文迹'
    }
  },
  {
    path: '/ar',
    name: 'ar',
    component: () => import('@/views/ArExperienceView.vue'),
    meta: {
      title: 'AR鉴赏 - 文迹'
    }
  },
  {
    path: '/reward',
    name: 'reward',
    component: () => import('@/views/RewardView.vue'),
    meta: {
      title: '得趣 - 文迹'
    }
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: {
      title: '藏经阁 - 文迹',
      requiresAuth: true
    }
  },
  {
    path: '/compiler',
    name: 'compiler',
    component: () => import('@/views/TargetCompilerView.vue'),
    meta: {
      title: '管理工具 - 文迹'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '文迹'
  
  const hasVisited = localStorage.getItem('wenji_visited')
  
  if (!hasVisited && to.name !== 'welcome') {
    next({ name: 'welcome' })
    return
  }
  
  if (to.name === 'welcome' && hasVisited) {
    next({ name: 'home' })
    return
  }
  
  next()
})

export default router
