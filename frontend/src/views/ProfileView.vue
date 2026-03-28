<template>
  <div class="profile-view">
    <div class="profile-header">
      <div class="header-bg-pattern" />
      
      <div class="user-info">
        <div class="avatar-container" @click="handleAvatarClick">
          <div class="avatar-ring" />
          <div class="avatar-inner">
            <img 
              v-if="!authStore.isGuest && userStore.avatar" 
              :src="userStore.avatar" 
              :alt="userStore.username"
              class="avatar-img"
            />
            <div v-else class="avatar-placeholder">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
          </div>
          <div class="level-badge">
            <span class="level-text font-serif">{{ userStore.currentLevel }}</span>
          </div>
        </div>
        
        <div class="user-details">
          <div class="user-primary">
            <h2 class="user-name font-serif">{{ authStore.isGuest ? '游客' : userStore.username }}</h2>
            <div class="user-title">
              <span class="title-icon">📜</span>
              <span class="title-text">{{ getUserTitle }}</span>
            </div>
          </div>
          
          <div class="user-actions">
            <button 
              v-if="authStore.isGuest" 
              class="login-btn"
              @click="handleLogin"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
                <polyline points="10 17 15 12 10 7"/>
                <line x1="15" y1="12" x2="3" y2="12"/>
              </svg>
              <span>点此登录</span>
            </button>
            <template v-else>
              <button 
                class="edit-btn"
                @click="showEditModal = true"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
                <span>编辑</span>
              </button>
              <button 
                class="logout-btn"
                @click="handleLogout"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                  <polyline points="16 17 21 12 16 7"/>
                  <line x1="21" y1="12" x2="9" y2="12"/>
                </svg>
              </button>
            </template>
          </div>
        </div>
      </div>
      
      <div class="stats-row">
        <div class="stat-item">
          <span class="stat-value">{{ userStore.experience }}</span>
          <span class="stat-label">阅历</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <span class="stat-value">{{ userStore.artifactCount }}</span>
          <span class="stat-label">藏品</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <span class="stat-value">{{ userStore.visitedCount }}</span>
          <span class="stat-label">足迹</span>
        </div>
      </div>
      
      <div v-if="userStore.nextLevel" class="level-progress">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }" />
        </div>
        <span class="progress-hint">距离「{{ userStore.nextLevel.name }}」还需 {{ userStore.nextLevel.required }} 阅历</span>
      </div>
      <div v-else class="level-progress max-level">
        <span class="progress-hint">已达最高境界「{{ userStore.currentLevel }}」</span>
      </div>
    </div>
    
    <div class="profile-content">
      <section class="collection-section">
        <div class="section-header">
          <div class="section-title-row">
            <h3 class="section-title font-serif">我的收藏</h3>
            <span class="section-count">{{ collectedCount }}/{{ totalCollections }}</span>
          </div>
        </div>
        
        <div class="collection-tabs">
          <button 
            class="tab-btn" 
            :class="{ active: activeTab === 'poems' }"
            @click="activeTab = 'poems'"
          >
            <span class="tab-icon">📜</span>
            <span>诗词</span>
          </button>
          <button 
            class="tab-btn" 
            :class="{ active: activeTab === 'seals' }"
            @click="activeTab = 'seals'"
          >
            <span class="tab-icon">🔴</span>
            <span>印章</span>
          </button>
        </div>
        
        <div v-if="activeTab === 'poems'" class="poem-section">
          <div class="collection-grid poem-grid">
            <div
              v-for="item in poemCollection"
              :key="item.id"
              class="poem-card"
              :class="{ unlocked: item.unlocked, 'blog-card': item.isBlog }"
              @click="handlePoemClick(item)"
            >
              <div v-if="item.unlocked" class="poem-content" :class="{ 'has-image': item.image }" :style="getPoemBackground(item)">
                <button class="delete-btn" @click.stop="handleDeleteItem(item)" title="删除">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"/>
                    <line x1="6" y1="6" x2="18" y2="18"/>
                  </svg>
                </button>
                <div class="poem-location-tag">{{ item.location }}</div>
                <div v-if="item.isBlog" class="blog-preview">
                  <p class="blog-title-text">{{ item.title }}</p>
                  <p class="blog-excerpt">{{ item.content?.slice(0, 50) }}{{ item.content?.length > 50 ? '...' : '' }}</p>
                </div>
                <div v-else class="poem-lines">
                  <p v-for="(line, idx) in item.lines.slice(0, 2)" :key="idx" class="poem-line font-serif">{{ line }}</p>
                </div>
                <div class="poem-date">{{ item.date }}</div>
              </div>
              <div v-else class="poem-locked">
                <svg class="lock-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
              </div>
            </div>
            
            <div class="poem-card add-card" @click="showAddPoemModal = true">
              <div class="add-content">
                <svg class="add-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="12" y1="5" x2="12" y2="19"/>
                  <line x1="5" y1="12" x2="19" y2="12"/>
                </svg>
                <span class="add-text">打卡留念</span>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else class="collection-grid seal-grid">
          <div
            v-for="item in sealCollection"
            :key="item.id"
            class="collection-card seal-card"
            :class="{ unlocked: item.unlocked }"
            @click="handleSealClick(item)"
          >
            <div v-if="item.unlocked" class="seal-content">
              <div class="seal-stamp">
                <span class="seal-text font-serif">{{ item.text }}</span>
              </div>
              <span class="seal-name">{{ item.name }}</span>
            </div>
            <div v-else class="seal-locked">
              <svg class="lock-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
            </div>
          </div>
        </div>
      </section>
      
      <section v-if="userStore.artifacts.length > 0" class="artifacts-section">
        <div class="section-header">
          <h3 class="section-title font-serif">文物拓片</h3>
          <span class="section-count clickable" @click="showArtifactCarousel = true">{{ userStore.artifactCount }} 件</span>
        </div>
        <div class="artifacts-preview">
          <div 
            v-for="artifact in latestArtifacts" 
            :key="artifact.id"
            class="artifact-card"
            @click="showArtifactCarousel = true"
          >
            <div class="artifact-image">
              <img v-if="artifact.imageUrl" :src="artifact.imageUrl" :alt="artifact.name" />
              <div v-else class="image-placeholder">🏺</div>
            </div>
            <div class="artifact-info">
              <span class="artifact-name font-serif">{{ artifact.name }}</span>
              <span class="artifact-meta">{{ artifact.dynasty }}</span>
            </div>
          </div>
        </div>
      </section>
    </div>
    
    <nav class="bottom-nav">
      <router-link to="/" class="nav-item">
        <span class="nav-icon">山</span>
        <span class="nav-label">山河卷</span>
      </router-link>
      <router-link to="/ar" class="nav-item">
        <span class="nav-icon">鉴</span>
        <span class="nav-label">AR鉴赏</span>
      </router-link>
      <router-link to="/profile" class="nav-item active">
        <span class="nav-icon">阁</span>
        <span class="nav-label">藏经阁</span>
      </router-link>
    </nav>
    
    <LoginModal 
      :visible="authStore.showLoginModal" 
      @success="handleLoginSuccess"
    />
    
    <EditProfileModal 
      :visible="showEditModal" 
      @close="showEditModal = false"
      @success="handleEditSuccess"
    />
    
    <Teleport to="body">
      <transition name="poem-modal">
        <div v-if="selectedPoem" class="poem-modal-overlay" @click.self="selectedPoem = null">
          <div class="poem-modal" :class="{ 'has-image': selectedPoem.image }">
            <div v-if="selectedPoem.image" class="poem-modal-image">
              <img :src="selectedPoem.image" alt="" />
              <div class="image-overlay" />
            </div>
            <div class="poem-modal-header">
              <div class="poem-modal-location">{{ selectedPoem.location }}</div>
              <button class="poem-modal-close" @click="selectedPoem = null">×</button>
            </div>
            <div class="poem-modal-body">
              <div class="poem-modal-lines font-serif">
                <p v-for="(line, idx) in selectedPoem.lines" :key="idx" class="poem-modal-line">{{ line }}</p>
              </div>
              <div class="poem-modal-footer">
                <span class="poem-modal-date">{{ selectedPoem.date }}</span>
                <span class="poem-modal-ai">{{ selectedPoem.image ? '打卡留念' : 'AI 游记' }}</span>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>
    
    <Teleport to="body">
      <transition name="poem-modal">
        <div v-if="selectedSeal" class="seal-modal-overlay" @click.self="selectedSeal = null">
          <div class="seal-modal">
            <div class="seal-modal-header">
              <div class="seal-modal-title">{{ selectedSeal.name }}</div>
              <button class="seal-modal-close" @click="selectedSeal = null">×</button>
            </div>
            <div class="seal-modal-body">
              <div class="seal-modal-stamp">
                <span class="seal-modal-text font-serif">{{ selectedSeal.text }}</span>
              </div>
              <div class="seal-modal-desc">
                <p>{{ getSealDescription(selectedSeal.name) }}</p>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>

    <Teleport to="body">
      <transition name="modal">
        <div v-if="showAddPoemModal" class="add-poem-overlay" @click.self="showAddPoemModal = false">
          <div class="add-poem-modal">
            <div class="modal-header">
              <h3 class="modal-title font-serif">打卡留念</h3>
              <button class="close-btn" @click="showAddPoemModal = false">×</button>
            </div>
            
            <div class="modal-body">
              <div class="image-upload-area" @click="triggerImageUpload">
                <img v-if="newPoem.image" :src="newPoem.image" class="preview-image" />
                <div v-else class="upload-placeholder">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                    <circle cx="8.5" cy="8.5" r="1.5"/>
                    <polyline points="21 15 16 10 5 21"/>
                  </svg>
                  <span>点击上传照片</span>
                </div>
                <input ref="imageInput" type="file" accept="image/*" class="hidden-input" @change="handleImageUpload" />
              </div>
              
              <div class="input-group">
                <label class="input-label">地点</label>
                <input v-model="newPoem.location" type="text" class="input-field" placeholder="记录你的足迹" />
              </div>
              
              <div class="poem-type-toggle">
                <button 
                  class="toggle-btn" 
                  :class="{ active: newPoem.type === 'poem' }"
                  @click="newPoem.type = 'poem'"
                >
                  写诗
                </button>
                <button 
                  class="toggle-btn" 
                  :class="{ active: newPoem.type === 'blog' }"
                  @click="newPoem.type = 'blog'"
                >
                  写游记
                </button>
              </div>
              
              <div v-if="newPoem.type === 'poem'" class="poem-mode-area">
                <div class="poem-sub-toggle">
                  <button 
                    class="sub-toggle-btn" 
                    :class="{ active: newPoem.poemMode === 'ai' }"
                    @click="newPoem.poemMode = 'ai'"
                  >
                    AI 生成
                  </button>
                  <button 
                    class="sub-toggle-btn" 
                    :class="{ active: newPoem.poemMode === 'custom' }"
                    @click="newPoem.poemMode = 'custom'"
                  >
                    自定义
                  </button>
                </div>
                
                <div v-if="newPoem.poemMode === 'custom'" class="custom-poem-area">
                  <textarea 
                    v-model="newPoem.customContent" 
                    class="poem-textarea font-serif"
                    placeholder="写下你的诗句...&#10;每行一句，最多四句"
                    rows="4"
                    maxlength="100"
                  />
                </div>
                
                <div v-else class="ai-preview">
                  <div v-if="aiGenerating" class="ai-loading">
                    <div class="loading-spinner" />
                    <span>AI 正在创作中...</span>
                  </div>
                  <div v-else-if="newPoem.aiLines.length" class="ai-lines font-serif">
                    <p v-for="(line, idx) in newPoem.aiLines" :key="idx">{{ line }}</p>
                  </div>
                  <div v-else class="ai-hint">
                    <span>点击下方按钮让 AI 为你创作</span>
                  </div>
                  <button 
                    v-if="!aiGenerating" 
                    class="generate-btn"
                    :disabled="!newPoem.location"
                    @click="generateAiPoem"
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
                    </svg>
                    生成诗词
                  </button>
                </div>
              </div>
              
              <div v-else class="blog-mode-area">
                <div class="blog-title-input">
                  <label class="input-label">标题</label>
                  <input v-model="newPoem.blogTitle" type="text" class="input-field" placeholder="给游记起个标题" />
                </div>
                <div class="blog-content-area">
                  <label class="input-label">游记内容</label>
                  <textarea 
                    v-model="newPoem.blogContent" 
                    class="blog-textarea"
                    placeholder="记录你的旅途见闻...&#10;分享你的文化探索之旅"
                    rows="6"
                    maxlength="500"
                  />
                </div>
              </div>
            </div>
            
            <div class="modal-footer">
              <button class="cancel-btn" @click="showAddPoemModal = false">取消</button>
              <button 
                class="save-btn" 
                :disabled="!canSavePoem"
                @click="savePoem"
              >
                保存
              </button>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>
    
    <Teleport to="body">
      <transition name="carousel">
        <div v-if="showArtifactCarousel" class="artifact-carousel-overlay" @click.self="showArtifactCarousel = false">
          <div class="artifact-carousel">
            <div class="carousel-header">
              <h3 class="carousel-title font-serif">文物拓片</h3>
              <button class="carousel-close" @click="closeCarousel">×</button>
            </div>
            
            <div class="carousel-body">
              <div class="carousel-map" ref="mapContainer"></div>
              
              <div class="carousel-container">
                <div class="carousel-track" :style="{ transform: `translateX(-${carouselIndex * 100}%)` }">
                  <div 
                    v-for="(artifact, idx) in userStore.artifacts" 
                    :key="artifact.id"
                    class="carousel-slide"
                    :class="{ active: idx === carouselIndex }"
                  >
                    <div 
                      class="slide-image" 
                      :class="{ clickable: hasModel(artifact) }"
                      @click="openModelViewer(artifact)"
                    >
                      <img v-if="artifact.imageUrl" :src="artifact.imageUrl" :alt="artifact.name" />
                      <div v-else class="image-placeholder large">🏺</div>
                      <div v-if="hasModel(artifact)" class="model-badge">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                          <path d="M2 17l10 5 10-5"/>
                          <path d="M2 12l10 5 10-5"/>
                        </svg>
                        <span>3D</span>
                      </div>
                    </div>
                    <div class="slide-info">
                      <h4 class="slide-name font-serif">{{ artifact.name }}</h4>
                      <div class="slide-meta">
                        <span class="meta-item">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                            <line x1="16" y1="2" x2="16" y2="6"/>
                            <line x1="8" y1="2" x2="8" y2="6"/>
                            <line x1="3" y1="10" x2="21" y2="10"/>
                          </svg>
                          {{ artifact.date || '2024.03.15' }}
                        </span>
                        <span class="meta-item">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                            <circle cx="12" cy="10" r="3"/>
                          </svg>
                          {{ artifact.location || artifact.dynasty }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="carousel-dots">
              <span 
                v-for="(_, idx) in userStore.artifacts" 
                :key="idx"
                class="dot"
                :class="{ active: idx === carouselIndex }"
                @click="goToSlide(idx)"
              />
            </div>
          </div>
        </div>
      </transition>
    </Teleport>
    
    <Teleport to="body">
      <transition name="model">
        <div v-if="showModelViewer" class="model-viewer-overlay" @click.self="closeModelViewer">
          <div class="model-viewer">
            <div class="model-header">
              <h3 class="model-title font-serif">{{ currentModelArtifact?.name }}</h3>
              <button class="model-close" @click="closeModelViewer">×</button>
            </div>
            <div class="model-container" ref="modelContainer"></div>
            <div class="model-controls">
              <button class="control-btn" @click="resetModelView">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 4v6h6M23 20v-6h-6"/>
                  <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15"/>
                </svg>
                <span>重置视角</span>
              </button>
              <button class="control-btn" @click="toggleAutoRotate">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8"/>
                  <path d="M21 3v5h-5"/>
                </svg>
                <span>{{ autoRotate ? '停止旋转' : '自动旋转' }}</span>
              </button>
              <button v-if="isARSupported" class="control-btn ar-btn" @click="startARMode">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                  <path d="M2 17l10 5 10-5"/>
                  <path d="M2 12l10 5 10-5"/>
                </svg>
                <span>AR展示</span>
              </button>
            </div>
            <div class="model-hint">拖动旋转 · 双指缩放 · 点击文物查看细节</div>
          </div>
        </div>
      </transition>
    </Teleport>
    
    <Teleport to="body">
      <transition name="ar">
        <div v-if="showARViewer" class="ar-viewer-overlay">
          <div class="ar-viewer">
            <div class="ar-header">
              <h3 class="ar-title font-serif">{{ currentModelArtifact?.name }}</h3>
              <button class="ar-close" @click="closeARViewer">×</button>
            </div>
            <div class="ar-container" ref="arContainer"></div>
            <div class="ar-controls">
              <button class="ar-control-btn" @click="placeModel">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <span>放置模型</span>
              </button>
            </div>
            <div class="ar-hint">移动设备寻找平面 · 点击放置模型</div>
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { useAuthStore } from '@/stores/authStore'
import { aiApi, travelBlogApi } from '@/api'
import LoginModal from '@/components/organisms/LoginModal.vue'
import EditProfileModal from '@/components/organisms/EditProfileModal.vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import * as THREE from 'three'
import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js'
import { OrbitControls } from 'three/addons/controls/OrbitControls.js'
import { ARButton } from 'three/addons/webxr/ARButton.js'

const router = useRouter()
const userStore = useUserStore()
const authStore = useAuthStore()

const activeTab = ref('poems')
const showEditModal = ref(false)
const loading = ref(false)
const selectedPoem = ref(null)
const selectedSeal = ref(null)
const showAddPoemModal = ref(false)
const aiGenerating = ref(false)
const imageInput = ref(null)
const showArtifactCarousel = ref(false)
const carouselIndex = ref(0)
const mapContainer = ref(null)
let map = null
let autoPlayTimer = null

const showModelViewer = ref(false)
const modelContainer = ref(null)
const currentModelArtifact = ref(null)
const autoRotate = ref(true)
let scene = null
let camera = null
let renderer = null
let controls = null
let model = null
let animationId = null

const modelMapping = {
  '青花瓷瓶': '/models/qinghuaci.glb',
  '粉彩瓷瓶': '/models/fencaici.glb',
  '青花瓷面具': '/models/qinghuacimianju.glb'
}

const showARViewer = ref(false)
const arContainer = ref(null)
const isARSupported = ref(false)
let arSession = null
let arScene = null
let arCamera = null
let arRenderer = null
let arModel = null
let arHitTestSource = null
let arReticle = null
let arAnimationId = null

const mockPoems = [
  {
    id: 'poem1',
    location: '故宫博物院',
    date: '2024.03.15',
    lines: ['红墙金瓦映斜阳', '千古帝王梦一场', '游人如织皆过客', '唯有宫墙诉沧桑'],
    unlocked: true
  },
  {
    id: 'poem2',
    location: '秦始皇兵马俑',
    date: '2024.03.10',
    lines: ['千年军阵守秦关', '陶俑无言望故山', '始皇帝业今何在', '唯有黄土伴清寒'],
    unlocked: true
  },
  {
    id: 'poem3',
    location: '长城',
    date: '2024.02.28',
    lines: ['万里长城万里长', '烽火台前忆旧章', '多少英雄埋骨处', '如今唯见草苍苍'],
    unlocked: true
  },
  {
    id: 'poem4',
    location: '敦煌莫高窟',
    date: '2024.02.20',
    lines: ['飞天曼舞画中仙', '丝路驼铃忆昔年', '千佛洞中藏秘境', '黄沙漫漫诉流年'],
    unlocked: false
  }
]

const newPoem = ref({
  image: '',
  location: '',
  type: 'poem',
  poemMode: 'ai',
  customContent: '',
  aiLines: [],
  blogTitle: '',
  blogContent: ''
})

const mockSeals = [
  { id: 'seal1', name: '初识', text: '初识', unlocked: true },
  { id: 'seal2', name: '登堂', text: '登堂', unlocked: true },
  { id: 'seal3', name: '入室', text: '入室', unlocked: false },
  { id: 'seal4', name: '登峰', text: '登峰', unlocked: false },
  { id: 'seal5', name: '造极', text: '造极', unlocked: false },
  { id: 'seal6', name: '大成', text: '大成', unlocked: false }
]

const poemCollection = ref(mockPoems)
const sealCollection = ref(mockSeals)

const POEM_STORAGE_KEY = 'wenji_user_poems'// 用户诗词存储的键

const loadUserPoems = () => {
  try {
    const stored = localStorage.getItem(POEM_STORAGE_KEY)
    if (stored) {
      const userPoems = JSON.parse(stored)
      const existingIds = new Set(poemCollection.value.map(p => p.id))
      userPoems.forEach(poem => {
        if (!existingIds.has(poem.id)) {
          poemCollection.value.unshift(poem)
        }
      })
    }
  } catch (error) {
    console.error('加载诗词失败:', error)
  }
}

const saveUserPoems = () => {
  try {
    const userPoems = poemCollection.value.filter(p => !p.id.startsWith('poem1') && !p.id.startsWith('poem2') && !p.id.startsWith('poem3') && !p.id.startsWith('poem4'))
    localStorage.setItem(POEM_STORAGE_KEY, JSON.stringify(userPoems))
  } catch (error) {
    console.error('保存诗词失败:', error)
  }
}

const getUserTitle = computed(() => {
  const level = userStore.currentLevel
  const titles = {
    '初识': '初入文坛',
    '登堂': '略有小成',
    '入室': '文采斐然',
    '登峰': '妙笔生花',
    '造极': '文坛巨匠',
    '大成': '一代宗师'
  }
  return titles[level] || '初入文坛'
})

const progressPercent = computed(() => {
  if (!userStore.nextLevel) return 100
  const thresholds = { '初识': 0, '登堂': 100, '入室': 300, '登峰': 600, '造极': 1000, '大成': 2000 }
  const current = thresholds[userStore.currentLevel] || 0
  const next = thresholds[userStore.nextLevel.name] || 0
  const range = next - current
  if (range <= 0) return 100
  const progress = userStore.experience - current
  return Math.min(100, Math.max(0, (progress / range) * 100))
})

const collectedCount = computed(() => {
  if (activeTab.value === 'poems') {
    return poemCollection.value.filter(p => p.unlocked).length
  }
  return sealCollection.value.filter(s => s.unlocked).length
})

const totalCollections = computed(() => {
  if (activeTab.value === 'poems') {
    return poemCollection.value.length
  }
  return sealCollection.value.length
})

const latestArtifacts = computed(() => {
  return userStore.artifacts.slice(0, 2)
})

const handleLogin = () => {
  authStore.showLoginModal = true
}

const handleLoginSuccess = async () => {
  await userStore.fetchUserInfo()
}

const handleLogout = async () => {
  await authStore.logout()
  userStore.clearUser()
  userStore.loadFromStorage()
}

const handleAvatarClick = () => {
  if (!authStore.isGuest) {
    showEditModal.value = true
  }
}

const handleEditSuccess = () => {
  showEditModal.value = false
}

const getPoemBackground = (item) => {
  if (item.image) {
    return {
      backgroundImage: `linear-gradient(180deg, rgba(0,0,0,0.3) 0%, rgba(0,0,0,0.5) 100%), url(${item.image})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center'
    }
  }
  return {}
}

const canSavePoem = computed(() => {
  if (newPoem.value.type === 'poem') {
    if (!newPoem.value.location) return false
    if (newPoem.value.poemMode === 'custom') {
      return newPoem.value.customContent.trim().length > 0
    }
    return newPoem.value.aiLines.length > 0
  }
  
  if (newPoem.value.type === 'blog') {
    return newPoem.value.blogTitle.trim().length > 0 && newPoem.value.blogContent.trim().length > 0
  }
  
  return false
})

const triggerImageUpload = () => {
  imageInput.value?.click()
}

const handleImageUpload = (e) => {
  const file = e.target.files?.[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (ev) => {
      newPoem.value.image = ev.target?.result
    }
    reader.readAsDataURL(file)
  }
}

const generateAiPoem = async () => {
  if (!newPoem.value.location) return
  
  aiGenerating.value = true
  
  try {
    const prompt = `请为"${newPoem.value.location}"这个地点创作一首七言绝句，要求：1. 突出该地点的特色和意境 2. 语言典雅优美 3. 只输出四句诗，不要其他解释。`
    
    const response = await aiApi.chat(prompt, `poem_profile_${Date.now()}`)
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let poem = ''
    let buffer = ''
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const content = line.slice(5).trim()
          if (content && content !== '[DONE]') {
            poem += content
          }
        } else if (line.trim() && !line.startsWith(':')) {
          poem += line
        }
      }
    }
    
    if (poem.trim()) {
      const poemLines = poem.trim().split(/[，。！？\n]/).filter(l => l.trim()).slice(0, 4)
      newPoem.value.aiLines = poemLines.length > 0 ? poemLines : ['云卷云舒天地间', '风起风落岁月闲', '此间美景君须记', '莫负韶华莫负山']
    }
  } catch (error) {
    console.error('AI生成诗词失败:', error)
    const templates = [
      ['云卷云舒天地间', '风起风落岁月闲', '此间美景君须记', '莫负韶华莫负山'],
      ['山川秀美入画来', '游人如织共徘徊', '此情此景难言表', '唯有诗心寄情怀']
    ]
    newPoem.value.aiLines = templates[Math.floor(Math.random() * templates.length)]
  } finally {
    aiGenerating.value = false
  }
}

const savePoem = async () => {
  if (newPoem.value.type === 'blog') {
    await saveBlog()
  } else {
    await savePoemContent()
  }
}

const savePoemContent = async () => {
  const lines = newPoem.value.poemMode === 'custom' 
    ? newPoem.value.customContent.split('\n').filter(l => l.trim())
    : newPoem.value.aiLines
  
  const poem = {
    id: `poem_${Date.now()}`,
    location: newPoem.value.location,
    date: new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\//g, '.'),
    lines: lines.slice(0, 4),
    image: newPoem.value.image,
    unlocked: true
  }
  
  poemCollection.value.unshift(poem)
  saveUserPoems()
  
  resetNewPoem()
  showAddPoemModal.value = false
}

const saveBlog = async () => {
  try {
    const blogData = {
      title: newPoem.value.blogTitle,
      content: newPoem.value.blogContent,
      siteId: newPoem.value.location,
      images: newPoem.value.image ? JSON.stringify([newPoem.value.image]) : null
    }
    
    const result = await travelBlogApi.publishBlog(blogData)
    
    if (result.success) {
      const blog = {
        id: `blog_${Date.now()}`,
        location: newPoem.value.location,
        date: new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\//g, '.'),
        title: newPoem.value.blogTitle,
        content: newPoem.value.blogContent,
        image: newPoem.value.image,
        unlocked: true,
        isBlog: true
      }
      
      poemCollection.value.unshift(blog)
      
      resetNewPoem()
      showAddPoemModal.value = false
    } else {
      console.error('保存游记失败:', result.message)
    }
  } catch (error) {
    console.error('保存游记失败:', error)
  }
}

const resetNewPoem = () => {
  newPoem.value = {
    image: '',
    location: '',
    type: 'poem',
    poemMode: 'ai',
    customContent: '',
    aiLines: [],
    blogTitle: '',
    blogContent: ''
  }
}

const handlePoemClick = (item) => {
  if (!item.unlocked) return
  selectedPoem.value = item
}

const handleDeleteItem = async (item) => {
  if (!confirm(`确定要删除这篇${item.isBlog ? '游记' : '诗词'}吗？`)) return
  
  if (item.isBlog) {
    const blogId = item.id.replace('blog_', '')
    try {
      const result = await travelBlogApi.deleteBlog(blogId)
      if (result.success) {
        poemCollection.value = poemCollection.value.filter(p => p.id !== item.id)
      } else {
        console.error('删除游记失败:', result.message)
      }
    } catch (error) {
      console.error('删除游记失败:', error)
    }
  } else {
    poemCollection.value = poemCollection.value.filter(p => p.id !== item.id)
    saveUserPoems()
  }
}

const handleCardClick = (item) => {
  if (!item.unlocked) return
}

const getSealDescription = (sealName) => {
  const descriptions = {
    '初识': '初识文坛，踏上探索传统文化之旅的第一步。',
    '登堂': '登堂入室，对传统文化有了更深入的理解。',
    '入室': '入室求索，已能领略传统文化的精髓所在。',
    '登峰': '登峰造极，对传统文化的研究已达到较高境界。',
    '造极': '造极登峰，在传统文化领域已有独到见解。',
    '大成': '大成之境，已成为传统文化领域的大家。'
  }
  return descriptions[sealName] || ''
}

const handleSealClick = (item) => {
  if (!item.unlocked) return
  selectedSeal.value = item
}

const viewArtifact = (artifact) => {
  router.push({ path: '/reward', query: { id: artifact.id } })
}

const initMap = () => {
  if (!mapContainer.value || map) return
  
  map = L.map(mapContainer.value, {
    center: [35.8617, 104.1954],
    zoom: 4,
    zoomControl: false,
    attributionControl: false
  })
  
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 18
  }).addTo(map)
  
  addMarkers()
}

const addMarkers = () => {
  if (!map) return
  
  const locations = [
    { name: '故宫博物院', lat: 39.9163, lng: 116.3972 },
    { name: '秦始皇兵马俑', lat: 34.3847, lng: 109.2783 },
    { name: '长城', lat: 40.4319, lng: 116.5704 },
    { name: '敦煌莫高窟', lat: 40.0359, lng: 94.8093 }
  ]
  
  locations.forEach(loc => {
    L.circleMarker([loc.lat, loc.lng], {
      radius: 6,
      fillColor: '#C9A227',
      color: '#fff',
      weight: 2,
      opacity: 1,
      fillOpacity: 0.8
    }).addTo(map).bindPopup(loc.name)
  })
}

const startAutoPlay = () => {
  stopAutoPlay()
  autoPlayTimer = setInterval(() => {
    if (carouselIndex.value < userStore.artifacts.length - 1) {
      carouselIndex.value++
    } else {
      carouselIndex.value = 0
    }
  }, 3000)
}

const stopAutoPlay = () => {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer)
    autoPlayTimer = null
  }
}

const closeCarousel = () => {
  stopAutoPlay()
  showArtifactCarousel.value = false
}

const goToSlide = (idx) => {
  carouselIndex.value = idx
  startAutoPlay()
}

const hasModel = (artifact) => {
  return modelMapping[artifact.name] !== undefined
}

const openModelViewer = (artifact) => {
  if (!hasModel(artifact)) return
  stopAutoPlay()
  currentModelArtifact.value = artifact
  showModelViewer.value = true
}

const closeModelViewer = () => {
  showModelViewer.value = false
  disposeThreeScene()
}

const initThreeScene = () => {
  if (!modelContainer.value) return
  
  const container = modelContainer.value
  const width = container.clientWidth
  const height = container.clientHeight
  
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x1a1a1a)
  
  camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 1000)
  camera.position.set(0, 1, 3)
  
  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setSize(width, height)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  container.appendChild(renderer.domElement)
  
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.autoRotate = autoRotate.value
  controls.autoRotateSpeed = 2
  
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)
  
  const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8)
  directionalLight.position.set(5, 10, 7)
  directionalLight.castShadow = true
  scene.add(directionalLight)
  
  const backLight = new THREE.DirectionalLight(0xffffff, 0.3)
  backLight.position.set(-5, 5, -5)
  scene.add(backLight)
  
  loadModel()
  animate()
}

const loadModel = () => {
  if (!currentModelArtifact.value || !scene) return
  
  const modelPath = modelMapping[currentModelArtifact.value.name]
  if (!modelPath) return
  
  const loader = new GLTFLoader()
  
  loader.load(
    modelPath,
    (gltf) => {
      if (model) {
        scene.remove(model)
      }
      
      model = gltf.scene
      
      const box = new THREE.Box3().setFromObject(model)
      const center = box.getCenter(new THREE.Vector3())
      const size = box.getSize(new THREE.Vector3())
      
      const maxDim = Math.max(size.x, size.y, size.z)
      const scale = 1.5 / maxDim
      model.scale.setScalar(scale)
      
      model.position.sub(center.multiplyScalar(scale))
      model.position.y = 0
      
      model.traverse((child) => {
        if (child.isMesh) {
          child.castShadow = true
          child.receiveShadow = true
        }
      })
      
      scene.add(model)
    },
    (progress) => {
      console.log('Loading model...', (progress.loaded / progress.total * 100).toFixed(1) + '%')
    },
    (error) => {
      console.error('Error loading model:', error)
    }
  )
}

const animate = () => {
  animationId = requestAnimationFrame(animate)
  
  if (controls) {
    controls.update()
  }
  
  if (renderer && scene && camera) {
    renderer.render(scene, camera)
  }
}

const resetModelView = () => {
  if (camera && controls) {
    camera.position.set(0, 1, 3)
    controls.reset()
  }
}

const toggleAutoRotate = () => {
  autoRotate.value = !autoRotate.value
  if (controls) {
    controls.autoRotate = autoRotate.value
  }
}

const disposeThreeScene = () => {
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
  
  if (controls) {
    controls.dispose()
    controls = null
  }
  
  if (model && scene) {
    scene.remove(model)
    model.traverse((child) => {
      if (child.isMesh) {
        child.geometry.dispose()
        if (child.material.map) {
          child.material.map.dispose()
        }
        child.material.dispose()
      }
    })
    model = null
  }
  
  if (renderer) {
    renderer.dispose()
    if (modelContainer.value && renderer.domElement) {
      modelContainer.value.removeChild(renderer.domElement)
    }
    renderer = null
  }
  
  scene = null
  camera = null
}

watch(showModelViewer, (val) => {
  if (val) {
    nextTick(() => {
      initThreeScene()
      checkARSupport()
    })
  } else {
    disposeThreeScene()
  }
})

const checkARSupport = async () => {
  if ('xr' in navigator) {
    try {
      const supported = await navigator.xr.isSessionSupported('immersive-ar')
      isARSupported.value = supported
    } catch (e) {
      isARSupported.value = false
    }
  } else {
    isARSupported.value = false
  }
}

const startARMode = async () => {
  if (!isARSupported.value) return
  
  closeModelViewer()
  showARViewer.value = true
  
  await nextTick()
  initARScene()
}

const initARScene = async () => {
  if (!arContainer.value) return
  
  const container = arContainer.value
  const width = container.clientWidth
  const height = container.clientHeight
  
  arScene = new THREE.Scene()
  
  arCamera = new THREE.PerspectiveCamera(70, width / height, 0.01, 20)
  
  arRenderer = new THREE.WebGLRenderer({ 
    antialias: true, 
    alpha: true 
  })
  arRenderer.setSize(width, height)
  arRenderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  arRenderer.xr.enabled = true
  container.appendChild(arRenderer.domElement)
  
  const light = new THREE.HemisphereLight(0xffffff, 0xbbbbff, 1)
  arScene.add(light)
  
  const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8)
  directionalLight.position.set(0, 5, 5)
  arScene.add(directionalLight)
  
  const reticleGeometry = new THREE.RingGeometry(0.15, 0.2, 32)
  reticleGeometry.rotateX(-Math.PI / 2)
  const reticleMaterial = new THREE.MeshBasicMaterial({ 
    color: 0xC9A227,
    opacity: 0.7,
    transparent: true 
  })
  arReticle = new THREE.Mesh(reticleGeometry, reticleMaterial)
  arReticle.matrixAutoUpdate = false
  arReticle.visible = false
  arScene.add(arReticle)
  
  try {
    arSession = await navigator.xr.requestSession('immersive-ar', {
      requiredFeatures: ['hit-test'],
      optionalFeatures: ['dom-overlay'],
      domOverlay: { root: container }
    })
    
    arRenderer.xr.setReferenceSpaceType('local')
    await arRenderer.xr.setSession(arSession)
    
    arSession.addEventListener('end', () => {
      closeARViewer()
    })
    
    loadARModel()
    animateAR()
    
  } catch (e) {
    console.error('AR初始化失败:', e)
    alert('AR功能初始化失败，请确保设备支持AR并授予摄像头权限')
    closeARViewer()
  }
}

const loadARModel = () => {
  if (!currentModelArtifact.value || !arScene) return
  
  const modelPath = modelMapping[currentModelArtifact.value.name]
  if (!modelPath) return
  
  const loader = new GLTFLoader()
  
  loader.load(
    modelPath,
    (gltf) => {
      if (arModel) {
        arScene.remove(arModel)
      }
      
      arModel = gltf.scene
      
      const box = new THREE.Box3().setFromObject(arModel)
      const size = box.getSize(new THREE.Vector3())
      const maxDim = Math.max(size.x, size.y, size.z)
      const scale = 0.3 / maxDim
      arModel.scale.setScalar(scale)
      
      arModel.traverse((child) => {
        if (child.isMesh) {
          child.castShadow = true
        }
      })
    },
    undefined,
    (error) => {
      console.error('加载模型失败:', error)
    }
  )
}

const animateAR = () => {
  arAnimationId = arRenderer.setAnimationLoop((timestamp, frame) => {
    if (frame) {
      const referenceSpace = arRenderer.xr.getReferenceSpace()
      const session = arRenderer.xr.getSession()
      
      if (!arHitTestSource) {
        session.requestReferenceSpace('viewer').then((viewerSpace) => {
          session.requestHitTestSource({ space: viewerSpace }).then((hitTestSource) => {
            arHitTestSource = hitTestSource
          })
        })
      }
      
      if (arHitTestSource) {
        const hitTestResults = frame.getHitTestResults(arHitTestSource)
        
        if (hitTestResults.length > 0) {
          const hit = hitTestResults[0]
          const pose = hit.getPose(referenceSpace)
          
          arReticle.visible = true
          arReticle.matrix.fromArray(pose.transform.matrix)
        } else {
          arReticle.visible = false
        }
      }
    }
    
    arRenderer.render(arScene, arCamera)
  })
}

const placeModel = () => {
  if (!arModel || !arReticle || !arReticle.visible) {
    alert('请先移动设备检测到平面')
    return
  }
  
  const placedModel = arModel.clone()
  placedModel.position.setFromMatrixPosition(arReticle.matrix)
  placedModel.rotation.y = Math.random() * Math.PI * 2
  arScene.add(placedModel)
}

const closeARViewer = () => {
  if (arAnimationId) {
    arRenderer.setAnimationLoop(null)
    arAnimationId = null
  }
  
  if (arSession) {
    arSession.end()
    arSession = null
  }
  
  if (arRenderer) {
    arRenderer.dispose()
    if (arContainer.value && arRenderer.domElement) {
      arContainer.value.removeChild(arRenderer.domElement)
    }
    arRenderer = null
  }
  
  if (arModel) {
    arModel.traverse((child) => {
      if (child.isMesh) {
        child.geometry.dispose()
        if (child.material.map) {
          child.material.map.dispose()
        }
        child.material.dispose()
      }
    })
    arModel = null
  }
  
  arScene = null
  arCamera = null
  arHitTestSource = null
  arReticle = null
  
  showARViewer.value = false
}

watch(showArtifactCarousel, (val) => {
  if (val) {
    carouselIndex.value = 0
    nextTick(() => {
      initMap()
      startAutoPlay()
    })
  } else {
    stopAutoPlay()
  }
})

const initPage = async () => {
  authStore.loadFromStorage()
  userStore.loadFromStorage()
  
  loadUserPoems()
  
  if (authStore.isLoggedIn && !userStore.username) {
    loading.value = true
    await userStore.fetchUserInfo()
    loading.value = false
  }
  
  await loadUserBlogs()
}

const loadUserBlogs = async () => {
  if (!authStore.isLoggedIn) return
  
  try {
    const result = await travelBlogApi.getMyBlogs()
    if (result.success && result.data) {
      const blogs = result.data.map(blog => ({
        id: `blog_${blog.blogId}`,
        location: blog.siteId || '',
        date: blog.createTime ? new Date(blog.createTime).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\//g, '.') : '',
        title: blog.title,
        content: blog.content,
        image: blog.images ? JSON.parse(blog.images)[0] : null,
        unlocked: true,
        isBlog: true
      }))
      
      const existingBlogIds = new Set(
        poemCollection.value
          .filter(p => p.isBlog)
          .map(p => p.id)
      )
      
      blogs.forEach(blog => {
        if (!existingBlogIds.has(blog.id)) {
          poemCollection.value.unshift(blog)
        }
      })
    }
  } catch (error) {
    console.error('加载游记失败:', error)
  }
}

onMounted(() => {
  initPage()
})

onUnmounted(() => {
  stopAutoPlay()
  disposeThreeScene()
  closeARViewer()
  if (map) {
    map.remove()
    map = null
  }
})
</script>

<style scoped>
.profile-view {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-height: 100dvh;
  background: #F0F4F8;
}

.profile-header {
  position: relative;
  padding: 20px;
  padding-top: calc(env(safe-area-inset-top) + 20px);
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 50%, #2D4059 100%);
  overflow: hidden;
}

.header-bg-pattern {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M30 0L60 30L30 60L0 30z' fill='none' stroke='rgba(255,255,255,0.03)' stroke-width='1'/%3E%3C/svg%3E");
  opacity: 0.5;
}

.user-info {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.avatar-container {
  position: relative;
  width: 72px;
  height: 72px;
  flex-shrink: 0;
  cursor: pointer;
}

.avatar-ring {
  position: absolute;
  inset: -3px;
  border: 2px solid rgba(201, 162, 39, 0.6);
  border-radius: 50%;
  animation: ring-pulse 2s ease-in-out infinite;
}

@keyframes ring-pulse {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.05); opacity: 1; }
}

.avatar-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #A8DADC 0%, #7FB3B5 100%);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  color: rgba(255, 255, 255, 0.8);
}

.level-badge {
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #C9A227 0%, #B8860B 100%);
  padding: 2px 12px;
  border-radius: 10px;
  border: 2px solid #2D4059;
}

.level-text {
  font-size: 11px;
  color: #FFF;
  white-space: nowrap;
}

.user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-primary {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 20px;
  color: #FFF;
  margin: 0;
  letter-spacing: 0.05em;
}

.user-title {
  display: flex;
  align-items: center;
  gap: 4px;
}

.title-icon {
  font-size: 12px;
}

.title-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.user-actions {
  display: flex;
  gap: 8px;
}

.login-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  color: #FFF;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

.edit-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  color: #FFF;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.edit-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

.logout-btn {
  padding: 8px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #FFF;
}

.stats-row {
  position: relative;
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-value {
  font-size: 18px;
  color: #FFF;
  font-weight: 600;
}

.stat-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
}

.stat-divider {
  width: 1px;
  background: rgba(255, 255, 255, 0.2);
}

.level-progress {
  position: relative;
}

.progress-bar {
  height: 4px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 6px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #C9A227 0%, #F0D78C 100%);
  border-radius: 2px;
  transition: width 0.4s ease;
}

.progress-hint {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

.max-level .progress-hint {
  color: #C9A227;
  text-align: center;
  display: block;
}

.profile-content {
  flex: 1;
  padding: 20px 16px 100px;
  overflow-y: auto;
}

.collection-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title {
  font-size: 16px;
  color: #2D4059;
  margin: 0;
  letter-spacing: 0.1em;
}

.section-count {
  font-size: 12px;
  color: #8B9A9C;
  background: rgba(45, 64, 89, 0.06);
  padding: 2px 8px;
  border-radius: 10px;
}

.collection-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(45, 64, 89, 0.05);
  border: none;
  border-radius: 20px;
  color: #8B9A9C;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn.active {
  background: #2D4059;
  color: #FFF;
}

.tab-icon {
  font-size: 14px;
}

.collection-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.poem-grid {
  grid-template-columns: repeat(2, 1fr);
}

.poem-card {
  position: relative;
  height: 140px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.poem-card:hover {
  transform: translateY(-2px);
}

.poem-content {
  width: 100%;
  height: 100%;
  background: linear-gradient(145deg, #FFF 0%, #F8F6F1 100%);
  border-radius: 12px;
  border: 1px solid rgba(45, 64, 89, 0.08);
  position: relative;
}

.delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(45, 64, 89, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;
  z-index: 10;
}

.poem-card:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: #ff4d4f;
  border-color: #ff4d4f;
  color: #fff;
}

.poem-content {
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 12px;
  position: relative;
  background-size: cover;
  background-position: center;
}

.poem-content.has-image .poem-location-tag,
.poem-content.has-image .poem-line,
.poem-content.has-image .poem-date {
  position: relative;
  z-index: 1;
}

.poem-content.has-image .poem-location-tag {
  background: rgba(255, 255, 255, 0.9);
}

.poem-content.has-image .poem-line {
  color: #FFF;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.poem-content.has-image .poem-date {
  color: rgba(255, 255, 255, 0.8);
}

.poem-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #E8D5B7, #C9A227, #E8D5B7);
}

.poem-location-tag {
  font-size: 11px;
  color: #2D4059;
  font-weight: 500;
  margin-bottom: auto;
  padding: 2px 6px;
  background: rgba(45, 64, 89, 0.06);
  border-radius: 4px;
  align-self: flex-start;
}

.poem-lines {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
}

.poem-line {
  font-size: 12px;
  color: #2D4059;
  line-height: 1.5;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.poem-date {
  font-size: 10px;
  color: #8B9A9C;
  text-align: right;
  margin-top: auto;
}

.poem-locked {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(45, 64, 89, 0.03);
  border-radius: 12px;
  border: 1px dashed rgba(45, 64, 89, 0.12);
}

.poem-locked .lock-icon {
  color: #B8C4C8;
}

.collection-card {
  position: relative;
  height: 140px;
  cursor: pointer;
}

.lock-icon {
  color: #8B9A9C;
  opacity: 0.5;
  margin-bottom: 8px;
}

.lock-hint {
  font-size: 12px;
  color: #8B9A9C;
  opacity: 0.5;
}

.seal-grid {
  grid-template-columns: repeat(3, 1fr);
}

.seal-card {
  height: 100px;
}

.seal-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #FDFBF7;
  border-radius: 12px;
  border: 1px solid rgba(45, 64, 89, 0.08);
}

.seal-stamp {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #C43C3C 0%, #8B2323 100%);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 6px;
  transform: rotate(-3deg);
}

.seal-text {
  font-size: 14px;
  color: #FFF;
  writing-mode: vertical-rl;
}

.seal-name {
  font-size: 11px;
  color: #8B9A9C;
}

.seal-locked {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(45, 64, 89, 0.03);
  border-radius: 12px;
  border: 1px dashed rgba(45, 64, 89, 0.15);
}

.seal-locked .lock-icon {
  color: #8B9A9C;
  opacity: 0.3;
}

.artifacts-section {
  margin-bottom: 24px;
}

.section-count.clickable {
  cursor: pointer;
  transition: all 0.2s;
}

.section-count.clickable:hover {
  background: rgba(201, 162, 39, 0.15);
  color: #C9A227;
}

.artifacts-preview {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.artifact-card {
  background: #FDFBF7;
  border-radius: 12px;
  border: 1px solid rgba(45, 64, 89, 0.08);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
}

.artifact-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(45, 64, 89, 0.1);
}

.artifact-card .artifact-image {
  width: 100%;
  height: 100px;
  border-radius: 0;
}

.artifact-card .artifact-info {
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.artifact-card .artifact-name {
  font-size: 13px;
  color: #2D4059;
}

.artifact-card .artifact-meta {
  font-size: 11px;
  color: #8B9A9C;
}

.artifact-image {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  background: rgba(45, 64, 89, 0.05);
  flex-shrink: 0;
}

.artifact-image img {
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
  font-size: 20px;
  opacity: 0.3;
}

.image-placeholder.large {
  font-size: 48px;
}

.artifact-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.artifact-name {
  font-size: 14px;
  color: #2D4059;
}

.artifact-meta {
  font-size: 11px;
  color: #8B9A9C;
}

.bottom-nav {
  display: flex;
  justify-content: space-around;
  padding: 12px 16px;
  padding-bottom: calc(env(safe-area-inset-bottom) + 12px);
  background: rgba(245, 242, 235, 0.98);
  border-top: 1px solid rgba(45, 64, 89, 0.08);
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 10;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  text-decoration: none;
  color: #8B9A9C;
  transition: color 0.3s ease;
}

.nav-item.active {
  color: #2D4059;
}

.nav-icon {
  font-size: 20px;
  font-family: 'Noto Serif SC', serif;
}

.nav-label {
  font-size: 10px;
}

@media (max-width: 428px) {
  .profile-header {
    padding: 16px;
  }
  
  .avatar-container {
    width: 60px;
    height: 60px;
  }
  
  .user-name {
    font-size: 18px;
  }
  
  .stats-row {
    gap: 12px;
    padding: 12px;
  }
  
  .stat-value {
    font-size: 16px;
  }
  
  .collection-grid {
    gap: 8px;
  }
  
  .collection-card {
    height: 120px;
  }
  
  .poem-card {
    height: 120px;
  }
  
  .seal-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .seal-card {
    height: 90px;
  }
}

.poem-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.poem-modal {
  width: 100%;
  max-width: 320px;
  background: linear-gradient(145deg, #FFF 0%, #F8F6F1 100%);
  border-radius: 16px;
  overflow: hidden;
  position: relative;
}

.poem-modal.has-image {
  max-width: 340px;
}

.poem-modal-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.poem-modal-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.poem-modal-image .image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60%;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.4));
}

.poem-modal.has-image .poem-modal-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 2;
  background: transparent;
  border-bottom: none;
}

.poem-modal.has-image .poem-modal-location {
  color: #FFF;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.poem-modal.has-image .poem-modal-close {
  background: rgba(255, 255, 255, 0.2);
  color: #FFF;
}

.poem-modal.has-image .poem-modal-body {
  position: relative;
  margin-top: -40px;
  z-index: 1;
}

.poem-modal.has-image .poem-modal-line {
  color: #FFF;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.4);
}

.poem-modal.has-image .poem-modal-footer {
  border-top-color: rgba(255, 255, 255, 0.2);
}

.poem-modal.has-image .poem-modal-date {
  color: rgba(255, 255, 255, 0.8);
}

.poem-modal::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #E8D5B7, #C9A227, #E8D5B7);
}

.poem-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 16px 12px;
  border-bottom: 1px solid rgba(45, 64, 89, 0.08);
}

.poem-modal-location {
  font-size: 15px;
  font-weight: 600;
  color: #2D4059;
}

.poem-modal-close {
  width: 28px;
  height: 28px;
  background: rgba(45, 64, 89, 0.06);
  border: none;
  border-radius: 50%;
  color: #8B9A9C;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.poem-modal-body {
  padding: 20px 24px;
}

.poem-modal-lines {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.poem-modal-line {
  font-size: 16px;
  color: #2D4059;
  line-height: 1.8;
  letter-spacing: 0.1em;
  margin: 0;
}

.poem-modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px dashed rgba(45, 64, 89, 0.1);
}

.poem-modal-date {
  font-size: 12px;
  color: #8B9A9C;
}

.poem-modal-ai {
  font-size: 11px;
  color: #C9A227;
  background: rgba(201, 162, 39, 0.1);
  padding: 3px 10px;
  border-radius: 12px;
}

.poem-modal-enter-active,
.poem-modal-leave-active {
  transition: opacity 0.25s ease;
}

.poem-modal-enter-from,
.poem-modal-leave-to {
  opacity: 0;
}

.poem-modal-enter-active .poem-modal,
.poem-modal-leave-active .poem-modal {
  transition: transform 0.25s ease;
}

.poem-modal-enter-from .poem-modal,
.poem-modal-leave-to .poem-modal {
  transform: scale(0.9);
}

.seal-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.seal-modal {
  width: 100%;
  max-width: 300px;
  background: linear-gradient(145deg, #FFF 0%, #F8F6F1 100%);
  border-radius: 16px;
  overflow: hidden;
  position: relative;
}

.seal-modal::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #E8D5B7, #C9A227, #E8D5B7);
}

.seal-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 16px 12px;
  border-bottom: 1px solid rgba(45, 64, 89, 0.08);
}

.seal-modal-title {
  font-size: 15px;
  font-weight: 600;
  color: #2D4059;
}

.seal-modal-close {
  width: 28px;
  height: 28px;
  background: rgba(45, 64, 89, 0.06);
  border: none;
  border-radius: 50%;
  color: #8B9A9C;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.seal-modal-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.seal-modal-stamp {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #C43C3C 0%, #8B2323 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: rotate(-3deg);
  box-shadow: 0 4px 12px rgba(196, 60, 60, 0.3);
}

.seal-modal-text {
  font-size: 24px;
  color: #FFF;
  writing-mode: vertical-rl;
  letter-spacing: 0.2em;
}

.seal-modal-desc {
  text-align: center;
}

.seal-modal-desc p {
  font-size: 14px;
  color: #2D4059;
  line-height: 1.6;
  margin: 0;
}

.poem-section {
  margin-bottom: 16px;
}

.add-card {
  border: 2px dashed rgba(45, 64, 89, 0.15);
  background: transparent;
}

.add-card:hover {
  border-color: rgba(45, 64, 89, 0.3);
  background: rgba(45, 64, 89, 0.02);
}

.add-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.add-icon {
  color: #8B9A9C;
}

.add-text {
  font-size: 12px;
  color: #8B9A9C;
}

.add-poem-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.add-poem-modal {
  width: 100%;
  max-width: 428px;
  background: #FDFBF7;
  border-radius: 20px 20px 0 0;
  max-height: 90vh;
  overflow-y: auto;
}

.add-poem-modal .modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(45, 64, 89, 0.08);
  position: sticky;
  top: 0;
  background: #FDFBF7;
  z-index: 1;
}

.add-poem-modal .modal-title {
  font-size: 18px;
  color: #2D4059;
  margin: 0;
}

.add-poem-modal .close-btn {
  width: 32px;
  height: 32px;
  background: rgba(45, 64, 89, 0.06);
  border: none;
  border-radius: 50%;
  color: #8B9A9C;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-poem-modal .modal-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.image-upload-area {
  width: 100%;
  height: 160px;
  border-radius: 12px;
  background: rgba(45, 64, 89, 0.03);
  border: 1px dashed rgba(45, 64, 89, 0.15);
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #8B9A9C;
}

.upload-placeholder svg {
  opacity: 0.5;
}

.upload-placeholder span {
  font-size: 13px;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hidden-input {
  display: none;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-label {
  font-size: 13px;
  color: #2D4059;
  font-weight: 500;
}

.input-field {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid rgba(45, 64, 89, 0.12);
  border-radius: 10px;
  font-size: 14px;
  color: #2D4059;
  background: #FFF;
  transition: border-color 0.2s;
}

.input-field:focus {
  outline: none;
  border-color: #C9A227;
}

.poem-type-toggle {
  display: flex;
  background: rgba(45, 64, 89, 0.04);
  border-radius: 10px;
  padding: 4px;
}

.poem-mode-area {
  margin-top: 16px;
}

.poem-sub-toggle {
  display: flex;
  background: rgba(45, 64, 89, 0.04);
  border-radius: 8px;
  padding: 3px;
  margin-bottom: 12px;
}

.sub-toggle-btn {
  flex: 1;
  padding: 8px 12px;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: 13px;
  color: #2D4059;
  cursor: pointer;
  transition: all 0.2s;
}

.sub-toggle-btn.active {
  background: #FFF;
  color: #C9A227;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.blog-mode-area {
  margin-top: 16px;
}

.blog-title-input {
  margin-bottom: 16px;
}

.blog-content-area {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.blog-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid rgba(45, 64, 89, 0.12);
  border-radius: 10px;
  font-size: 14px;
  color: #2D4059;
  background: #FFF;
  resize: none;
  transition: border-color 0.2s;
  font-family: inherit;
  line-height: 1.6;
}

.blog-textarea:focus {
  outline: none;
  border-color: #C9A227;
}

.blog-card .poem-content {
  background: linear-gradient(135deg, rgba(201, 162, 39, 0.1) 0%, rgba(45, 64, 89, 0.05) 100%);
}

.blog-preview {
  padding: 8px 0;
}

.blog-title-text {
  font-size: 15px;
  font-weight: 600;
  color: #2D4059;
  margin-bottom: 6px;
  line-height: 1.4;
}

.blog-excerpt {
  font-size: 12px;
  color: rgba(45, 64, 89, 0.7);
  line-height: 1.5;
}

.toggle-btn {
  flex: 1;
  padding: 10px 16px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 14px;
  color: #8B9A9C;
  cursor: pointer;
  transition: all 0.2s;
}

.toggle-btn.active {
  background: #FFF;
  color: #2D4059;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.custom-poem-area {
  width: 100%;
}

.poem-textarea {
  width: 100%;
  padding: 14px;
  border: 1px solid rgba(45, 64, 89, 0.12);
  border-radius: 10px;
  font-size: 15px;
  color: #2D4059;
  background: #FFF;
  resize: none;
  line-height: 1.8;
  letter-spacing: 0.05em;
}

.poem-textarea:focus {
  outline: none;
  border-color: #C9A227;
}

.ai-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}

.ai-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  color: #8B9A9C;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid rgba(201, 162, 39, 0.2);
  border-top-color: #C9A227;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.ai-lines {
  width: 100%;
  padding: 16px;
  background: rgba(201, 162, 39, 0.05);
  border-radius: 10px;
  text-align: center;
}

.ai-lines p {
  font-size: 15px;
  color: #2D4059;
  line-height: 1.8;
  margin: 0;
}

.ai-hint {
  padding: 16px;
  color: #8B9A9C;
  font-size: 13px;
}

.generate-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #C9A227 0%, #E8D5B7 100%);
  border: none;
  border-radius: 20px;
  color: #FFF;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.generate-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.generate-btn:not(:disabled):hover {
  opacity: 0.9;
}

.add-poem-modal .modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid rgba(45, 64, 89, 0.08);
  position: sticky;
  bottom: 0;
  background: #FDFBF7;
}

.cancel-btn {
  flex: 1;
  padding: 12px;
  background: rgba(45, 64, 89, 0.06);
  border: none;
  border-radius: 10px;
  color: #8B9A9C;
  font-size: 15px;
  cursor: pointer;
}

.save-btn {
  flex: 1;
  padding: 12px;
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  border: none;
  border-radius: 10px;
  color: #FFF;
  font-size: 15px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.save-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.save-btn:not(:disabled):hover {
  opacity: 0.9;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .add-poem-modal,
.modal-leave-active .add-poem-modal {
  transition: transform 0.3s ease;
}

.modal-enter-from .add-poem-modal,
.modal-leave-to .add-poem-modal {
  transform: translateY(100%);
}

.artifact-carousel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.artifact-carousel {
  width: 100%;
  max-width: 360px;
  background: #FDFBF7;
  border-radius: 20px;
  overflow: hidden;
}

.carousel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(45, 64, 89, 0.08);
}

.carousel-title {
  font-size: 18px;
  color: #2D4059;
  margin: 0;
}

.carousel-close {
  width: 32px;
  height: 32px;
  background: rgba(45, 64, 89, 0.06);
  border: none;
  border-radius: 50%;
  color: #8B9A9C;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-container {
  position: relative;
  overflow: hidden;
}

.carousel-track {
  display: flex;
  transition: transform 0.3s ease;
}

.carousel-slide {
  min-width: 100%;
  display: flex;
  flex-direction: column;
}

.slide-image {
  width: 100%;
  height: 240px;
  background: rgba(45, 64, 89, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
}

.slide-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.slide-info {
  padding: 16px 20px;
  text-align: center;
}

.slide-name {
  font-size: 18px;
  color: #2D4059;
  margin: 0 0 12px 0;
}

.slide-meta {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #8B9A9C;
}

.meta-item svg {
  opacity: 0.6;
}

.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 50%;
  color: #2D4059;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1;
}

.carousel-btn.prev {
  left: 10px;
}

.carousel-btn.next {
  right: 10px;
}

.carousel-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px 20px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(45, 64, 89, 0.15);
  cursor: pointer;
  transition: all 0.2s;
}

.dot.active {
  background: #C9A227;
  width: 20px;
  border-radius: 4px;
}

.carousel-enter-active,
.carousel-leave-active {
  transition: opacity 0.3s ease;
}

.carousel-enter-from,
.carousel-leave-to {
  opacity: 0;
}

.carousel-enter-active .artifact-carousel,
.carousel-leave-active .artifact-carousel {
  transition: transform 0.3s ease;
}

.carousel-enter-from .artifact-carousel,
.carousel-leave-to .artifact-carousel {
  transform: scale(0.9);
}

.slide-image.clickable {
  cursor: pointer;
  position: relative;
}

.slide-image.clickable:hover {
  opacity: 0.9;
}

.slide-image.clickable:hover .model-badge {
  opacity: 1;
}

.model-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  background: rgba(201, 162, 39, 0.9);
  border-radius: 16px;
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.model-badge svg {
  width: 14px;
  height: 14px;
}

.model-viewer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1100;
  padding: 20px;
}

.model-viewer {
  width: 100%;
  max-width: 400px;
  background: #1a1a1a;
  border-radius: 20px;
  overflow: hidden;
}

.model-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(45, 64, 89, 0.9);
}

.model-title {
  font-size: 18px;
  color: #FDFBF7;
  margin: 0;
}

.model-close {
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  color: #FDFBF7;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.model-container {
  width: 100%;
  height: 350px;
  background: #1a1a1a;
}

.model-controls {
  display: flex;
  gap: 12px;
  padding: 12px 20px;
  background: rgba(30, 30, 30, 0.95);
}

.control-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  background: rgba(201, 162, 39, 0.15);
  border: 1px solid rgba(201, 162, 39, 0.3);
  border-radius: 10px;
  color: #C9A227;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.control-btn:hover {
  background: rgba(201, 162, 39, 0.25);
}

.model-hint {
  text-align: center;
  padding: 12px 20px 16px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  background: rgba(30, 30, 30, 0.95);
}

.model-enter-active,
.model-leave-active {
  transition: opacity 0.3s ease;
}

.model-enter-from,
.model-leave-to {
  opacity: 0;
}

.model-enter-active .model-viewer,
.model-leave-active .model-viewer {
  transition: transform 0.3s ease;
}

.model-enter-from .model-viewer,
.model-leave-to .model-viewer {
  transform: scale(0.9);
}

.ar-btn {
  background: rgba(201, 162, 39, 0.2) !important;
  border: 1px solid rgba(201, 162, 39, 0.4) !important;
}

.ar-viewer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
}

.ar-viewer {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.ar-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(45, 64, 89, 0.9);
  z-index: 10;
}

.ar-title {
  font-size: 18px;
  color: #FDFBF7;
  margin: 0;
}

.ar-close {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  color: #FDFBF7;
  font-size: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ar-container {
  flex: 1;
  width: 100%;
  background: transparent;
}

.ar-controls {
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
}

.ar-control-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 32px;
  background: rgba(201, 162, 39, 0.9);
  border: none;
  border-radius: 50px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(201, 162, 39, 0.4);
}

.ar-control-btn:active {
  transform: scale(0.95);
}

.ar-hint {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  padding: 12px 20px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  background: rgba(0, 0, 0, 0.5);
  z-index: 10;
}

.ar-enter-active,
.ar-leave-active {
  transition: opacity 0.3s ease;
}

.ar-enter-from,
.ar-leave-to {
  opacity: 0;
}
</style>
