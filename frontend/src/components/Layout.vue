<!-- src/components/MobileLayout.vue -->
<template>
  <div class="mobile-layout">
    <!-- 头部 -->
    <header v-if="showHeader" class="layout-header">
      <button v-if="showBack" class="back-btn" @click="goBack">
        ←
      </button>
      <h1 v-if="title" class="layout-title">{{ title }}</h1>
      <slot name="header-actions"></slot>
    </header>

    <!-- 主内容区 -->
    <main class="layout-main">
      <slot></slot>
    </main>

    <!-- 底部引文 -->
    <footer v-if="showFooterQuote" class="layout-footer">
      <div class="footer-quote">{{ footerQuote }}</div>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

defineProps({
  showHeader: {
    type: Boolean,
    default: true
  },
  showBack: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  showFooterQuote: {
    type: Boolean,
    default: true
  },
  footerQuote: {
    type: String,
    default: '步履所至，皆成文迹'
  }
})

const router = useRouter()

function goBack() {
  router.go(-1)
}
</script>

<style scoped>
.mobile-layout {
  background-color: #f8f5f0;
  font-family: 'Noto Serif SC', serif;
  color: #3a3530;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 0;
  margin: 0;
  width: 100%;
}

.layout-header {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  box-sizing: border-box;
}

.back-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #a68a64;
  cursor: pointer;
  margin-right: 12px;
  padding: 4px 8px;
  flex-shrink: 0;
}

.layout-title {
  font-size: 18px;
  font-weight: 600;
  flex: 1;
  text-align: center;
  margin: 0;
}

.layout-main {
  flex: 1;
  padding: 20px;
  max-width: 100%;
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

.layout-footer {
  text-align: center;
  padding: 20px 0;
  margin-top: auto;
  width: 100%;
}

.footer-quote {
  color: #c4b8a8;
  font-size: 12px;
  font-family: 'Kaiti SC', serif;
  line-height: 1.5;
  margin: 0;
}
</style>