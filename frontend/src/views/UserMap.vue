<template>
  <Layout title="我的文化地图" :showBack="true">
    <div class="user-map-container">
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
          ✏️ 编辑
        </button>
        <button @click="shareMap" class="action-btn">
          📤 分享
        </button>
        <button 
          v-if="!currentMap.isDefault" 
          @click="setDefault" 
          class="action-btn"
        >
          ⭐ 设为默认
        </button>
        <button @click="deleteMap" class="action-btn delete">
          🗑️ 删除
        </button>
      </div>

      <!-- 地图样式切换 -->
      <div class="style-selector" v-if="currentMap">
        <span>地图样式：</span>
        <select v-model="currentMap.mapStyle" @change="updateMapStyle">
          <option value="light">浅色</option>
          <option value="dark">深色</option>
          <option value="retro">复古</option>
          <option value="custom">自定义</option>
        </select>
      </div>

      <!-- 地图容器 -->
      <div class="map-wrapper">
        <MapComponent 
          ref="mapComponent"
          :user-map="currentMap"
          @site-clicked="handleSiteClicked"
        />
      </div>

      <!-- 已点亮的文化点列表 -->
      <div class="visited-sites" v-if="currentMap && visitedSites.length > 0">
        <h3>已点亮的文化点 ({{ visitedSites.length }})</h3>
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

      <!-- 创建/编辑地图对话框 -->
      <el-dialog
        v-model="showDialog"
        :title="isEditMode ? '编辑地图' : '创建新地图'"
        width="90%"
        :close-on-click-modal="false"
      >
        <el-form :model="mapForm" label-width="80px">
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
              <el-option label="自定义" value="custom" />
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
          <el-button @click="showDialog = false">取消</el-button>
          <el-button type="primary" @click="saveMap">确定</el-button>
        </template>
      </el-dialog>

      <!-- 分享对话框 -->
      <el-dialog
        v-model="showShareDialog"
        title="分享地图"
        width="90%"
      >
        <el-form label-width="80px">
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
          <el-button @click="showShareDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmShare">分享到社区</el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import MapComponent from './MapComponent.vue'
import * as userMapApi from '@/api/userMap'
import { getHeritageSiteDetail } from '@/api/mapApi'

const router = useRouter()

// 响应式数据
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
  mapStyle: 'light'
})

// 获取用户地图列表
const fetchUserMaps = async () => {
  try {
    const res = await userMapApi.getUserMaps()
    if (res.code === 1 || res.code === '200') {
      userMaps.value = res.data || []
      // 如果有地图，选择第一个或默认地图
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
    mapStyle: 'light'
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
      // 刷新地图以应用新样式
      if (mapComponent.value) {
        mapComponent.value.updateMapStyle(currentMap.value.mapStyle)
      }
    }
  } catch (error) {
    console.error('更新地图样式失败:', error)
  }
}

// 处理文化点点击
const handleSiteClicked = (site) => {
  console.log('点击文化点:', site)
  // 可以在这里跳转到详情页或显示更多信息
}

// 聚焦到指定文化点
const focusOnSite = (site) => {
  if (mapComponent.value) {
    mapComponent.value.focusOnSite(site)
  }
}

// 生命周期
onMounted(() => {
  fetchUserMaps()
})
</script>

<style scoped>
.user-map-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  box-sizing: border-box;
}

.map-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.map-tab {
  flex-shrink: 0;
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #e5e2dd;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.map-tab.active {
  background: #A68A64;
  color: #fff;
  border-color: #A68A64;
}

.map-tab.create-tab {
  border-style: dashed;
}

.default-badge {
  margin-left: 4px;
  padding: 2px 6px;
  background: #FFD700;
  color: #333;
  font-size: 10px;
  border-radius: 8px;
}

.map-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #e5e2dd;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn:hover {
  background: #f5f5f5;
}

.action-btn.delete {
  color: #f56c6c;
  border-color: #f56c6c;
}

.style-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 8px;
  font-size: 14px;
}

.style-selector select {
  padding: 4px 8px;
  border: 1px solid #e5e2dd;
  border-radius: 4px;
}

.map-wrapper {
  flex: 1;
  min-height: 400px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.visited-sites {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.visited-sites h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #333;
}

.sites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.site-item {
  display: flex;
  gap: 12px;
  padding: 8px;
  background: #f8f5f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.site-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.site-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
}

.site-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.site-info h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #333;
}

.site-info p {
  margin: 0;
  font-size: 12px;
  color: #666;
}
</style>
