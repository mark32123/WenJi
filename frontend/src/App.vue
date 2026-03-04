<!-- App.vue -->
<template>
  <div id="app">
    <transition
      :name="transitionName"
      mode="out-in"
    >
      <router-view />
    </transition>
  </div>
</template>

<script>
export default {
  data() {
    return {
      transitionName: 'slide-right'
    };
  },
  watch: {
     $route(to, from) {
      // 简单判断：路径深度增加 → 进入；减少 → 返回
      const toDepth = to.path.split('/').length;
      const fromDepth = from.path.split('/').length;
      
      // 如果是从引导页跳转到登录页，使用淡入淡出效果
      if (from.path === '/guide' && to.path === '/login') {
        this.transitionName = 'fade';
      } else {
        this.transitionName = toDepth > fromDepth ? 'slide-right' : 'slide-left';
      }
    }
  }
};
</script>

<style>
/* 淡入淡出过渡效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 1s ease-in-out;
}

.fade-enter-from {
  opacity: 0;
}

.fade-leave-to {
  opacity: 0;
}
</style>