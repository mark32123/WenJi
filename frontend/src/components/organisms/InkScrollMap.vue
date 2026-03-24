<template>
  <div class="ink-scroll-map" ref="containerRef">
    <div class="scroll-container" ref="scrollRef">
      <div class="ink-background" :class="timeOfDay">
        <svg class="sky-svg" viewBox="0 0 3000 500" preserveAspectRatio="xMidYMax slice">
          <defs>
            <linearGradient id="skyGradientDay" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="#87CEEB"/>
              <stop offset="50%" stop-color="#E8D4C4"/>
              <stop offset="100%" stop-color="#D4C4B0"/>
            </linearGradient>
            
            <linearGradient id="skyGradientSunset" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="#FF6B6B"/>
              <stop offset="30%" stop-color="#FFA07A"/>
              <stop offset="60%" stop-color="#FFD93D"/>
              <stop offset="100%" stop-color="#D4C4B0"/>
            </linearGradient>
            
            <linearGradient id="skyGradientNight" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="#1a1a2e"/>
              <stop offset="50%" stop-color="#16213e"/>
              <stop offset="100%" stop-color="#0f3460"/>
            </linearGradient>
            
            <linearGradient id="mountainFar" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="transparent"/>
              <stop offset="60%" stop-color="rgba(139, 154, 156, 0.25)"/>
              <stop offset="100%" stop-color="rgba(139, 154, 156, 0.4)"/>
            </linearGradient>
            
            <linearGradient id="mountainMid" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="transparent"/>
              <stop offset="50%" stop-color="rgba(45, 64, 89, 0.2)"/>
              <stop offset="100%" stop-color="rgba(45, 64, 89, 0.35)"/>
            </linearGradient>
            
            <linearGradient id="mountainNear" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="transparent"/>
              <stop offset="40%" stop-color="rgba(26, 26, 26, 0.15)"/>
              <stop offset="100%" stop-color="rgba(26, 26, 26, 0.25)"/>
            </linearGradient>
            
            <linearGradient id="routeGradient" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="rgba(201, 162, 39, 0.15)"/>
              <stop offset="50%" stop-color="rgba(201, 162, 39, 0.5)"/>
              <stop offset="100%" stop-color="rgba(201, 162, 39, 0.15)"/>
            </linearGradient>
            
            <radialGradient id="sunGlow" cx="50%" cy="50%" r="50%">
              <stop offset="0%" stop-color="rgba(255, 200, 50, 0.8)"/>
              <stop offset="50%" stop-color="rgba(255, 150, 50, 0.3)"/>
              <stop offset="100%" stop-color="transparent"/>
            </radialGradient>
            
            <radialGradient id="moonGlow" cx="50%" cy="50%" r="50%">
              <stop offset="0%" stop-color="rgba(255, 255, 255, 0.9)"/>
              <stop offset="50%" stop-color="rgba(200, 200, 255, 0.3)"/>
              <stop offset="100%" stop-color="transparent"/>
            </radialGradient>
            
            <filter id="blur-far">
              <feGaussianBlur stdDeviation="4"/>
            </filter>
            
            <filter id="blur-mid">
              <feGaussianBlur stdDeviation="2"/>
            </filter>
            
            <filter id="glow">
              <feGaussianBlur stdDeviation="4" result="coloredBlur"/>
              <feMerge>
                <feMergeNode in="coloredBlur"/>
                <feMergeNode in="SourceGraphic"/>
              </feMerge>
            </filter>
          </defs>
          
          <rect width="3000" height="500" :fill="skyGradientUrl"/>
          
          <circle v-if="timeOfDay === 'day'" class="sun" cx="2500" cy="80" r="40" fill="url(#sunGlow)"/>
          <circle v-if="timeOfDay === 'night'" class="moon" cx="500" cy="60" r="30" fill="url(#moonGlow)"/>
          
          <g v-if="timeOfDay === 'night'" class="stars">
            <circle v-for="star in stars" :key="star.id" 
              class="star" 
              :cx="star.x" 
              :cy="star.y" 
              :r="star.size"
              :style="{ animationDelay: star.delay + 's' }"
            />
          </g>
          
          <g class="clouds" :class="{ 'night-clouds': timeOfDay === 'night' }">
            <ellipse class="cloud cloud-1" cx="200" cy="80" rx="80" ry="35" />
            <ellipse class="cloud cloud-2" cx="600" cy="60" rx="100" ry="40" />
            <ellipse class="cloud cloud-3" cx="1000" cy="90" rx="90" ry="38" />
            <ellipse class="cloud cloud-4" cx="1400" cy="70" rx="110" ry="42" />
            <ellipse class="cloud cloud-5" cx="1800" cy="85" rx="95" ry="36" />
            <ellipse class="cloud cloud-6" cx="2200" cy="55" rx="85" ry="32" />
            <ellipse class="cloud cloud-7" cx="2600" cy="75" rx="105" ry="40" />
          </g>
          
          <g class="birds" v-if="timeOfDay !== 'night'">
            <path v-for="bird in birds" :key="bird.id"
              class="bird" 
              :d="bird.path"
              :style="{ transform: `translateX(${bird.offset}px)`, animationDelay: bird.delay + 's' }"
            />
          </g>
          
          <path class="mountain-far" filter="url(#blur-far)"
            d="M0,500 L0,180 Q100,160 200,170 Q350,130 500,160 Q650,110 800,140 Q950,100 1100,130 Q1250,80 1400,120 Q1550,70 1700,110 Q1850,60 2000,100 Q2150,50 2300,90 Q2450,40 2600,80 Q2750,30 2900,70 Q2950,60 3000,80 L3000,500 Z"
            fill="url(#mountainFar)"/>
          
          <path class="mountain-mid" filter="url(#blur-mid)"
            d="M0,500 L0,240 Q150,220 300,240 Q450,190 600,230 Q750,170 900,210 Q1050,160 1200,200 Q1350,150 1500,190 Q1650,140 1800,180 Q1950,130 2100,170 Q2250,120 2400,160 Q2550,110 2700,150 Q2850,100 3000,140 L3000,500 Z"
            fill="url(#mountainMid)"/>
          
          <path class="mountain-near"
            d="M0,500 L0,300 Q100,290 200,300 Q350,260 500,290 Q650,250 800,280 Q950,240 1100,270 Q1250,230 1400,260 Q1550,220 1700,250 Q1850,210 2000,240 Q2150,200 2300,230 Q2450,190 2600,220 Q2750,180 2900,210 Q2950,200 3000,220 L3000,500 Z"
            fill="url(#mountainNear)"/>
          
          <path class="route-path" 
            d="M50,380 Q200,350 400,370 Q600,330 800,360 Q1000,320 1200,350 Q1400,310 1600,340 Q1800,300 2000,330 Q2200,290 2400,320 Q2600,280 2800,310 Q2900,300 2950,320"
            fill="none" 
            stroke="url(#routeGradient)" 
            stroke-width="4"
            stroke-dasharray="15,8"
            stroke-linecap="round"/>
        </svg>
        
        <div class="mist-layer" />
        
        <div class="river-layer">
          <svg viewBox="0 0 3000 80" preserveAspectRatio="none">
            <path class="river-path"
              d="M0,40 Q250,30 500,45 Q750,55 1000,40 Q1250,25 1500,45 Q1750,60 2000,40 Q2250,25 2500,45 Q2750,60 3000,40"
              fill="none"
              stroke="rgba(100, 149, 237, 0.2)"
              stroke-width="3"/>
          </svg>
        </div>
        
        <div v-if="weather === 'rain'" class="rain-container">
          <div v-for="drop in rainDrops" :key="drop.id" 
            class="rain-drop" 
            :style="{ left: drop.x + '%', animationDelay: drop.delay + 's' }"
          />
        </div>
        
        <div v-if="weather === 'snow'" class="snow-container">
          <div v-for="flake in snowFlakes" :key="flake.id" 
            class="snow-flake" 
            :style="{ left: flake.x + '%', animationDelay: flake.delay + 's', animationDuration: flake.duration + 's' }"
          />
        </div>
      </div>
      
      <div class="route-labels">
        <div class="route-label start">
          <span class="label-text font-serif">长安</span>
          <span class="label-sub">起点</span>
        </div>
        <div class="route-label mid">
          <span class="label-text font-serif">洛阳</span>
          <span class="label-sub">古都</span>
        </div>
        <div class="route-label end">
          <span class="label-text font-serif">西域</span>
          <span class="label-sub">远方</span>
        </div>
      </div>
      
      <div class="nodes-container">
        <div
          v-for="node in nodes"
          :key="node.id"
          class="map-node"
          :class="{ 
            unlocked: node.unlocked, 
            active: activeNodeId === node.id,
            collected: node.collected,
            special: node.special,
            hidden: node.hidden && !node.revealed
          }"
          :style="getNodeStyle(node)"
          @click="handleNodeClick(node)"
        >
          <div class="node-marker" :class="`marker-${node.type || 'default'}`">
            <div class="marker-ring" />
            <div class="marker-outer" />
            <div class="marker-inner">
              <span v-if="node.collected" class="marker-check">✓</span>
              <span v-else-if="node.special" class="marker-icon">{{ node.icon || '★' }}</span>
            </div>
            <div v-if="node.unlocked" class="marker-glow" />
            <div v-if="node.special && !node.collected" class="marker-particles">
              <span v-for="i in 6" :key="i" class="particle" :style="{ '--i': i }" />
            </div>
          </div>
          
          <div class="node-label">
            <span class="label-name font-serif">{{ node.name }}</span>
            <span class="label-dynasty">{{ node.dynasty }}</span>
            <span v-if="node.category" class="label-category">{{ node.category }}</span>
          </div>
          
          <transition name="bookmark">
            <div v-if="activeNodeId === node.id && node.unlocked" class="node-bookmark">
              <div class="bookmark-content">
                <span class="bookmark-title font-serif">{{ node.name }}</span>
                <span class="bookmark-location">{{ node.location }}</span>
                <span v-if="node.museum" class="bookmark-museum">{{ node.museum }}</span>
              </div>
              <div class="bookmark-arrow" />
            </div>
          </transition>
        </div>
      </div>
      
      <transition name="event-fade">
        <div v-if="randomEvent" class="random-event" @click="handleEventClick">
          <div class="event-glow" />
          <div class="event-icon">{{ randomEvent.icon }}</div>
          <div class="event-text">{{ randomEvent.text }}</div>
        </div>
      </transition>
      
      <div class="progress-indicator">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }" />
        </div>
        <div class="progress-info">
          <span class="progress-text">已探索 {{ collectedCount }}/{{ nodes.length }} 处</span>
          <span class="progress-weather" @click="toggleWeather">
            {{ weatherIcon }}
          </span>
          <span class="progress-time" @click="toggleTime">
            {{ timeIcon }}
          </span>
        </div>
      </div>
    </div>
    
    <div class="scroll-hint" v-if="showHint">
      <span class="hint-icon">←</span>
      <span class="hint-text">滑动探索山河</span>
      <span class="hint-icon">→</span>
    </div>
    
    <transition name="slide-up">
      <div v-if="selectedNode" class="node-detail-panel">
        <div class="detail-content">
          <div class="detail-image">
            <img 
              v-if="selectedNode.imageUrl" 
              :src="selectedNode.imageUrl" 
              :alt="selectedNode.name" 
              @error="handleImageError"
              @load="handleImageLoad"
            />
            <div class="image-placeholder" :style="{ display: selectedNode.imageUrl ? 'none' : 'flex' }">
              <span class="placeholder-icon">{{ getCategoryIcon(selectedNode.category) }}</span>
            </div>
          </div>
          <div class="detail-info">
            <div class="info-row">
              <h3 class="detail-name font-serif">{{ selectedNode.name }}</h3>
              <span class="detail-category">{{ selectedNode.category }}</span>
            </div>
            <p class="detail-meta">{{ selectedNode.dynasty }} · {{ selectedNode.location }}</p>
            <p v-if="selectedNode.museum" class="detail-museum">{{ selectedNode.museum }}</p>
          </div>
          <div class="detail-action">
            <InkButton
              v-if="selectedNode.unlocked && !selectedNode.collected"
              type="primary"
              size="sm"
              @click="handleCollect"
            >
              收藏
            </InkButton>
            <span v-else-if="selectedNode.collected" class="collected-badge">已收藏</span>
            <span v-else class="locked-badge">未解锁</span>
          </div>
        </div>
        <button class="close-btn" @click="selectedNode = null">×</button>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import InkButton from '@/components/atoms/InkButton.vue'

const props = defineProps({
  nodes: {
    type: Array,
    default: () => []
  },
  showHint: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['nodeClick', 'nodeCollect', 'scroll', 'eventTrigger'])

const containerRef = ref(null)
const scrollRef = ref(null)
const activeNodeId = ref(null)
const selectedNode = ref(null)
const timeOfDay = ref('day')
const weather = ref('clear')
const randomEvent = ref(null)

const collectedCount = computed(() => props.nodes.filter(n => n.collected).length)
const progressPercent = computed(() => {
  if (props.nodes.length === 0) return 0
  return (collectedCount.value / props.nodes.length) * 100
})

const skyGradientUrl = computed(() => {
  switch (timeOfDay.value) {
    case 'sunset': return 'url(#skyGradientSunset)'
    case 'night': return 'url(#skyGradientNight)'
    default: return 'url(#skyGradientDay)'
  }
})

const weatherIcon = computed(() => {
  switch (weather.value) {
    case 'rain': return '🌧️'
    case 'snow': return '❄️'
    default: return '☀️'
  }
})

const timeIcon = computed(() => {
  switch (timeOfDay.value) {
    case 'sunset': return '🌅'
    case 'night': return '🌙'
    default: return '☀️'
  }
})

const stars = computed(() => {
  return Array.from({ length: 50 }, (_, i) => ({
    id: i,
    x: Math.random() * 3000,
    y: Math.random() * 200,
    size: Math.random() * 2 + 0.5,
    delay: Math.random() * 3
  }))
})

const birds = computed(() => {
  return Array.from({ length: 8 }, (_, i) => ({
    id: i,
    path: `M0,0 Q5,-5 10,0 Q15,-5 20,0`,
    offset: Math.random() * 500,
    delay: Math.random() * 5
  }))
})

const rainDrops = computed(() => {
  return Array.from({ length: 100 }, (_, i) => ({
    id: i,
    x: Math.random() * 100,
    delay: Math.random() * 2
  }))
})

const snowFlakes = computed(() => {
  return Array.from({ length: 50 }, (_, i) => ({
    id: i,
    x: Math.random() * 100,
    delay: Math.random() * 5,
    duration: Math.random() * 3 + 4
  }))
})

const getNodeStyle = (node) => ({
  left: `${node.positionX}%`,
  top: `${node.positionY}%`
})

const getCategoryIcon = (category) => {
  const icons = {
    '陶瓷艺术': '🏺',
    '青铜器': '🔔',
    '书画艺术': '📜',
    '玉器': '💎',
    '金器': '✨',
    '壁画艺术': '🎨',
    '丝织品': '🧵',
    '历史遗迹': '🏛️'
  }
  return icons[category] || '🏺'
}

const handleNodeClick = (node) => {
  if (node.hidden && !node.revealed) return
  
  if (activeNodeId.value === node.id) {
    if (node.unlocked) {
      selectedNode.value = node
    }
  } else {
    activeNodeId.value = node.id
    if (!node.unlocked) {
      selectedNode.value = null
    }
  }
  emit('nodeClick', node)
}

const handleCollect = () => {
  if (selectedNode.value) {
    emit('nodeCollect', selectedNode.value)
    selectedNode.value = { ...selectedNode.value, collected: true }
  }
}

const handleImageError = (e) => {
  e.target.style.display = 'none'
  const placeholder = e.target.parentElement.querySelector('.image-placeholder')
  if (placeholder) {
    placeholder.style.display = 'flex'
  }
}

const handleImageLoad = (e) => {
  e.target.style.display = 'block'
  const placeholder = e.target.parentElement.querySelector('.image-placeholder')
  if (placeholder) {
    placeholder.style.display = 'none'
  }
}

const handleScroll = () => {
  if (scrollRef.value) {
    const scrollLeft = scrollRef.value.scrollLeft
    const maxScroll = scrollRef.value.scrollWidth - scrollRef.value.clientWidth
    const progress = maxScroll > 0 ? scrollLeft / maxScroll : 0
    emit('scroll', { progress, scrollLeft, maxScroll })
    
    if (progress > 0.1) {
      showScrollHint.value = false
    }
    
    if (Math.random() < 0.001 && !randomEvent.value) {
      triggerRandomEvent()
    }
  }
}

const toggleWeather = () => {
  const weathers = ['clear', 'rain', 'snow']
  const currentIndex = weathers.indexOf(weather.value)
  weather.value = weathers[(currentIndex + 1) % weathers.length]
}

const toggleTime = () => {
  const times = ['day', 'sunset', 'night']
  const currentIndex = times.indexOf(timeOfDay.value)
  timeOfDay.value = times[(currentIndex + 1) % times.length]
}

const triggerRandomEvent = () => {
  const events = [
    { icon: '🎁', text: '发现神秘宝箱！', type: 'treasure' },
    { icon: '📜', text: '拾得古籍残卷', type: 'scroll' },
    { icon: '🔮', text: '遇见神秘商人', type: 'merchant' },
    { icon: '⭐', text: '流星划过天际', type: 'meteor' },
    { icon: '🎵', text: '听到悠扬琴声', type: 'music' }
  ]
  randomEvent.value = events[Math.floor(Math.random() * events.length)]
  
  setTimeout(() => {
    randomEvent.value = null
  }, 5000)
}

const handleEventClick = () => {
  if (randomEvent.value) {
    emit('eventTrigger', randomEvent.value)
    randomEvent.value = null
  }
}

const showScrollHint = ref(props.showHint)

onMounted(() => {
  if (scrollRef.value) {
    scrollRef.value.addEventListener('scroll', handleScroll)
  }
  
  setInterval(() => {
    if (Math.random() < 0.1 && !randomEvent.value) {
      triggerRandomEvent()
    }
  }, 30000)
})

onUnmounted(() => {
  if (scrollRef.value) {
    scrollRef.value.removeEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped>
.ink-scroll-map {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #F5E6D3;
}

.scroll-container {
  width: 100%;
  height: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.scroll-container::-webkit-scrollbar {
  display: none;
}

.ink-background {
  position: absolute;
  inset: 0;
  width: 300%;
  pointer-events: none;
  transition: filter 0.5s ease;
}

.ink-background.night {
  filter: brightness(0.6);
}

.sky-svg {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 85%;
}

.sun {
  animation: sun-pulse 4s ease-in-out infinite;
}

@keyframes sun-pulse {
  0%, 100% { transform: scale(1); opacity: 0.9; }
  50% { transform: scale(1.1); opacity: 1; }
}

.moon {
  animation: moon-glow 3s ease-in-out infinite;
}

@keyframes moon-glow {
  0%, 100% { filter: brightness(1); }
  50% { filter: brightness(1.2); }
}

.stars {
  animation: stars-twinkle 2s ease-in-out infinite;
}

.star {
  fill: #fff;
  animation: star-blink 2s ease-in-out infinite;
}

@keyframes star-blink {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

.mountain-far {
  opacity: 0.6;
}

.mountain-mid {
  opacity: 0.8;
}

.mountain-near {
  opacity: 1;
}

.clouds {
  animation: clouds-drift 80s linear infinite;
}

.clouds.night-clouds {
  opacity: 0.3;
}

.cloud {
  fill: rgba(255, 255, 255, 0.5);
}

@keyframes clouds-drift {
  0% { transform: translateX(0); }
  100% { transform: translateX(-300px); }
}

.birds {
  animation: birds-fly 40s linear infinite;
}

.bird {
  stroke: rgba(45, 64, 89, 0.4);
  fill: none;
  stroke-width: 2;
  animation: bird-flap 0.5s ease-in-out infinite;
}

@keyframes birds-fly {
  0% { transform: translateX(0); }
  100% { transform: translateX(800px); }
}

@keyframes bird-flap {
  0%, 100% { d: path("M0,0 Q5,-5 10,0 Q15,-5 20,0"); }
  50% { d: path("M0,0 Q5,5 10,0 Q15,5 20,0"); }
}

.route-path {
  animation: route-dash 4s ease-in-out infinite;
}

@keyframes route-dash {
  0% { stroke-dashoffset: 0; }
  100% { stroke-dashoffset: -46; }
}

.river-layer {
  position: absolute;
  bottom: 8%;
  left: 0;
  width: 300%;
  height: 80px;
}

.river-path {
  stroke-dasharray: 12 6;
  animation: river-flow 20s linear infinite;
}

@keyframes river-flow {
  0% { stroke-dashoffset: 0; }
  100% { stroke-dashoffset: -100; }
}

.mist-layer {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(245, 230, 211, 0.9) 0%,
    transparent 20%,
    transparent 65%,
    rgba(245, 230, 211, 0.5) 100%
  );
}

.rain-container {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.rain-drop {
  position: absolute;
  top: -20px;
  width: 2px;
  height: 20px;
  background: linear-gradient(180deg, transparent, rgba(100, 149, 237, 0.6));
  animation: rain-fall 1s linear infinite;
}

@keyframes rain-fall {
  0% { transform: translateY(0); }
  100% { transform: translateY(100vh); }
}

.snow-container {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.snow-flake {
  position: absolute;
  top: -10px;
  width: 8px;
  height: 8px;
  background: radial-gradient(circle, #fff 0%, transparent 70%);
  border-radius: 50%;
  animation: snow-fall 5s linear infinite;
}

@keyframes snow-fall {
  0% { transform: translateY(0) rotate(0deg); }
  100% { transform: translateY(100vh) rotate(360deg); }
}

.route-labels {
  position: absolute;
  width: 300%;
  height: 100%;
  pointer-events: none;
}

.route-label {
  position: absolute;
  bottom: 20%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.route-label.start {
  left: 2%;
}

.route-label.mid {
  left: 50%;
  transform: translateX(-50%);
}

.route-label.end {
  right: 2%;
}

.label-text {
  font-size: 18px;
  color: rgba(45, 64, 89, 0.7);
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
}

.label-sub {
  font-size: 11px;
  color: rgba(139, 154, 156, 0.7);
}

.nodes-container {
  position: relative;
  width: 300%;
  height: 100%;
  min-height: 320px;
}

.map-node {
  position: absolute;
  transform: translate(-50%, -50%);
  cursor: pointer;
  z-index: 10;
  transition: transform 0.3s ease;
}

.map-node:hover {
  transform: translate(-50%, -50%) scale(1.1);
}

.map-node.hidden {
  opacity: 0;
  pointer-events: none;
}

.node-marker {
  position: relative;
  width: 36px;
  height: 36px;
}

.marker-ring {
  position: absolute;
  inset: -6px;
  border: 2px dashed rgba(201, 162, 39, 0.4);
  border-radius: 50%;
  opacity: 0;
  transition: all 0.3s ease;
}

.map-node.collected .marker-ring {
  opacity: 1;
  animation: ring-rotate 12s linear infinite;
}

@keyframes ring-rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.marker-outer {
  position: absolute;
  inset: 2px;
  border: 2px solid rgba(139, 154, 156, 0.5);
  border-radius: 50%;
  transition: all 0.4s ease;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
}

.marker-inner {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 12px;
  height: 12px;
  transform: translate(-50%, -50%);
  background: rgba(139, 154, 156, 0.4);
  border-radius: 50%;
  transition: all 0.4s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.marker-check {
  font-size: 10px;
  color: #fff;
  font-weight: bold;
}

.marker-icon {
  font-size: 10px;
}

.map-node.unlocked .marker-outer {
  border-color: #C9A227;
  background: rgba(201, 162, 39, 0.2);
}

.map-node.unlocked .marker-inner {
  background: #C9A227;
}

.map-node.collected .marker-outer {
  border-color: #2D4059;
  background: rgba(45, 64, 89, 0.15);
}

.map-node.collected .marker-inner {
  background: #2D4059;
}

.map-node.special .marker-outer {
  border-color: #E74C3C;
  background: rgba(231, 76, 60, 0.15);
  animation: special-pulse 2s ease-in-out infinite;
}

@keyframes special-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(231, 76, 60, 0.4); }
  50% { box-shadow: 0 0 0 10px rgba(231, 76, 60, 0); }
}

.map-node.special .marker-inner {
  background: #E74C3C;
}

.marker-glow {
  position: absolute;
  inset: -8px;
  background: radial-gradient(
    circle,
    rgba(201, 162, 39, 0.4) 0%,
    transparent 70%
  );
  border-radius: 50%;
  animation: pulse 2.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.4);
    opacity: 1;
  }
}

.marker-particles {
  position: absolute;
  inset: -10px;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: #E74C3C;
  border-radius: 50%;
  top: 50%;
  left: 50%;
  animation: particle-float 2s ease-in-out infinite;
  animation-delay: calc(var(--i) * 0.2s);
}

@keyframes particle-float {
  0%, 100% {
    transform: translate(-50%, -50%) rotate(calc(var(--i) * 60deg)) translateX(15px) scale(0);
    opacity: 0;
  }
  50% {
    transform: translate(-50%, -50%) rotate(calc(var(--i) * 60deg)) translateX(25px) scale(1);
    opacity: 1;
  }
}

.map-node.active .marker-outer {
  transform: scale(1.3);
  border-color: #2D4059;
  background: rgba(45, 64, 89, 0.2);
}

.map-node.active .marker-inner {
  transform: translate(-50%, -50%) scale(1.3);
  background: #2D4059;
}

.node-label {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
}

.label-name {
  font-size: 13px;
  color: #2D4059;
  font-weight: 500;
}

.label-dynasty {
  font-size: 10px;
  color: #8B9A9C;
}

.label-category {
  font-size: 9px;
  color: #C9A227;
  padding: 1px 6px;
  background: rgba(201, 162, 39, 0.1);
  border-radius: 8px;
}

.node-bookmark {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-bottom: 14px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 10px;
  box-shadow: 0 4px 24px rgba(45, 64, 89, 0.25);
  border: 1px solid rgba(201, 162, 39, 0.4);
  z-index: 50;
  pointer-events: none;
}

.bookmark-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.bookmark-title {
  font-size: 15px;
  color: #2D4059;
  font-weight: 500;
}

.bookmark-location {
  font-size: 11px;
  color: #8B9A9C;
}

.bookmark-museum {
  font-size: 10px;
  color: #C9A227;
}

.bookmark-arrow {
  position: absolute;
  bottom: -7px;
  left: 50%;
  transform: translateX(-50%);
  width: 14px;
  height: 14px;
  background: rgba(255, 255, 255, 0.98);
  border-right: 1px solid rgba(201, 162, 39, 0.4);
  border-bottom: 1px solid rgba(201, 162, 39, 0.4);
  transform: translateX(-50%) rotate(45deg);
}

.bookmark-enter-active,
.bookmark-leave-active {
  transition: all 0.3s ease;
}

.bookmark-enter-from,
.bookmark-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(10px);
}

.random-event {
  position: absolute;
  top: 20%;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(45, 64, 89, 0.3);
  cursor: pointer;
  z-index: 100;
  animation: event-bounce 0.5s ease;
}

@keyframes event-bounce {
  0% { transform: translateX(-50%) scale(0); }
  50% { transform: translateX(-50%) scale(1.1); }
  100% { transform: translateX(-50%) scale(1); }
}

.event-glow {
  position: absolute;
  inset: -20px;
  background: radial-gradient(circle, rgba(201, 162, 39, 0.3) 0%, transparent 70%);
  animation: event-glow 1.5s ease-in-out infinite;
}

@keyframes event-glow {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.2); opacity: 1; }
}

.event-icon {
  font-size: 32px;
  animation: event-icon-float 2s ease-in-out infinite;
}

@keyframes event-icon-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.event-text {
  font-size: 14px;
  color: #2D4059;
  font-weight: 500;
}

.event-fade-enter-active,
.event-fade-leave-active {
  transition: all 0.5s ease;
}

.event-fade-enter-from,
.event-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) scale(0.8);
}

.progress-indicator {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 14px 24px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  z-index: 20;
}

.progress-bar {
  width: 140px;
  height: 5px;
  background: rgba(45, 64, 89, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #C9A227, #D4AF37);
  border-radius: 3px;
  transition: width 0.4s ease;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-text {
  font-size: 12px;
  color: #8B9A9C;
}

.progress-weather,
.progress-time {
  font-size: 16px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.progress-weather:hover,
.progress-time:hover {
  transform: scale(1.2);
}

.scroll-hint {
  position: absolute;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 20px;
  background: rgba(45, 64, 89, 0.12);
  border-radius: 24px;
  animation: fade-hint 3s ease-in-out infinite;
  z-index: 20;
}

@keyframes fade-hint {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

.hint-icon {
  font-size: 16px;
  color: #8B9A9C;
}

.hint-text {
  font-size: 13px;
  color: #2D4059;
}

.node-detail-panel {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  padding-bottom: calc(env(safe-area-inset-bottom) + 12px);
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.detail-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-image {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  background: rgba(45, 64, 89, 0.05);
  position: relative;
}

.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(201, 162, 39, 0.1);
}

.placeholder-icon {
  font-size: 24px;
}

.detail-info {
  flex: 1;
  min-width: 0;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}

.detail-name {
  font-size: 16px;
  color: #2D4059;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.detail-category {
  font-size: 10px;
  color: #C9A227;
  padding: 2px 6px;
  background: rgba(201, 162, 39, 0.1);
  border-radius: 4px;
  flex-shrink: 0;
}

.detail-meta {
  font-size: 11px;
  color: #8B9A9C;
  margin: 0 0 2px 0;
}

.detail-museum {
  font-size: 10px;
  color: #C9A227;
  margin: 0;
}

.detail-action {
  flex-shrink: 0;
}

.collected-badge,
.locked-badge {
  font-size: 11px;
  padding: 6px 12px;
  border-radius: 16px;
  background: rgba(45, 64, 89, 0.1);
  color: #8B9A9C;
}

.collected-badge {
  background: rgba(45, 64, 89, 0.15);
  color: #2D4059;
}

.close-btn {
  position: absolute;
  top: 8px;
  right: 12px;
  width: 24px;
  height: 24px;
  border: none;
  background: rgba(45, 64, 89, 0.1);
  border-radius: 50%;
  font-size: 16px;
  color: #8B9A9C;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: rgba(45, 64, 89, 0.2);
  color: #2D4059;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.4s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}
</style>
