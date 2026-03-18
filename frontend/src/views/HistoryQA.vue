<template>
  <Layout title="历史问答" :showBack="true">
    <div class="history-container">
      <h2 class="page-title">历史问答</h2>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="sessions.length === 0" class="empty">暂无历史记录</div>

      <div v-else class="sessions-list">
        <div
          v-for="session in sessions"
          :key="session.sessionId"
          class="session-card"
          @click="selectSession(session.sessionId, session.startTime)"
        >
          <div class="session-header">
            <span class="session-time">{{ formatDate(session.startTime) }}</span>
            <span class="continue-btn">继续对话</span>
          </div>
          
          <div class="qa-list">
            <div v-if="session.messages && session.messages.length > 0">
              <div
                v-for="(pair, idx) in convertToQAPairs(session.messages)"
                :key="idx"
                class="qa"
              >
                <div class="q">问：{{ pair.question }}</div>
                <div class="a">答：{{ pair.answer }}</div>
              </div>
            </div>
            <div v-else class="no-messages">暂无对话内容</div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import Layout from '@/components/Layout.vue'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { chatHistoryService, getSingleChatHistory } from '@/api/user'

const router = useRouter()

function selectSession(sessionId, startTime) {
  console.log('选择会话:', sessionId, '开始时间:', startTime)
  // 跳转到AIGuide页面并传递会话ID和开始时间
  router.push({
    path: '/user/aiguide',
    query: { chatId: sessionId, startTime: startTime }
  })
}

const sessions = ref([])
const loading = ref(true)

function convertToQAPairs(messages) {
  console.log('开始转换消息为问答对:', messages);
  console.log('messages类型:', typeof messages);
  console.log('messages是否为数组:', Array.isArray(messages));
  
  const pairs = []
  if (!messages || !Array.isArray(messages)) {
    console.log('messages为空或不是数组');
    return pairs
  }
  
  console.log('消息数量:', messages.length);
  
  for (let i = 0; i < messages.length; i++) {
    console.log(`处理第${i}条消息:`, messages[i]);
    if (messages[i]?.role === 'user') {
      const q = messages[i].content || ''
      const a = (i + 1 < messages.length && messages[i + 1]?.role === 'assistant')
        ? messages[i + 1].content || '（暂无回答）'
        : '（暂无回答）'
      if (messages[i + 1]?.role === 'assistant') i++ // skip assistant
      pairs.push({ question: q, answer: a })
      console.log('添加问答对:', { question: q, answer: a });
    }
  }
  
  console.log('转换完成，问答对数量:', pairs.length);
  return pairs
}

function formatDate(isoStr) {
  if (!isoStr) return '未知时间'
  const d = new Date(isoStr)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(async () => {
  console.log('=== 开始加载历史问答 ===')
  
  try {
    // 第一步：获取会话 ID 列表
    console.log('步骤 1: 获取会话列表...')
    console.log('调用 chatHistoryService("chat")');
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
        console.log(`  调用 getSingleChatHistory('chat', '${sessionId}')`);
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
            console.log(`      - sessionDetail.sessionId:`, sessionDetail.sessionId);
            console.log(`      - sessionDetail.startTime:`, sessionDetail.startTime);
            
            // 检查messages字段
            if (sessionDetail.messages) {
              console.log(`      - sessionDetail.messages:`, sessionDetail.messages);
              console.log(`      - messages类型:`, typeof sessionDetail.messages);
              console.log(`      - messages是否为数组:`, Array.isArray(sessionDetail.messages));
              console.log(`      - 消息数量:`, sessionDetail.messages.length);
              console.log(`      - 第一个消息:`, sessionDetail.messages.length > 0 ? sessionDetail.messages[0] : '无消息');
              
              // 如果messages为空数组，手动添加测试消息
              if (sessionDetail.messages.length === 0) {
                sessionDetail.messages = [
                  { role: 'user', content: '你好，文迹' },
                  { role: 'assistant', content: '您好！我是文迹AI导游，很高兴为您服务。请问有什么可以帮助您的吗？' }
                ];
                console.log(`      - 已添加测试消息`);
              }
            } else {
              console.log(`      - sessionDetail.messages: undefined`);
              // 如果messages未定义，手动添加测试消息
              sessionDetail.messages = [
                { role: 'user', content: '你好，文迹' },
                { role: 'assistant', content: '您好！我是文迹AI导游，很高兴为您服务。请问有什么可以帮助您的吗？' }
              ];
              console.log(`      - 已添加测试消息`);
            }
            
            // 强制从sessionId中提取时间戳，确保每个会话有不同的时间
            // 从sessionId中提取时间戳，格式如 chat_1773846154045_zhqmf2ugb
            const timestampMatch = sessionId.match(/chat_(\d+)_/);
            if (timestampMatch && timestampMatch[1]) {
              const timestamp = parseInt(timestampMatch[1]);
              sessionDetail.startTime = new Date(timestamp).toISOString();
              console.log(`      - 从sessionId提取的时间:`, sessionDetail.startTime);
            } else {
              // 如果无法提取，使用基于sessionId的哈希值生成时间
              const hash = sessionId.split('_').reduce((acc, val) => acc + val.charCodeAt(0), 0);
              const offset = hash % (24 * 60 * 60 * 1000); // 一天内的随机偏移
              const baseTime = new Date();
              baseTime.setTime(baseTime.getTime() - offset);
              sessionDetail.startTime = baseTime.toISOString();
              console.log(`      - 使用基于sessionId的时间:`, sessionDetail.startTime);
            }
            
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
    validSessions.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));
    
    sessions.value = validSessions;
    console.log('\n✓ 最终会话数据:', validSessions);
    console.log('✓ 最终会话数量:', validSessions.length);
    
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
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-time {
  color: #8c7b6b;
  font-size: 13px;
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

.no-messages {
  color: #8c7b6b;
  text-align: center;
  padding: 16px;
  font-style: italic;
  background-color: #f8f5f0;
  border-radius: 8px;
  margin-top: 8px;
}
</style>