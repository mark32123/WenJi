<template>
  <Layout title="所有徽章" :showBack="true">
    <div class="all-badges-page">
      <div class="badges-grid">
        <div
          v-for="(badge, index) in allBadges"
          :key="index"
          class="badge-item"
          :class="{ 'locked': !badge.unlocked }"
          @click="showBadgeFullscreen(badge)"
        >
          <div class="badge-circle">
            <div class="badge-text">{{ badge.text }}</div>
          </div>
          <div class="badge-name">{{ badge.name }}</div>
          <div class="badge-status">{{ badge.unlocked ? '已解锁' : '未解锁' }}</div>
        </div>
      </div>
    </div>

    <!-- 全屏徽章展示 -->
    <div v-if="selectedBadge" class="badge-fullscreen" @click="closeBadgeFullscreen">
      <div class="badge-fullscreen-content" @click.stop>
        <div class="badge-fullscreen-close" @click="closeBadgeFullscreen">×</div>
        <div class="badge-fullscreen-badge">
          <div class="badge-fullscreen-inner">
            <div class="badge-fullscreen-text">{{ selectedBadge.text }}</div>
          </div>
        </div>
        <div class="badge-fullscreen-info">
          <div class="badge-fullscreen-title">{{ selectedBadge.name }}</div>
          <div class="badge-fullscreen-desc">{{ selectedBadge.description }}</div>
          <div class="badge-fullscreen-status">{{ selectedBadge.unlocked ? '已解锁' : '未解锁' }}</div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref } from 'vue';
import Layout from '@/components/Layout.vue';

// 所有徽章数据
const allBadges = ref([
  { text: '茶艺\n传人', name: '茶艺传人', description: '通过学习茶艺文化并完成相关任务获得', unlocked: true },
  { text: '绣坊\n学徒', name: '绣坊学徒', description: '参观刺绣工坊并了解刺绣技艺获得', unlocked: true },
  { text: '瓷匠', name: '瓷匠', description: '探索瓷器文化并完成相关挑战获得', unlocked: true },
  { text: '古建\n守护者', name: '古建守护者', description: '参观多处古建筑并了解其历史文化获得', unlocked: true },
  { text: '园林\n鉴赏家', name: '园林鉴赏家', description: '游览苏州园林并完成相关任务获得', unlocked: false },
  { text: '陶艺\n大师', name: '陶艺大师', description: '深入了解陶艺制作过程并完成作品获得', unlocked: false },
  { text: '书法\n爱好者', name: '书法爱好者', description: '学习书法知识并完成相关练习获得', unlocked: false },
  { text: '非遗\n守护者', name: '非遗守护者', description: '探索多个非遗项目并完成相关任务获得', unlocked: false },
  { text: '茶道\n行者', name: '茶道行者', description: '探索各地茶文化并完成相关任务获得', unlocked: false },
  { text: '漆器\n匠人', name: '漆器匠人', description: '了解漆器制作工艺并完成相关任务获得', unlocked: false },
  { text: '剪纸\n艺术家', name: '剪纸艺术家', description: '学习剪纸技艺并完成作品获得', unlocked: false },
  { text: '玉雕\n大师', name: '玉雕大师', description: '了解玉雕文化并完成相关任务获得', unlocked: false },
  { text: '皮影\n传承人', name: '皮影传承人', description: '探索皮影戏文化并完成相关任务获得', unlocked: false },
  { text: '木偶\n艺术家', name: '木偶艺术家', description: '了解木偶戏文化并完成相关任务获得', unlocked: false },
  { text: '瓷器\n专家', name: '瓷器专家', description: '深入了解瓷器文化并完成相关任务获得', unlocked: false },
  { text: '文化\n使者', name: '文化使者', description: '完成多个非遗文化探索任务获得', unlocked: false }
]);

const selectedBadge = ref(null);

const showBadgeFullscreen = (badge) => {
  selectedBadge.value = badge;
  document.body.style.overflow = 'hidden';
};

const closeBadgeFullscreen = () => {
  selectedBadge.value = null;
  document.body.style.overflow = '';
};
</script>

<style scoped>
.all-badges-page {
  padding: 20px;
}

.badges-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  padding: 10px 0;
}

.badge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.badge-item:hover {
  transform: translateY(-5px);
}

.badge-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #A68A64 0%, #8B7355 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  box-shadow: 0 4px 8px rgba(166, 138, 100, 0.3);
  transition: all 0.3s ease;
}

.badge-item.locked .badge-circle {
  background: #E0E0E0;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.badge-text {
  font-size: 18px;
  font-weight: 600;
  color: white;
  text-align: center;
  white-space: pre-line;
  line-height: 1.3;
}

.badge-item.locked .badge-text {
  color: #999;
}

.badge-name {
  font-size: 14px;
  font-weight: 600;
  color: #5A524A;
  margin-bottom: 4px;
}

.badge-status {
  font-size: 12px;
  color: #8C7B6B;
}

.badge-item.locked .badge-status {
  color: #999;
}

/* 全屏徽章展示样式 */
.badge-fullscreen {
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

.badge-fullscreen-content {
  background: white;
  border-radius: 16px;
  padding: 30px;
  max-width: 320px;
  width: 90%;
  text-align: center;
  position: relative;
}

.badge-fullscreen-close {
  position: absolute;
  top: 10px;
  right: 15px;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  line-height: 1;
}

.badge-fullscreen-badge {
  margin-bottom: 20px;
}

.badge-fullscreen-inner {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: linear-gradient(135deg, #A68A64 0%, #8B7355 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  box-shadow: 0 8px 16px rgba(166, 138, 100, 0.4);
}

.badge-fullscreen-text {
  font-size: 28px;
  font-weight: 600;
  color: white;
  text-align: center;
  white-space: pre-line;
  line-height: 1.3;
}

.badge-fullscreen-info {
  text-align: left;
}

.badge-fullscreen-title {
  font-size: 20px;
  font-weight: 600;
  color: #5A524A;
  margin-bottom: 10px;
}

.badge-fullscreen-desc {
  font-size: 14px;
  color: #8C7B6B;
  line-height: 1.6;
  margin-bottom: 15px;
}

.badge-fullscreen-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  background: #E8F5E9;
  color: #4CAF50;
}

.badge-fullscreen-content.locked .badge-fullscreen-inner {
  background: #E0E0E0;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.badge-fullscreen-content.locked .badge-fullscreen-text {
  color: #999;
}

.badge-fullscreen-content.locked .badge-fullscreen-status {
  background: #F5F5F5;
  color: #999;
}
</style>
