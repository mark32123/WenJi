import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  const userId = ref(null)
  const username = ref('')
  const avatar = ref('')
  const phone = ref('')
  const email = ref('')
  const gender = ref(null)
  const realName = ref('')
  const location = ref('')
  const birthday = ref(null)
  
  const experience = ref(0)
  const points = ref(0)
  const level = ref('初识')
  const poems = ref([])
  const seals = ref([])
  const artifacts = ref([])
  const visitedSites = ref([])
  const totalDistance = ref(0)
  
  const loading = ref(false)
  const error = ref(null)
  
  const levelThresholds = [
    { name: '初识', minExp: 0 },
    { name: '登堂', minExp: 100 },
    { name: '入室', minExp: 300 },
    { name: '登峰', minExp: 600 },
    { name: '造极', minExp: 1000 },
    { name: '大成', minExp: 2000 }
  ]

  const currentLevel = computed(() => {
    const sorted = [...levelThresholds].sort((a, b) => b.minExp - a.minExp)
    return sorted.find(t => experience.value >= t.minExp)?.name || '初识'
  })

  const nextLevel = computed(() => {
    const currentIndex = levelThresholds.findIndex(t => t.name === currentLevel.value)
    if (currentIndex < levelThresholds.length - 1) {
      const nextThreshold = levelThresholds[currentIndex + 1].minExp
      const required = nextThreshold - experience.value
      if (required > 0) {
        return {
          name: levelThresholds[currentIndex + 1].name,
          required: required
        }
      }
    }
    return null
  })

  const poemCount = computed(() => poems.value.length)
  const sealCount = computed(() => seals.value.length)
  const artifactCount = computed(() => artifacts.value.length)
  const visitedCount = computed(() => visitedSites.value.length)

  const fetchUserInfo = async () => {
    loading.value = true
    error.value = null
    
    try {
      const result = await userApi.getCurrentUser()
      
      if (result.success && result.data) {
        const data = result.data
        username.value = data.username || ''
        phone.value = data.phone || ''
        email.value = data.email || ''
        gender.value = data.gender
        realName.value = data.realName || ''
        location.value = data.location || ''
        birthday.value = data.birthday || null
        avatar.value = data.avatarUrl || ''
        experience.value = data.experience || data.points || 0
        points.value = data.points || data.experience || 0
        level.value = data.level || '初识'
        
        return { success: true, data }
      } else {
        error.value = result.message || '获取用户信息失败'
        return { success: false, error: result.message }
      }
    } catch (e) {
      error.value = e.message || '网络错误'
      return { success: false, error: e.message }
    } finally {
      loading.value = false
    }
  }

  const updateUserInfo = async (updateData) => {
    loading.value = true
    error.value = null
    
    try {
      const result = await userApi.updateUserInfo(updateData)
      
      if (result.success && result.data) {
        const data = result.data
        username.value = data.username || username.value
        phone.value = data.phone || phone.value
        email.value = data.email || email.value
        gender.value = data.gender !== undefined ? data.gender : gender.value
        realName.value = data.realName || realName.value
        location.value = data.location || location.value
        birthday.value = data.birthday || birthday.value
        avatar.value = data.avatarUrl || avatar.value
        experience.value = data.experience || data.points || experience.value
        points.value = data.points || data.experience || points.value
        level.value = data.level || level.value
        
        return { success: true, data }
      } else {
        error.value = result.message || '更新用户信息失败'
        return { success: false, error: result.message }
      }
    } catch (e) {
      error.value = e.message || '网络错误'
      return { success: false, error: e.message }
    } finally {
      loading.value = false
    }
  }

  const addExperience = (amount) => {
    const oldLevel = currentLevel.value
    experience.value += amount
    
    if (currentLevel.value !== oldLevel) {
      return {
        leveledUp: true,
        oldLevel,
        newLevel: currentLevel.value,
        gainedExp: amount
      }
    }
    
    return {
      leveledUp: false,
      gainedExp: amount
    }
  }

  const unlockPoem = (poem) => {
    if (!poems.value.some(p => p.id === poem.id)) {
      poems.value.push({
        id: poem.id,
        title: poem.title,
        author: poem.author,
        content: poem.content,
        dynasty: poem.dynasty,
        unlockedAt: Date.now()
      })
      return { success: true, isNew: true }
    }
    return { success: true, isNew: false }
  }

  const collectSeal = (seal) => {
    if (!seals.value.some(s => s.id === seal.id)) {
      seals.value.push({
        id: seal.id,
        name: seal.name,
        text: seal.text,
        imageUrl: seal.imageUrl,
        collectedAt: Date.now()
      })
      return { success: true, isNew: true }
    }
    return { success: true, isNew: false }
  }

  const collectArtifact = (artifact) => {
    if (!artifacts.value.some(a => a.id === artifact.id)) {
      artifacts.value.push({
        id: artifact.id,
        name: artifact.name,
        imageUrl: artifact.imageUrl,
        poem: artifact.poem,
        dynasty: artifact.dynasty,
        location: artifact.location,
        collectedAt: Date.now()
      })
      return { success: true, isNew: true }
    }
    return { success: true, isNew: false }
  }

  const visitSite = (site) => {
    if (!visitedSites.value.some(s => s.id === site.id)) {
      visitedSites.value.push({
        id: site.id,
        name: site.name,
        location: site.location,
        visitedAt: Date.now()
      })
      return { success: true, isNew: true }
    }
    return { success: true, isNew: false }
  }

  const addDistance = (distance) => {
    totalDistance.value += distance
  }

  const resetProgress = () => {
    experience.value = 0
    level.value = '初识'
    poems.value = []
    seals.value = []
    artifacts.value = []
    visitedSites.value = []
    totalDistance.value = 0
  }

  const clearUser = () => {
    userId.value = null
    username.value = ''
    avatar.value = ''
    phone.value = ''
    email.value = ''
    gender.value = null
    realName.value = ''
    location.value = ''
    birthday.value = null
    experience.value = 0
    points.value = 0
    level.value = '初识'
  }

  const loadFromStorage = () => {
    try {
      const saved = localStorage.getItem('wenji_user_store')
      if (saved) {
        const data = JSON.parse(saved)
        userId.value = data.userId || null
        username.value = data.username || ''
        avatar.value = data.avatar || ''
        experience.value = data.experience || 0
        level.value = data.level || '初识'
        poems.value = data.poems || []
        seals.value = data.seals || []
        visitedSites.value = data.visitedSites || []
        totalDistance.value = data.totalDistance || 0
        
        const validArtifacts = (data.artifacts || []).filter(a => {
          const validIds = ['qinghuaci', 'fencaici', 'qinghuacimianju', 'beijing1', 'beijing2', 'beijing3', 'beijing4']
          return validIds.includes(a.id)
        }).map(a => {
          const artifactData = {
            qinghuaci: { name: '青花瓷瓶', imageUrl: '/images/qinghuaci.jpg', dynasty: '明代', location: '景德镇', date: '2024.03.15' },
            fencaici: { name: '粉彩瓷瓶', imageUrl: '/images/fencaici.jpg', dynasty: '清代', location: '景德镇', date: '2024.03.16' },
            qinghuacimianju: { name: '青花瓷面具', imageUrl: '/images/qinghuacimianju.jpg', dynasty: '明代', location: '景德镇', date: '2024.03.17' },
            beijing1: { name: '故宫太和殿', imageUrl: '/images/gugong-taihedian.jpg', dynasty: '明清', location: '北京', date: '2024.03.18' },
            beijing2: { name: '长城烽火台', imageUrl: '/images/changcheng-fenghuotai.jpg', dynasty: '明代', location: '北京', date: '2024.03.19' },
            beijing3: { name: '天坛祈年殿', imageUrl: '/images/tiantan-qiniandian.jpg', dynasty: '明代', location: '北京', date: '2024.03.20' },
            beijing4: { name: '颐和园长廊', imageUrl: '/images/yiheyuan-changlang.jpg', dynasty: '清代', location: '北京', date: '2024.03.21' }
          }
          return {
            id: a.id,
            ...artifactData[a.id],
            collectedAt: a.collectedAt || Date.now()
          }
        })
        artifacts.value = validArtifacts.length > 0 ? validArtifacts : [
          { id: 'qinghuaci', name: '青花瓷瓶', imageUrl: '/images/qinghuaci.jpg', dynasty: '明代', location: '景德镇', date: '2024.03.15', collectedAt: Date.now() },
          { id: 'fencaici', name: '粉彩瓷瓶', imageUrl: '/images/fencaici.jpg', dynasty: '清代', location: '景德镇', date: '2024.03.16', collectedAt: Date.now() },
          { id: 'qinghuacimianju', name: '青花瓷面具', imageUrl: '/images/qinghuacimianju.jpg', dynasty: '明代', location: '景德镇', date: '2024.03.17', collectedAt: Date.now() },
          { id: 'beijing1', name: '故宫太和殿', imageUrl: '/images/gugong-taihedian.jpg', dynasty: '明清', location: '北京', date: '2024.03.18', collectedAt: Date.now() },
          { id: 'beijing2', name: '长城烽火台', imageUrl: '/images/changcheng-fenghuotai.jpg', dynasty: '明代', location: '北京', date: '2024.03.19', collectedAt: Date.now() }
        ]
      }
    } catch (e) {
      console.warn('加载用户数据失败:', e)
    }
  }

  const saveToStorage = () => {
    try {
      const data = {
        userId: userId.value,
        username: username.value,
        avatar: avatar.value,
        experience: experience.value,
        level: level.value,
        poems: poems.value,
        seals: seals.value,
        artifacts: artifacts.value,
        visitedSites: visitedSites.value,
        totalDistance: totalDistance.value
      }
      localStorage.setItem('wenji_user_store', JSON.stringify(data))
    } catch (e) {
      console.warn('保存用户数据失败:', e)
    }
  }

  return {
    userId,
    username,
    avatar,
    phone,
    email,
    gender,
    realName,
    location,
    birthday,
    experience,
    points,
    level,
    poems,
    seals,
    artifacts,
    visitedSites,
    totalDistance,
    loading,
    error,
    currentLevel,
    nextLevel,
    poemCount,
    sealCount,
    artifactCount,
    visitedCount,
    fetchUserInfo,
    updateUserInfo,
    addExperience,
    unlockPoem,
    collectSeal,
    collectArtifact,
    visitSite,
    addDistance,
    resetProgress,
    clearUser,
    loadFromStorage,
    saveToStorage
  }
})
