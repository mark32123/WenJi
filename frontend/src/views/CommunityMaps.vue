<template>
  <Layout title="社区地图" :showBack="true">
    <div class="community-maps-container">
      <!-- 筛选和排序 -->
      <div class="filter-bar">
        <el-select v-model="sortBy" placeholder="排序方式" @change="loadMaps">
          <el-option label="最受欢迎" value="popular" />
          <el-option label="最新发布" value="latest" />
          <el-option label="最多浏览" value="viewed" />
        </el-select>
      </div>

      <!-- 地图列表 -->
      <div class="maps-list" v-loading="loading">
        <div 
          v-for="map in maps" 
          :key="map.mapId"
          @click="viewMapDetail(map)"
          class="map-card"
        >
          <div class="map-header">
            <img :src="map.avatarUrl || '/default-avatar.png'" class="user-avatar" />
            <div class="user-info">
              <h3>{{ map.mapName }}</h3>
              <p>{{ map.username }}</p>
            </div>
            <el-button 
              v-if="map.isLiked" 
              type="primary" 
              size="small"
              @click.stop="unlikeMap(map)"
            >
              已赞 {{ map.likeCount }}
            </el-button>
            <el-button 
              v-else 
              size="small"
              @click.stop="likeMap(map)"
            >
              点赞 {{ map.likeCount }}
            </el-button>
          </div>

          <p class="map-description">{{ map.description }}</p>

          <div class="map-stats">
            <span>👁️ {{ map.viewCount }}</span>
            <span>📤 {{ map.shareCount }}</span>
            <span>🕐 {{ formatDate(map.createTime) }}</span>
          </div>

          <div class="map-preview">
            <div class="site-count">
              已点亮 {{ map.siteIds ? map.siteIds.split(',').length : 0 }} 个文化点
            </div>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div class="load-more" v-if="hasMore">
        <el-button @click="loadMore" :loading="loadingMore">
          加载更多
        </el-button>
      </div>

      <!-- 地图详情对话框 -->
      <el-dialog
        v-model="showDetailDialog"
        title="地图详情"
        width="90%"
        :close-on-click-modal="false"
      >
        <div v-if="selectedMap" class="map-detail">
          <div class="detail-header">
            <img :src="selectedMap.avatarUrl || '/default-avatar.png'" class="detail-avatar" />
            <div class="detail-info">
              <h2>{{ selectedMap.mapName }}</h2>
              <p>创建者：{{ selectedMap.username }}</p>
              <p>{{ selectedMap.description }}</p>
            </div>
          </div>

          <div class="detail-stats">
            <div class="stat-item">
              <span class="stat-value">{{ selectedMap.likeCount }}</span>
              <span class="stat-label">点赞</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ selectedMap.viewCount }}</span>
              <span class="stat-label">浏览</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ selectedMap.shareCount }}</span>
              <span class="stat-label">分享</span>
            </div>
          </div>

          <div class="detail-actions">
            <el-button 
              v-if="selectedMap.isLiked" 
              type="primary"
              @click="unlikeMap(selectedMap)"
            >
              取消点赞
            </el-button>
            <el-button 
              v-else 
              type="primary"
              @click="likeMap(selectedMap)"
            >
              点赞
            </el-button>
            <el-button @click="shareMap(selectedMap)">
              分享
            </el-button>
            <el-button @click="useMap(selectedMap)">
              使用此地图
            </el-button>
          </div>
        </div>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Layout from '@/components/Layout.vue'
import * as userMapApi from '@/api/userMap'

const router = useRouter()

// 响应式数据
const maps = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const sortBy = ref('popular')
const showDetailDialog = ref(false)
const selectedMap = ref(null)
const page = ref(1)
const pageSize = ref(10)

// 加载地图列表
const loadMaps = async () => {
  loading.value = true
  page.value = 1
  hasMore.value = true

  try {
    const res = await userMapApi.getPublicMaps({
      page: page.value,
      pageSize: pageSize.value
    })

    if (res.code === 1 || res.code === '200') {
      maps.value = res.data || []
      hasMore.value = maps.value.length >= pageSize.value
    }
  } catch (error) {
    console.error('加载社区地图失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return

  loadingMore.value = true
  page.value++

  try {
    const res = await userMapApi.getPublicMaps({
      page: page.value,
      pageSize: pageSize.value
    })

    if (res.code === 1 || res.code === '200') {
      const newMaps = res.data || []
      maps.value = [...maps.value, ...newMaps]
      hasMore.value = newMaps.length >= pageSize.value
    }
  } catch (error) {
    console.error('加载更多失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loadingMore.value = false
  }
}

// 查看地图详情
const viewMapDetail = (map) => {
  selectedMap.value = map
  showDetailDialog.value = true
}

// 点赞地图
const likeMap = async (map) => {
  try {
    const res = await userMapApi.likeMap(map.mapId)
    if (res.code === 1 || res.code === '200') {
      map.isLiked = true
      map.likeCount++
      ElMessage.success('点赞成功')
    } else {
      ElMessage.error(res.msg || '点赞失败')
    }
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('点赞失败')
  }
}

// 取消点赞
const unlikeMap = async (map) => {
  try {
    const res = await userMapApi.unlikeMap(map.mapId)
    if (res.code === 1 || res.code === '200') {
      map.isLiked = false
      map.likeCount--
      ElMessage.success('取消点赞成功')
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('取消点赞失败:', error)
    ElMessage.error('操作失败')
  }
}

// 分享地图
const shareMap = async (map) => {
  try {
    const res = await userMapApi.shareMap({
      mapId: map.mapId,
      shareType: 'community',
      shareContent: ''
    })

    if (res.code === 1 || res.code === '200') {
      ElMessage.success('分享成功')
      map.shareCount++
    } else {
      ElMessage.error(res.msg || '分享失败')
    }
  } catch (error) {
    console.error('分享失败:', error)
    ElMessage.error('分享失败')
  }
}

// 使用地图（复制或参考）
const useMap = (map) => {
  ElMessage.info('功能开发中，敬请期待')
  // TODO: 实现复制地图功能
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`

  return date.toLocaleDateString()
}

// 生命周期
onMounted(() => {
  loadMaps()
})
</script>

<style scoped>
.community-maps-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  box-sizing: border-box;
}

.filter-bar {
  display: flex;
  justify-content: flex-end;
  padding: 8px 0;
}

.maps-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  overflow-y: auto;
}

.map-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.map-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.map-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  flex: 1;
}

.user-info h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #333;
}

.user-info p {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.map-description {
  margin: 8px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.map-stats {
  display: flex;
  gap: 16px;
  margin: 12px 0;
  font-size: 12px;
  color: #999;
}

.map-preview {
  padding: 8px;
  background: #f5f5f5;
  border-radius: 8px;
}

.site-count {
  font-size: 13px;
  color: #666;
  text-align: center;
}

.load-more {
  text-align: center;
  padding: 16px;
}

.map-detail {
  padding: 16px;
}

.detail-header {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.detail-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.detail-info {
  flex: 1;
}

.detail-info h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #333;
}

.detail-info p {
  margin: 4px 0;
  font-size: 14px;
  color: #666;
}

.detail-stats {
  display: flex;
  justify-content: space-around;
  margin: 24px 0;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 600;
  color: #A68A64;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.detail-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
