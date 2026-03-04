<!-- 统一地图页面 - 古风风格 -->
<template>
  <Layout 
    title="文迹地图" 
    :showBack="true"
    :showFooterQuote="!isMapExpanded"
    footerQuote="步履所至，皆成文迹"
  >
    <!-- 顶部功能切换标签 -->
    <div class="tab-container">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        @click="switchTab(tab.key)"
        :class="['tab-btn', { active: currentTab === tab.key }]"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span class="tab-text">{{ tab.name }}</span>
      </button>
    </div>

    <!-- 探索模式 -->
    <div v-if="currentTab === 'explore'" class="explore-container">
      <!-- 地图卡片 -->
      <div
        v-if="!isMapExpanded"
        class="map-card"
        @click="expandMap"
      >
        <div class="map-card-content">
          <div class="map-card-title">华夏非遗地图</div>
          <div class="map-card-hint">轻触展开，探索非遗足迹</div>
        </div>
      </div>

      <!-- 用户状态 -->
      <div v-if="!isMapExpanded" class="user-stats">
        <div class="stat-item">
          <span class="stat-label">经验值</span>
          <span class="stat-value">{{ userStats.exp }}/{{ userStats.maxExp }}</span>
        </div>
      </div>
    </div>

    <!-- 我的地图模式 -->
    <div v-if="currentTab === 'user'" class="user-map-container">
      <!-- 地图切换标签 -->
      <div class="map-tabs">
        <button
          v-for="map in userMaps"
          :key="map.mapId"
          @click="selectMap(map)"
          :class="{ active: currentMap?.mapId === map.mapId }"
          class="map-tab"
        >
          {{ map.mapName }}
          <span v-if="map.isDefault" class="default-badge">默认</span>
        </button>
        <button @click="showCreateDialog" class="map-tab create-tab">
          + 新建
        </button>
      </div>

      <!-- 地图操作按钮 -->
      <div class="map-actions" v-if="currentMap">
        <button @click="editMap" class="action-btn">
          <span class="btn-icon">✏️</span>
          <span class="btn-text">编辑</span>
        </button>
        <button @click="shareMap" class="action-btn">
          <span class="btn-icon">📤</span>
          <span class="btn-text">分享</span>
        </button>
        <button
          v-if="!currentMap.isDefault"
          @click="setDefault"
          class="action-btn"
        >
          <span class="btn-icon">⭐</span>
          <span class="btn-text">设为默认</span>
        </button>
        <button @click="deleteMap" class="action-btn delete">
          <span class="btn-icon">🗑️</span>
          <span class="btn-text">删除</span>
        </button>
      </div>

      <!-- 地图样式切换 -->
      <div class="style-selector" v-if="currentMap">
        <span class="style-label">地图样式：</span>
        <select v-model="currentMap.mapStyle" @change="updateMapStyle" class="style-select">
          <option value="light">浅色</option>
          <option value="dark">深色</option>
          <option value="retro">复古</option>
          <option value="ancient">古韵</option>
        </select>
      </div>

      <!-- 已点亮的文化点列表 -->
      <div class="visited-sites" v-if="currentMap && visitedSites.length > 0">
        <h3 class="section-title">已点亮的文化点 ({{ visitedSites.length }})</h3>
        <div class="sites-grid">
          <div
            v-for="site in visitedSites"
            :key="site.siteId"
            @click="focusOnSite(site)"
            class="site-item"
          >
            <img :src="site.coverImage" :alt="site.name" class="site-image" />
            <div class="site-info">
              <h4>{{ site.name }}</h4>
              <p>{{ site.category }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 全屏地图（两种模式共享） -->
    <div v-if="isMapExpanded" class="fullscreen-map-wrapper">
      <MapComponent 
        ref="mapComponent"
        :user-map="currentTab === 'user' ? currentMap : null"
        :map-style="currentTab === 'user' ? currentMap?.mapStyle : 'ancient'"
        @site-clicked="onSiteClicked" 
      />
      <button class="close-map-btn" @click.stop="collapseMap">
        <span class="close-icon">✕</span>
      </button>
    </div>

    <!-- 地点详情弹窗 -->
    <Teleport to="body" v-if="selectedSite">
      <div class="modal-overlay active">
        <div class="modal-content ancient-style">
          <div class="modal-header">
            <h2 class="modal-title">{{ selectedSite.name }}</h2>
            <button class="close-modal" @click="closeDetail">
              <span class="close-icon">✕</span>
            </button>
          </div>
          <div class="modal-image">{{ selectedSite.category }}</div>
          <p class="modal-desc">
            <span class="desc-label">类型：</span>{{ getSiteTypeText(selectedSite.type) }}<br />
            <span class="desc-label">状态：</span>{{ getStatusText(selectedSite.status) }}<br />
            <span class="desc-label">热度：</span>{{ Math.round((selectedSite.heat_level || 0) * 100) }}%
          </p>
          <button class="ar-button" @click="startAR">
            <span class="ar-icon">👓</span>
            <span>AR虚拟体验</span>
          </button>
        </div>
      </div>
    </Teleport>

    <!-- 创建/编辑地图对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="isEditMode ? '编辑地图' : '创建新地图'"
      width="90%"
      :close-on-click-modal="false"
      class="ancient-dialog"
    >
      <el-form :model="mapForm" label-width="80px" class="ancient-form">
        <el-form-item label="地图名称">
          <el-input v-model="mapForm.mapName" placeholder="请输入地图名称" />
        </el-form-item>
        <el-form-item label="地图描述">
          <el-input
            v-model="mapForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入地图描述"
          />
        </el-form-item>
        <el-form-item label="地图样式">
          <el-select v-model="mapForm.mapStyle">
            <el-option label="浅色" value="light" />
            <el-option label="深色" value="dark" />
            <el-option label="复古" value="retro" />
            <el-option label="古韵" value="ancient" />
          </el-select>
        </el-form-item>
        <el-form-item label="公开设置">
          <el-switch
            v-model="isPublic"
            active-text="公开"
            inactive-text="私有"
          />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false" class="ancient-btn">取消</el-button>
        <el-button type="primary" @click="saveMap" class="ancient-btn ancient-primary">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分享对话框 -->
    <el-dialog
      v-model="showShareDialog"
      title="分享地图"
      width="90%"
      class="ancient-dialog"
    >
      <el-form label-width="80px" class="ancient-form">
        <el-form-item label="分享内容">
          <el-input
            v-model="shareContent"
            type="textarea"
            :rows="3"
            placeholder="添加分享说明..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showShareDialog = false" class="ancient-btn">取消</el-button>
        <el-button type="primary" @click="confirmShare" class="ancient-btn ancient-primary">分享到社区</el-button>
      </template>
    </el-dialog>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Layout from '@/components/Layout.vue'
import MapComponent from './MapComponent.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentUserInfo } from '@/api/user'
import * as userMapApi from '@/api/userMap'
import { getHeritageSiteDetail } from '@/api/mapApi'

const router = useRouter()

// 标签页配置
const tabs = [
  { key: 'explore', name: '探索', icon: '🗺️' },
  { key: 'user', name: '我的地图', icon: '📍' }
]

// 状态
const currentTab = ref('explore')
const isMapExpanded = ref(false)
const selectedSite = ref(null)

// 用户数据
const userStats = ref({
  exp: 0,
  maxExp: 100
})

// 自定义地图相关
const userMaps = ref([])
const currentMap = ref(null)
const showDialog = ref(false)
const showShareDialog = ref(false)
const isEditMode = ref(false)
const isPublic = ref(false)
const isDefault = ref(false)
const shareContent = ref('')
const mapComponent = ref(null)
const visitedSites = ref([])

const mapForm = ref({
  mapId: null,
  mapName: '',
  description: '',
  mapStyle: 'ancient'
})

// 切换标签页
const switchTab = (tabKey) => {
  currentTab.value = tabKey
  if (tabKey === 'user') {
    fetchUserMaps()
  }
}

// 获取用户信息
const fetchUserStats = async () => {
  try {
    const response = await getCurrentUserInfo()
    if (response.code === 1) {
      userStats.value = {
        exp: response.data.experience,
        maxExp: response.data.maxExp || 500
      }
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
    console.error('获取用户信息失败:', error)
    userStats.value = {
      exp: 0,
      maxExp: 500
    }
  }
}

// 获取用户地图列表
const fetchUserMaps = async () => {
  try {
    const res = await userMapApi.getUserMaps()
    if (res.code === 1 || res.code === '200') {
      userMaps.value = res.data || []
      if (userMaps.value.length > 0) {
        const defaultMap = userMaps.value.find(m => m.isDefault === 1)
        currentMap.value = defaultMap || userMaps.value[0]
        await loadVisitedSites()
      }
    }
  } catch (error) {
    console.error('获取用户地图失败:', error)
    ElMessage.error('获取地图列表失败')
  }
}

// 加载已访问的文化点
const loadVisitedSites = async () => {
  if (!currentMap.value?.siteIds) return

  try {
    const siteIds = currentMap.value.siteIds.split(',').filter(id => id)
    if (siteIds.length === 0) return

    const sitePromises = siteIds.map(id => getHeritageSiteDetail(id))
    const sites = await Promise.all(sitePromises)
    visitedSites.value = sites.filter(site => site)
  } catch (error) {
    console.error('加载已访问文化点失败:', error)
  }
}

// 选择地图
const selectMap = (map) => {
  currentMap.value = map
  loadVisitedSites()
}

// 显示创建对话框
const showCreateDialog = () => {
  isEditMode.value = false
  mapForm.value = {
    mapId: null,
    mapName: '',
    description: '',
    mapStyle: 'ancient'
  }
  isPublic.value = false
  isDefault.value = false
  showDialog.value = true
}

// 编辑地图
const editMap = () => {
  if (!currentMap.value) return

  isEditMode.value = true
  mapForm.value = {
    mapId: currentMap.value.mapId,
    mapName: currentMap.value.mapName,
    description: currentMap.value.description,
    mapStyle: currentMap.value.mapStyle
  }
  isPublic.value = currentMap.value.isPublic === 1
  isDefault.value = currentMap.value.isDefault === 1
  showDialog.value = true
}

// 保存地图
const saveMap = async () => {
  try {
    const data = {
      ...mapForm.value,
      isPublic: isPublic.value ? 1 : 0,
      isDefault: isDefault.value ? 1 : 0,
      siteIds: currentMap.value?.siteIds || '',
      customMarkers: currentMap.value?.customMarkers || ''
    }

    let res
    if (isEditMode.value) {
      res = await userMapApi.updateUserMap(data)
    } else {
      res = await userMapApi.createUserMap(data)
    }

    if (res.code === 1 || res.code === '200') {
      ElMessage.success(isEditMode.value ? '更新成功' : '创建成功')
      showDialog.value = false
      await fetchUserMaps()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('保存地图失败:', error)
    ElMessage.error('保存失败')
  }
}

// 删除地图
const deleteMap = async () => {
  if (!currentMap.value) return

  try {
    await ElMessageBox.confirm(
      '确定要删除这个地图吗？删除后无法恢复。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await userMapApi.deleteUserMap(currentMap.value.mapId)
    if (res.code === 1 || res.code === '200') {
      ElMessage.success('删除成功')
      currentMap.value = null
      await fetchUserMaps()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除地图失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 设置默认地图
const setDefault = async () => {
  if (!currentMap.value) return

  try {
    const res = await userMapApi.setDefaultMap(currentMap.value.mapId)
    if (res.code === 1 || res.code === '200') {
      ElMessage.success('设置成功')
      await fetchUserMaps()
    } else {
      ElMessage.error(res.msg || '设置失败')
    }
  } catch (error) {
    console.error('设置默认地图失败:', error)
    ElMessage.error('设置失败')
  }
}

// 分享地图
const shareMap = () => {
  if (!currentMap.value) return

  if (currentMap.value.isPublic === 0) {
    ElMessage.warning('请先将地图设置为公开')
    return
  }

  shareContent.value = ''
  showShareDialog.value = true
}

// 确认分享
const confirmShare = async () => {
  try {
    const res = await userMapApi.shareMap({
      mapId: currentMap.value.mapId,
      shareType: 'community',
      shareContent: shareContent.value
    })

    if (res.code === 1 || res.code === '200') {
      ElMessage.success('分享成功')
      showShareDialog.value = false
    } else {
      ElMessage.error(res.msg || '分享失败')
    }
  } catch (error) {
    console.error('分享地图失败:', error)
    ElMessage.error('分享失败')
  }
}

// 更新地图样式
const updateMapStyle = async () => {
  if (!currentMap.value) return

  try {
    const res = await userMapApi.updateUserMap({
      mapId: currentMap.value.mapId,
      mapStyle: currentMap.value.mapStyle
    })

    if (res.code === 1 || res.code === '200') {
      if (mapComponent.value) {
        mapComponent.value.updateMapStyle(currentMap.value.mapStyle)
      }
    }
  } catch (error) {
    console.error('更新地图样式失败:', error)
  }
}

// 处理文化点点击
const onSiteClicked = (site) => {
  selectedSite.value = site
}

// 聚焦到指定文化点
const focusOnSite = (site) => {
  if (mapComponent.value) {
    mapComponent.value.focusOnSite(site)
  }
}

// 地图展开/收起
function expandMap() {
  isMapExpanded.value = true
  document.body.style.overflow = 'hidden'
}

function collapseMap() {
  isMapExpanded.value = false
  selectedSite.value = null
  document.body.style.overflow = ''
}

function closeDetail() {
  selectedSite.value = null
}

function startAR() {
  router.push('/user/ceramicAr')
  closeDetail()
}

// 辅助函数
const getSiteTypeText = (type) => {
  const map = {
    handicraft: '手工艺',
    museum: '博物馆',
    archaeology: '考古遗址',
    architecture: '传统建筑',
    food_craft: '食品工艺',
    performance: '表演艺术'
  }
  return map[type] || type
}

const getStatusText = (status) => {
  const map = {
    unlocked: '已解锁',
    locked: '未解锁',
    visited: '已访问'
  }
  return map[status] || status
}

// 生命周期
onMounted(() => {
  fetchUserStats()
  fetchUserMaps()
})
</script>

<style scoped>
/* 古风配色方案 */
:root {
  --ancient-primary: #8B7355;
  --ancient-secondary: #A68A64;
  --ancient-accent: #D4AF37;
  --ancient-bg: #F5F1E8;
  --ancient-text: #3E3B36;
  --ancient-border: #C4B88A;
}

/* 标签页容器 */
.tab-container {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  margin-bottom: 16px;
  border-bottom: 2px solid var(--ancient-border);
}

.tab-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: transparent;
  border: none;
  border-radius: 8px 8px 0 0;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.tab-btn::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--ancient-primary);
  transform: scaleX(0);
  transition: transform 0.3s;
}

.tab-btn.active::after {
  transform: scaleX(1);
}

.tab-icon {
  font-size: 24px;
}

.tab-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--ancient-text);
}

.tab-btn.active .tab-text {
  color: var(--ancient-primary);
}

/* 探索模式容器 */
.explore-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 地图卡片 */
.map-card {
  width: 100%;
  max-width: 320px;
  height: 160px;
  background: linear-gradient(135deg, var(--ancient-bg) 0%, #E8E0D5 100%);
  border: 2px solid var(--ancient-border);
  border-radius: 12px;
  margin: 0 auto;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.15);
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.map-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(139, 115, 85, 0.25);
}

.map-card::before {
  content: "🗺️";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 64px;
  opacity: 0.15;
}

.map-card-content {
  position: absolute;
  bottom: 16px;
  left: 16px;
  right: 16px;
  z-index: 1;
}

.map-card-title {
  font-weight: 700;
  font-size: 20px;
  color: var(--ancient-primary);
  margin-bottom: 8px;
  font-family: "Noto Serif SC", serif;
}

.map-card-hint {
  font-size: 14px;
  color: var(--ancient-secondary);
  opacity: 0.9;
}

/* 用户状态 */
.user-stats {
  display: flex;
  justify-content: center;
  padding: 16px;
  background: var(--ancient-bg);
  border: 1px solid var(--ancient-border);
  border-radius: 8px;
}

.stat-item {
  display: flex;
  gap: 12px;
  align-items: center;
  font-size: 14px;
  color: var(--ancient-text);
}

.stat-label {
  color: var(--ancient-secondary);
}

.stat-value {
  font-weight: 600;
  color: var(--ancient-primary);
}

/* 我的地图容器 */
.user-map-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 地图标签 */
.map-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 8px 0;
  border-bottom: 1px solid var(--ancient-border);
}

.map-tab {
  flex-shrink: 0;
  padding: 10px 20px;
  background: var(--ancient-bg);
  border: 1px solid var(--ancient-border);
  border-radius: 20px 20px 0 0;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  color: var(--ancient-text);
}

.map-tab.active {
  background: var(--ancient-primary);
  color: white;
  border-color: var(--ancient-primary);
}

.map-tab.create-tab {
  border-style: dashed;
}

.default-badge {
  margin-left: 6px;
  padding: 2px 8px;
  background: var(--ancient-accent);
  color: var(--ancient-text);
  font-size: 10px;
  border-radius: 10px;
  font-weight: 600;
}

/* 地图操作按钮 */
.map-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding: 12px 0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--ancient-bg);
  border: 1px solid var(--ancient-border);
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  color: var(--ancient-text);
}

.action-btn:hover {
  background: var(--ancient-secondary);
  color: white;
  border-color: var(--ancient-secondary);
}

.action-btn.delete {
  color: #C0392B;
  border-color: #C0392B;
}

.action-btn.delete:hover {
  background: #C0392B;
  color: white;
}

.btn-icon {
  font-size: 16px;
}

.btn-text {
  font-size: 14px;
}

/* 样式选择器 */
.style-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--ancient-bg);
  border: 1px solid var(--ancient-border);
  border-radius: 8px;
}

.style-label {
  font-size: 14px;
  color: var(--ancient-text);
  font-weight: 600;
}

.style-select {
  padding: 6px 12px;
  border: 1px solid var(--ancient-border);
  border-radius: 6px;
  background: white;
  color: var(--ancient-text);
  font-size: 14px;
}

/* 已访问站点 */
.visited-sites {
  background: var(--ancient-bg);
  border: 1px solid var(--ancient-border);
  border-radius: 8px;
  padding: 20px;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: var(--ancient-primary);
  font-family: "Noto Serif SC", serif;
  font-weight: 700;
  border-bottom: 1px solid var(--ancient-border);
  padding-bottom: 12px;
}

.sites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.site-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: white;
  border: 1px solid var(--ancient-border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.site-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.15);
}

.site-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--ancient-border);
}

.site-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.site-info h4 {
  margin: 0 0 6px 0;
  font-size: 14px;
  color: var(--ancient-text);
  font-weight: 600;
}

.site-info p {
  margin: 0;
  font-size: 12px;
  color: var(--ancient-secondary);
}

/* 全屏地图 */
.fullscreen-map-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 1000;
  background: var(--ancient-bg);
}

.fullscreen-map-wrapper :deep(.map-root-container) {
  width: 100% !important;
  height: 100% !important;
  border-radius: 0 !important;
}

.close-map-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 44px;
  height: 44px;
  border: 2px solid var(--ancient-primary);
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  color: var(--ancient-primary);
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.2);
  z-index: 2001;
  transition: all 0.3s;
}

.close-map-btn:hover {
  background: var(--ancient-primary);
  color: white;
  transform: scale(1.1);
}

.close-icon {
  font-size: 24px;
  font-weight: 600;
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(62, 59, 54, 0.7);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 2000;
}

.modal-content.ancient-style {
  background: linear-gradient(135deg, var(--ancient-bg) 0%, #E8E0D5 100%);
  border: 2px solid var(--ancient-primary);
  width: 90%;
  max-width: 360px;
  border-radius: 16px 16px 0 0;
  padding: 24px;
  box-shadow: 0 -4px 24px rgba(139, 115, 85, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--ancient-border);
}

.modal-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--ancient-primary);
  font-family: "Noto Serif SC", serif;
}

.close-modal {
  background: none;
  border: none;
  font-size: 20px;
  color: var(--ancient-secondary);
  cursor: pointer;
  width: 32px;
  height: 32px;
  transition: all 0.3s;
}

.close-modal:hover {
  color: var(--ancient-primary);
  transform: rotate(90deg);
}

.modal-image {
  width: 100%;
  height: 120px;
  background: var(--ancient-bg);
  border: 1px solid var(--ancient-border);
  border-radius: 8px;
  margin: 16px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ancient-secondary);
  font-size: 14px;
}

.modal-desc {
  font-size: 15px;
  line-height: 1.8;
  color: var(--ancient-text);
  margin: 16px 0;
}

.desc-label {
  color: var(--ancient-secondary);
  font-weight: 600;
}

.ar-button {
  background: linear-gradient(135deg, var(--ancient-primary), var(--ancient-secondary));
  color: white;
  border: none;
  border-radius: 12px;
  width: 100%;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  font-family: "Noto Serif SC", serif;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.2);
}

.ar-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(139, 115, 85, 0.3);
}

.ar-icon {
  font-size: 20px;
}

/* 古风对话框样式 */
.ancient-dialog :deep(.el-dialog) {
  background: var(--ancient-bg);
  border: 2px solid var(--ancient-primary);
  border-radius: 12px;
}

.ancient-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid var(--ancient-border);
  padding: 16px 20px;
}

.ancient-dialog :deep(.el-dialog__title) {
  color: var(--ancient-primary);
  font-family: "Noto Serif SC", serif;
  font-weight: 700;
  font-size: 18px;
}

.ancient-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.ancient-form :deep(.el-form-item__label) {
  color: var(--ancient-text);
  font-weight: 600;
}

.ancient-btn {
  border: 1px solid var(--ancient-border);
  color: var(--ancient-text);
  font-family: "Noto Serif SC", serif;
}

.ancient-btn:hover {
  background: var(--ancient-bg);
  border-color: var(--ancient-primary);
}

.ancient-primary {
  background: linear-gradient(135deg, var(--ancient-primary), var(--ancient-secondary));
  border: none;
  color: white;
}

.ancient-primary:hover {
  background: linear-gradient(135deg, var(--ancient-secondary), var(--ancient-primary));
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.2);
}
</style>
