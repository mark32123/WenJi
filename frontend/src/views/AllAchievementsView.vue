<template>
  <Layout title="所有成就" :showBack="true">
    <div class="all-achievements-page">
      <div class="achievements-list">
        <div
          v-for="(achievement, index) in allAchievements"
          :key="index"
          class="achievement-item"
          :class="{ 'locked': !achievement.unlocked }"
          @click="showAchievementFullscreen(achievement)"
        >
          <div class="achievement-icon">{{ achievement.icon }}</div>
          <div class="achievement-content">
            <div class="achievement-title">{{ achievement.title }}</div>
            <div class="achievement-desc">{{ achievement.description }}</div>
            <div class="achievement-status">{{ achievement.unlocked ? '已解锁' : '未解锁' }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 全屏成就展示 -->
    <div v-if="selectedAchievement" class="achievement-fullscreen" @click="closeAchievementFullscreen">
      <div class="achievement-fullscreen-content" @click.stop>
        <div class="achievement-fullscreen-close" @click="closeAchievementFullscreen">×</div>
        <div class="achievement-fullscreen-icon">{{ selectedAchievement.icon }}</div>
        <div class="achievement-fullscreen-info">
          <div class="achievement-fullscreen-title">{{ selectedAchievement.title }}</div>
          <div class="achievement-fullscreen-desc">{{ selectedAchievement.description }}</div>
          <div class="achievement-fullscreen-status">{{ selectedAchievement.unlocked ? '已解锁' : '未解锁' }}</div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref } from 'vue';
import Layout from '@/components/Layout.vue';

// 所有成就数据
const allAchievements = ref([
  { icon: '🎖️', title: '文化使者', description: '成功完成三次AI导游讲解任务。', unlocked: true },
  { icon: '🔍', title: '历史探寻者', description: '参观了五个不同的历史文化景点。', unlocked: true },
  { icon: '🏛️', title: '古建筑专家', description: '探索并了解十处古建筑的历史文化。', unlocked: false },
  { icon: '🎨', title: '艺术鉴赏家', description: '参观五个不同类型的艺术展览。', unlocked: false },
  { icon: '🍵', title: '茶道大师', description: '深入了解并体验三种不同的茶文化。', unlocked: false },
  { icon: '🎭', title: '戏曲爱好者', description: '观看并了解三种传统戏曲。', unlocked: false },
  { icon: '🧵', title: '手工艺达人', description: '体验并学习五种传统手工艺。', unlocked: false },
  { icon: '📜', title: '古籍研究者', description: '研究并了解五部古籍文献。', unlocked: false },
  { icon: '🎪', title: '民俗文化探索者', description: '体验五种不同的民俗文化活动。', unlocked: false },
  { icon: '🏺', title: '陶瓷文化专家', description: '深入了解陶瓷文化并完成相关任务。', unlocked: false },
  { icon: '🎭', title: '戏曲传承人', description: '深入了解戏曲文化并完成相关任务。', unlocked: false },
  { icon: '🎯', title: '任务达人', description: '完成100个探索任务。', unlocked: false },
  { icon: '🌟', title: '星级探索者', description: '获得10个徽章。', unlocked: false },
  { icon: '🏆', title: '成就大师', description: '解锁所有成就。', unlocked: false },
  { icon: '🗺️', title: '地图探索者', description: '探索所有省份的非遗景点。', unlocked: false }
]);

const selectedAchievement = ref(null);

const showAchievementFullscreen = (achievement) => {
  selectedAchievement.value = achievement;
  document.body.style.overflow = 'hidden';
};

const closeAchievementFullscreen = () => {
  selectedAchievement.value = null;
  document.body.style.overflow = '';
};
</script>

<style scoped>
.all-achievements-page {
  padding: 20px;
}

.achievements-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 10px 0;
}

.achievement-item {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
}

.achievement-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.achievement-item.locked {
  opacity: 0.7;
}

.achievement-icon {
  font-size: 36px;
  margin-right: 15px;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.achievement-content {
  flex: 1;
}

.achievement-title {
  font-size: 16px;
  font-weight: 600;
  color: #5A524A;
  margin-bottom: 5px;
}

.achievement-desc {
  font-size: 13px;
  color: #8C7B6B;
  line-height: 1.5;
  margin-bottom: 5px;
}

.achievement-status {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  background: #E8F5E9;
  color: #4CAF50;
}

.achievement-item.locked .achievement-status {
  background: #F5F5F5;
  color: #999;
}

/* 全屏成就展示样式 */
.achievement-fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.achievement-fullscreen-content {
  background: white;
  border-radius: 16px;
  padding: 30px;
  max-width: 320px;
  width: 90%;
  text-align: center;
  position: relative;
}

.achievement-fullscreen-close {
  position: absolute;
  top: 10px;
  right: 15px;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  line-height: 1;
}

.achievement-fullscreen-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.achievement-fullscreen-info {
  text-align: left;
}

.achievement-fullscreen-title {
  font-size: 20px;
  font-weight: 600;
  color: #5A524A;
  margin-bottom: 10px;
}

.achievement-fullscreen-desc {
  font-size: 14px;
  color: #8C7B6B;
  line-height: 1.6;
  margin-bottom: 15px;
}

.achievement-fullscreen-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  background: #E8F5E9;
  color: #4CAF50;
}

.achievement-fullscreen-content.locked .achievement-fullscreen-status {
  background: #F5F5F5;
  color: #999;
}
</style>
