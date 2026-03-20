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
  background: linear-gradient(135deg, #f5f5f0 0%, #ebe8e0 50%, #e8e5dd 100%);
  background-size: 400% 400%;
  animation: backgroundFlow 30s ease infinite;
  font-family: 'Noto Serif SC', serif;
  color: #3a3530;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 0;
  margin: 0;
  width: 100%;
}

@keyframes backgroundFlow {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.layout-main {
  flex: 1;
  padding: 20px;
  max-width: 100%;
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
  position: relative;
}

/* 水墨画风格的动态背景 */
.layout-main::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(ellipse at 20% 30%, rgba(64, 64, 64, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 70%, rgba(96, 96, 96, 0.06) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 50%, rgba(80, 80, 80, 0.04) 0%, transparent 60%);
  animation: inkFlow 20s ease-in-out infinite;
  pointer-events: none;
  z-index: 0;
}

@keyframes inkFlow {
  0%, 100% {
    opacity: 0.6;
    transform: scale(1) translate(0, 0);
  }
  25% {
    opacity: 0.8;
    transform: scale(1.1) translate(2%, 3%);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.05) translate(-3%, 2%);
  }
  75% {
    opacity: 0.9;
    transform: scale(1.08) translate(1%, -2%);
  }
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