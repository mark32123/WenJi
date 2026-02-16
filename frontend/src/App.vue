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
      
      this.transitionName = toDepth > fromDepth ? 'slide-right' : 'slide-left';
    }
  }
};
</script>