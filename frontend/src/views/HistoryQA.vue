<template>
  <Layout title="历史问答" :showBack="true">
    <div class="history-container">
      <h2 class="page-title">历史问答</h2>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="sessions.length === 0" class="empty">暂无历史记录</div>

      <div v-else class="sessions-list">
        <div
          v-for="session in sessions"
          :key="session.session_id"
          class="session-card"
        >
          
          <div class="qa-list">
            <div
              v-for="(pair, idx) in convertToQAPairs(session.messages)"
              :key="idx"
              class="qa"
            >
              <div class="q">问：{{ pair.question }}</div>
              <div class="a">答：{{ pair.answer }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import Layout from '@/components/Layout.vue'
import { ref, onMounted } from 'vue'
import { chatHistoryService, getSingleChatHistory } from '@/api/user'

const sessions = ref([])
const loading = ref(true)

function convertToQAPairs(messages) {
  const pairs = []
  for (let i = 0; i < messages.length; i++) {
    if (messages[i].role === 'user') {
      const q = messages[i].content
      const a = (i + 1 < messages.length && messages[i + 1].role === 'assistant')
        ? messages[i + 1].content
        : '（暂无回答）'
      if (messages[i + 1]?.role === 'assistant') i++ // skip assistant
      pairs.push({ question: q, answer: a })
    }
  }
  return pairs
}

// function formatDate(isoStr) {
//   if (!isoStr) return '未知时间'
//   const d = new Date(isoStr)
//   return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
// }

onMounted(async () => {
  console.log('=== 开始加载历史问答 ===')
  
  try {
    // 第一步：获取会话 ID 列表
    console.log('步骤 1: 获取会话列表...')
    const response = await chatHistoryService('chat');
    console.log('✓ 会话列表响应:', JSON.stringify(response, null, 2));
    console.log('  - response.code:', response?.code);
    console.log('  - response.data:', response?.data);
    console.log('  - response.msg:', response?.msg);
    
    let sessionIds = [];
    if (response && typeof response === 'object' && response.code === 1 && Array.isArray(response.data)) {
      sessionIds = response.data;
      console.log('✓ 提取出会话 ID 数组:', sessionIds);
      console.log('  - 数组长度:', sessionIds.length);
      console.log('  - 数组内容:', JSON.stringify(sessionIds));
    } else {
      console.error('✗ 会话列表响应格式错误:', response);
      console.error('  - code 应该是 1，实际是:', response?.code);
      console.error('  - data 应该是数组，实际是:', typeof response?.data, Array.isArray(response?.data));
      sessionIds = [];
    }
    
    // 第二步：如果没有会话 ID，直接返回
    if (!Array.isArray(sessionIds) || sessionIds.length === 0) {
      console.log('暂无历史记录');
      sessions.value = [];
      loading.value = false;
      return;
    }
    
    console.log(`步骤 2: 获取 ${sessionIds.length} 个会话的详情...`);
    
    // 第三步：逐个获取会话详情
    const validSessions = [];
    
    for (const sessionId of sessionIds) {
      try {
        console.log(`\n  --- 获取会话 ${sessionId} ---`);
        const sessionDetailResponse = await getSingleChatHistory('chat', sessionId);
        console.log(`  ✓ 会话 ${sessionId} 响应:`, JSON.stringify(sessionDetailResponse, null, 2));
        console.log(`    - code:`, sessionDetailResponse?.code);
        console.log(`    - data 类型:`, typeof sessionDetailResponse?.data, Array.isArray(sessionDetailResponse?.data) ? '(数组)' : '(非数组)');
        console.log(`    - data 长度:`, Array.isArray(sessionDetailResponse?.data) ? sessionDetailResponse.data.length : 'N/A');
        
        // 检查返回格式是否正确
        if (sessionDetailResponse && sessionDetailResponse.code === 1 && Array.isArray(sessionDetailResponse.data)) {
          const sessionDetail = sessionDetailResponse.data[0];
          if (sessionDetail) {
            console.log(`    ✓ 成功获取会话 ${sessionId}`);
            console.log(`      - sessionDetail:`, sessionDetail);
            validSessions.push(sessionDetail);
          } else {
            console.warn(`    ⚠ 会话 ${sessionId} 数据为空`);
          }
        } else {
          console.warn(`    ✗ 会话 ${sessionId} 响应格式不正确:`);
          console.warn(`      - code:`, sessionDetailResponse?.code, '(应该是 1)');
          console.warn(`      - data 是数组吗？`, Array.isArray(sessionDetailResponse?.data));
        }
      } catch (singleSessionErr) {
        console.error(`    ✗ 获取会话 ${sessionId} 详情失败:`, singleSessionErr);
        console.error(`      - 错误类型:`, typeof singleSessionErr);
        console.error(`      - 错误消息:`, singleSessionErr?.message);
        console.error(`      - 完整错误:`, singleSessionErr);
        // 继续处理下一个会话
      }
    }
    
    console.log(`\n步骤 3: 成功获取 ${validSessions.length}/${sessionIds.length} 个会话`);
    
    // 第四步：按开始时间排序（最新的在前）
    validSessions.sort((a, b) => new Date(b.start_time) - new Date(a.start_time));
    
    sessions.value = validSessions;
    console.log('\n✓ 最终会话数据:', validSessions);
    
  } catch (err) {
    console.error('\n✗ 加载历史失败:', err);
    console.error('  - 错误类型:', typeof err);
    console.error('  - 错误是否为字符串？', typeof err === 'string');
    console.error('  - 错误是否为对象？', typeof err === 'object');
    console.error('  - 错误消息:', err?.message);
    console.error('  - 错误堆栈:', err?.stack);
    console.error('  - 完整错误:', JSON.stringify(err, Object.getOwnPropertyNames(err), 2));
    sessions.value = [];
  } finally {
    loading.value = false;
    console.log('\n=== 加载完成 ===');
  }
});
</script>

<style scoped>
/* 复用之前的样式，略作调整 */
.history-container {
  font-family: "Noto Serif SC", serif;
  background: #f8f5f0;
  padding: 20px;
  min-height: 100vh;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #3a3530;
  margin-bottom: 16px;
}
.loading, .empty {
  color: #8c7b6b;
  text-align: center;
  padding: 20px;
}
.session-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}
.session-header {
  font-size: 14px;
  color: #a68a64;
  margin-bottom: 12px;
  font-weight: 500;
}
.qa {
  margin-bottom: 12px;
}
.q {
  font-weight: 600;
  color: #3a3530;
}
.a {
  color: #8c7b6b;
  margin-top: 6px;
  line-height: 1.5;
}
.qa:last-child {
  margin-bottom: 0;
}
</style>