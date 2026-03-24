<template>
  <div class="welcome-screen fixed inset-0 overflow-hidden">
    <div class="background-layer">
      <div class="bg-image" />
      <div class="ink-overlay" />
    </div>
    
    <div class="content-layer flex flex-col items-center justify-center">
      <div class="calligraphy-container">
        <svg
          class="calligraphy-svg"
          viewBox="0 0 200 400"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            <linearGradient id="inkGradient" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="#2D2D2D"/>
              <stop offset="50%" stop-color="#1A1A1A"/>
              <stop offset="100%" stop-color="#333333"/>
            </linearGradient>
            
            <filter id="ink-texture">
              <feTurbulence type="fractalNoise" baseFrequency="0.04" numOctaves="3" result="noise"/>
              <feDisplacementMap in="SourceGraphic" in2="noise" scale="2" xChannelSelector="R" yChannelSelector="G"/>
            </filter>
            
            <filter id="brush-effect">
              <feMorphology operator="dilate" radius="0.5" result="dilated"/>
              <feGaussianBlur in="dilated" stdDeviation="0.3"/>
            </filter>
          </defs>
          
          <g class="character-group" filter="url(#brush-effect)">
            <text
              class="character wen"
              x="100"
              y="140"
              text-anchor="middle"
              font-size="100"
              font-family="Ma Shan Zheng, STKaiti, Kaiti SC, SimSun, serif"
              fill="none"
              stroke="url(#inkGradient)"
              stroke-width="2.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            >文</text>
            
            <text
              class="character ji"
              x="100"
              y="280"
              text-anchor="middle"
              font-size="100"
              font-family="Ma Shan Zheng, STKaiti, Kaiti SC, SimSun, serif"
              fill="none"
              stroke="url(#inkGradient)"
              stroke-width="2.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            >迹</text>
          </g>
        </svg>
        
        <div class="subtitle-container">
          <span class="subtitle-text">探寻千年文明</span>
          <span class="subtitle-dot">·</span>
          <span class="subtitle-text">触摸历史脉搏</span>
        </div>
      </div>
      
      <div class="loading-indicator">
        <div class="loading-line">
          <div class="loading-progress" :style="{ width: `${progress}%` }" />
        </div>
      </div>
    </div>
    
    <div class="seal-button" @click="openGithub">
      <div class="seal-border">
        <span class="seal-text">源码</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const progress = ref(0)

const openGithub = () => {
  window.open('https://github.com/bingege-0729/WenJi', '_blank')
}

let animationFrame = null
let startTime = null
const totalDuration = 3500

const animate = (timestamp) => {
  if (!startTime) startTime = timestamp
  const elapsed = timestamp - startTime
  progress.value = Math.min((elapsed / totalDuration) * 100, 100)
  
  if (elapsed < totalDuration) {
    animationFrame = requestAnimationFrame(animate)
  }
}

let redirectTimeout = null

onMounted(() => {
  animationFrame = requestAnimationFrame(animate)
  
  redirectTimeout = setTimeout(() => {
    localStorage.setItem('wenji_visited', 'true')
    router.replace('/')
  }, totalDuration + 500)
})

onUnmounted(() => {
  if (animationFrame) cancelAnimationFrame(animationFrame)
  if (redirectTimeout) clearTimeout(redirectTimeout)
})
</script>

<style scoped>
.welcome-screen {
  background: linear-gradient(
    135deg,
    #F5F2EB 0%,
    #E8E4DB 25%,
    #DDD8CC 50%,
    #E8E4DB 75%,
    #F5F2EB 100%
  );
}

.background-layer {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-image {
  position: absolute;
  inset: -20px;
  background: 
    radial-gradient(ellipse at 30% 20%, rgba(168, 218, 220, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 80%, rgba(45, 64, 89, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 50%, rgba(139, 154, 156, 0.1) 0%, transparent 60%);
  animation: ken-burns 20s ease-in-out infinite alternate;
}

@keyframes ken-burns {
  0% {
    transform: scale(1) translate(0, 0);
  }
  100% {
    transform: scale(1.1) translate(-2%, 2%);
  }
}

.ink-overlay {
  position: absolute;
  inset: 0;
  background: 
    url("data:image/svg+xml,%3Csvg viewBox='0 0 400 400' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
  opacity: 0.03;
  pointer-events: none;
}

.content-layer {
  position: relative;
  z-index: 10;
  min-height: 100vh;
  min-height: 100dvh;
}

.calligraphy-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;
}

.calligraphy-svg {
  width: 180px;
  height: 360px;
  filter: drop-shadow(0 4px 20px rgba(26, 26, 26, 0.15));
}

.character {
  stroke-dasharray: 800;
  stroke-dashoffset: 800;
}

.character.wen {
  animation: draw-character 1.8s ease-out forwards;
}

.character.ji {
  animation: draw-character 1.8s ease-out 0.7s forwards;
}

@keyframes draw-character {
  0% {
    stroke-dashoffset: 800;
    fill: transparent;
  }
  70% {
    stroke-dashoffset: 0;
    fill: transparent;
  }
  100% {
    stroke-dashoffset: 0;
    fill: url(#inkGradient);
  }
}

.character-group {
  opacity: 0;
  animation: fade-in 0.5s ease-out forwards;
}

@keyframes fade-in {
  to {
    opacity: 1;
  }
}

.subtitle-container {
  display: flex;
  align-items: center;
  gap: 12px;
  opacity: 0;
  animation: subtitle-fade-in 1s ease-out 2.2s forwards;
}

@keyframes subtitle-fade-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 0.7;
    transform: translateY(0);
  }
}

.subtitle-text {
  font-size: 14px;
  color: #5D4E37;
  letter-spacing: 0.3em;
  font-family: 'Noto Serif SC', 'STSong', 'SimSun', serif;
}

.subtitle-dot {
  font-size: 12px;
  color: #8B7355;
}

.loading-indicator {
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  opacity: 0;
  animation: fade-in 0.5s ease-out 2.5s forwards;
}

.loading-line {
  width: 120px;
  height: 2px;
  background: rgba(45, 64, 89, 0.1);
  border-radius: 1px;
  overflow: hidden;
}

.loading-progress {
  height: 100%;
  background: linear-gradient(90deg, #8B7355, #5D4E37);
  border-radius: 1px;
  transition: width 0.1s linear;
}

.seal-button {
  position: absolute;
  bottom: 60px;
  right: 30px;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
  opacity: 0;
  animation: seal-fade-in 0.8s ease-out 2.8s forwards;
  z-index: 20;
}

@keyframes seal-fade-in {
  to {
    opacity: 1;
  }
}

.seal-border {
  width: 56px;
  height: 56px;
  border: 2px solid #C43C3C;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(196, 60, 60, 0.08);
  transition: all 0.3s ease;
  transform: rotate(-5deg);
}

.seal-button:hover .seal-border {
  background: rgba(196, 60, 60, 0.15);
  transform: rotate(0deg) scale(1.05);
  box-shadow: 0 4px 12px rgba(196, 60, 60, 0.2);
}

.seal-text {
  font-size: 14px;
  color: #C43C3C;
  font-family: 'Ma Shan Zheng', 'STKaiti', 'Kaiti SC', serif;
  letter-spacing: 0.1em;
}

@media (max-width: 428px) {
  .calligraphy-svg {
    width: 140px;
    height: 280px;
  }
  
  .subtitle-text {
    font-size: 12px;
    letter-spacing: 0.2em;
  }
  
  .subtitle-container {
    gap: 8px;
  }
  
  .seal-button {
    bottom: 50px;
    right: 20px;
  }
  
  .seal-border {
    width: 48px;
    height: 48px;
  }
  
  .seal-text {
    font-size: 12px;
  }
  
  .loading-indicator {
    bottom: 60px;
  }
  
  .loading-line {
    width: 100px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .bg-image {
    animation: none;
  }
  
  .character.wen,
  .character.ji {
    animation: none;
    stroke-dashoffset: 0;
    fill: url(#inkGradient);
  }
  
  .character-group,
  .subtitle-container,
  .seal-button,
  .loading-indicator {
    animation: none;
    opacity: 1;
  }
}
</style>
