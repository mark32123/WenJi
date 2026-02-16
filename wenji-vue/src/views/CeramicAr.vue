<template>
    <Layout title="陶瓷3D鉴赏" :showBack="true">
      <div class="page-wrapper">
        <div class="ceramic-ar-container">
          <!-- 搜索与分类区 -->
          <section class="search-section">
            <div class="search-box">
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索（青花瓷/面具/粉彩瓷）"
                class="search-input"
              />
              <button @click="selectModel" class="search-btn">
                查看模型
              </button>
            </div>

            <div class="category-buttons">
              <button
                v-for="item in ceramicCategories"
                :key="item.type"
                @click="selectCategory(item)"
                class="category-btn"
                :class="{ active: activeModel?.type === item.type }"
              >
                {{ item.name }}
              </button>
            </div>
          </section>

          <!-- 模型展示区 -->
          <section
            v-if="activeModel"
            class="model-display-section fade-slide"
            enter-active-class="fade-slide-enter-active"
            leave-active-class="fade-slide-leave-active"
            enter-from-class="fade-slide-enter-from"
            leave-to-class="fade-slide-leave-to"
          >
            <div class="model-info-card">
              <div class="model-header">
                <h2>{{ activeModel.name }} 3D模型</h2>
                <button @click="closeModel" class="close-btn">×</button>
              </div>

              <div class="model-content">
                <!-- 3D模型容器 - 核心展示区，无额外遮挡 -->
                <div class="model-viewer-wrapper">
                  <ModelViewer
                    :model-url="activeModel.model"
                    class="model-viewer-container"
                  />
                </div>

                <!-- 模型介绍 - 固定底部，不挤压模型 -->
                <div class="model-description">
                  <h3>{{ activeModel.name }}介绍</h3>
                  <p>{{ activeModel.description }}</p>
                </div>
              </div>
            </div>
          </section>

          <!-- 初始引导区 -->
          <section
            v-if="!activeModel"
            class="guide-section fade-slide"
            enter-active-class="fade-slide-enter-active"
            leave-active-class="fade-slide-leave-active"
            enter-from-class="fade-slide-enter-from"
            leave-to-class="fade-slide-leave-to"
          >
            <div class="guide-card">
              <h2>欢迎体验陶瓷3D鉴赏</h2>
              <p>点击下方分类或搜索查看陶瓷3D模型</p>

              <div class="model-grid">
                <div
                  v-for="item in ceramicCategories"
                  :key="item.type"
                  class="model-card"
                  @click="selectCategory(item)"
                >
                  <div class="model-preview">
                    <img
                      :src="item.cover"
                      :alt="item.name"
                      class="preview-image"
                      @error="handleGuideImgError($event, item.type)"
                    />
                    <div class="preview-overlay">
                      <span>点击查看3D模型</span>
                    </div>
                  </div>
                  <div class="model-info">
                    <h3>{{ item.name }}</h3>
                    <p>{{ item.previewDesc }}</p>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </Layout>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ModelViewer from '../components/ModelViewer.vue'

import Layout from '@/components/Layout.vue'
// 初始化路由实例
const router = useRouter()

const ceramicCategories = ref([
  {
    type: '青花瓷',
    name: '青花瓷',
    cover: '/images/qinghuaci.jpg',
    model: '/models/qinghuaci.glb',
    description: '青花瓷是中国瓷器的主流品种之一，属釉下彩瓷。青花瓷是用含氧化钴的钴矿为原料，在陶瓷坯体上描绘纹饰，再罩上一层透明釉，经高温还原焰一次烧成。钴料烧成后呈蓝色，具有着色力强、发色鲜艳、烧成率高、呈色稳定的特点。',
    previewDesc: '经典青花瓷模型，展现传统纹饰',
  },
  {
    type: '青花瓷面具',
    name: '青花瓷面具',
    cover: '/images/qinghuacimianju.jpg',
    model: '/models/qinghuacimianju.glb',
    description: '青花瓷面具是青花瓷工艺与传统面具造型结合的特色陶瓷工艺品，以钴料蓝白纹饰为核心，融合民族图腾与瑞兽纹样，保留釉下彩高温烧成工艺，兼具青花瓷的典雅与面具的艺术表现力。',
    previewDesc: '融合传统青花与面具造型的3D模型'
  },
  {
    type: '粉彩瓷',
    name: '粉彩瓷',
    cover: '/images/fencaici.jpg',
    model: '/models/fencaici.glb',
    description: '粉彩瓷是景德镇窑在五彩瓷基础上，受珐琅彩制作工艺的影响而创制的一种釉上彩新品种。粉彩瓷的特点是色彩柔和，层次丰富，画面立体感强。粉彩瓷的烧制温度较低，色彩更加丰富多样，是清代瓷器的重要品种。',
    previewDesc: '细腻粉彩瓷模型，展现柔和色彩',
  }
])

const searchKeyword = ref('')
const activeModel = ref(null)

const selectCategory = (item) => {
  activeModel.value = item
  searchKeyword.value = item.type
}

const selectModel = () => {
  if (!searchKeyword.value.trim()) return

  const found = ceramicCategories.value.find(
    item => item.type.includes(searchKeyword.value) ||
            item.name.includes(searchKeyword.value)
  )

  if (found) {
    activeModel.value = found
  } else {
    alert(`未找到"${searchKeyword.value}"相关的模型`)
  }
}

const closeModel = () => {
  activeModel.value = null
  searchKeyword.value = ''
}

const handleGuideImgError = (e, type) => {
  e.target.src = `https://picsum.photos/seed/${type}/300/200`
  e.target.alt = `${type} - 默认图片`
}
</script>

<style scoped>

.search-section {
  margin: 0 auto 1.5rem auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0 0.8rem;
  box-sizing: border-box;
}

.search-box {
  display: flex;
  gap: 8px;
  width: 100%;
}

.search-input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #e5e2dd;
  border-radius: 6px;
  font-size: 0.9rem;
  color: #333;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  outline: none;
  box-sizing: border-box;
}

.search-input:focus {
  border-color: #A68A64;
  box-shadow: 0 2px 4px rgba(166,138,100,0.1);
}

.search-btn {
  padding: 10px 18px;
  background: #A68A64;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  white-space: nowrap;
}

.search-btn:hover {
  opacity: 0.95;
  transform: translateY(-1px);
}

.category-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.category-btn {
  padding: 6px 16px;
  background: #fff;
  border: 1px solid #e5e2dd;
  border-radius: 16px;
  color: #333;
  cursor: pointer;
  font-size: 0.85rem;
}

.category-btn.active,
.category-btn:hover {
  background: #A68A64;
  color: #fff;
  border-color: #A68A64;
}

/* 模型展示区 */
.model-display-section {
  margin: 1rem auto;
  padding: 0 0.8rem;
  box-sizing: border-box;
}

.model-info-card {
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.model-header {
  padding: 1rem 1.2rem;
  background: #A68A64;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-sizing: border-box;
}

.model-header h2 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.close-btn {
  background: transparent;
  border: none;
  color: #fff;
  font-size: 1.5rem;
  cursor: pointer;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.model-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1.2rem;
  box-sizing: border-box;
  height: calc(100vh - 200px);
}

.model-viewer-wrapper {
  flex: 1;
  position: relative;
  min-height: 300px;
  z-index: 1;
}

.model-viewer-container {
  width: 100%;
  height: 100%;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e5e2dd;
  box-sizing: border-box;
}

.model-description {
  background: #f8f5f0;
  padding: 1rem 1.2rem;
  border-radius: 6px;
  box-sizing: border-box;
  max-height: 120px;
  overflow-y: auto;
}

.model-description h3 {
  margin-top: 0;
  color: #333;
  font-size: 1rem;
  margin-bottom: 0.5rem;
}

.model-description p {
  line-height: 1.6;
  color: #666;
  font-size: 0.85rem;
  text-align: justify;
  margin: 0;
}

/* 引导区 */
.guide-section {
  margin: 0 auto;
  padding: 0 0.8rem;
  box-sizing: border-box;
}

.guide-card {
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 1.2rem 1rem;
  box-sizing: border-box;
  text-align: center;
}

.guide-card h2 {
  color: #333;
  margin: 0 0 0.3rem 0;
  font-size: 1.2rem;
  font-weight: 600;
}

.guide-card p {
  color: #666;
  margin: 0 0 1rem 0;
  font-size: 0.85rem;
}

.model-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
  margin: 1rem 0;
}

.model-card {
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
  cursor: pointer;
  border: 1px solid #e5e2dd;
}

.model-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.model-preview {
  position: relative;
  height: 140px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.model-card:hover .preview-image {
  transform: scale(1.03);
}

.preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(166,138,100,0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.model-card:hover .preview-overlay {
  opacity: 1;
}

.preview-overlay span {
  color: white;
  font-size: 0.9rem;
  font-weight: 500;
}

.model-info {
  padding: 0.8rem 1rem;
  box-sizing: border-box;
  text-align: left;
}

.model-info h3 {
  margin: 0 0 0.3rem 0;
  color: #333;
  font-size: 0.95rem;
  font-weight: 600;
}

.model-info p {
  margin: 0;
  color: #666;
  font-size: 0.8rem;
  line-height: 1.5;
}

/* 大屏适配 */
@media (min-width: 428px) {
  .model-viewer-container {
    height: 45vh;
  }
  .model-preview {
    height: 160px;
  }
}
</style>