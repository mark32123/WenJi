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
  console.log('正在获取会话列表...')
  
  try {
    // 获取会话列表
    const response = await chatHistoryService('chat');
    
    // 验证响应格式
    let sessionIds = [];
    if (response && typeof response === 'object' && response.code === 1 && Array.isArray(response.data)) {
      sessionIds = response.data;
      console.log('提取出会话ID数组:', sessionIds);
    } else {
      console.error('会话列表响应格式错误:', response);
      sessionIds = [];
    }
    
    // 检查是否是数组
    if (Array.isArray(sessionIds)) {
      console.log('会话ID数组长度:', sessionIds.length);
      
      if (sessionIds.length > 0) {
        // 逐个获取会话详情
        const validSessions = [];
        
        for (const sessionId of sessionIds) {
          try {
            const sessionDetailResponse = await getSingleChatHistory('chat', sessionId);
            
            // 检查返回格式是否正确
            if (sessionDetailResponse && sessionDetailResponse.code === 1 && Array.isArray(sessionDetailResponse.data)) {
              // 假设 data 是一个包含单个会话的数组
              const sessionDetail = sessionDetailResponse.data[0];
              if (sessionDetail) {
                validSessions.push(sessionDetail);
              }
            } else {
              console.warn(`会话 ${sessionId} 详情响应格式不正确:`, sessionDetailResponse);
            }
          } catch (singleSessionErr) {
            console.error(`获取会话 ${sessionId} 详情失败:`, singleSessionErr);
            console.error('单个会话错误详情:', {
              name: singleSessionErr?.name,
              message: singleSessionErr?.message,
              stack: singleSessionErr?.stack,
              errorType: typeof singleSessionErr,
              response: singleSessionErr?.response
            });
            // 继续处理下一个会话，而不是中断整个流程
          }
        }
        
        // 按开始时间排序（最新的在前）
        validSessions.sort((a, b) => new Date(b.start_time) - new Date(a.start_time));
        
        sessions.value = validSessions;
        console.log('最终会话数据:', validSessions);
      } else {
        // 如果没有会话ID，就设置为空数组
        sessions.value = [];
      }
    } else {
      console.error('会话ID列表格式错误:', sessionIds);
      sessions.value = [];
    }
  } catch (err) {
    console.error('加载历史失败 - 错误在:', err);
    console.error('错误类型:', typeof err);
    console.error('错误是否为对象:', typeof err === 'object');
    
    // 特殊情况处理：如果错误实际上是一个成功响应，就正常处理
    if (err && typeof err === 'object' && err.code === '200' && Array.isArray(err.data)) {
      console.log('检测到响应被误认为错误，直接处理数据...');
      const sessionIds = err.data;
      
      if (Array.isArray(sessionIds)) {
        console.log('会话ID数组长度:', sessionIds.length);
        
        if (sessionIds.length > 0) {
          // 逐个获取会话详情
          const validSessions = [];
          
          for (const sessionId of sessionIds) {
            try {
              const sessionDetailResponse = await getSingleChatHistory('chat', sessionId);
              // 检查返回格式是否正确
              if (sessionDetailResponse && sessionDetailResponse.code === '200' && Array.isArray(sessionDetailResponse.data)) {
                // 假设 data 是一个包含单个会话的数组
                const sessionDetail = sessionDetailResponse.data[0];
                if (sessionDetail) {
                  validSessions.push(sessionDetail);
                }
              } else {
                console.warn(`会话 ${sessionId} 详情响应格式不正确:`, sessionDetailResponse);
              }
            } catch (singleSessionErr) {
              console.error(`获取会话 ${sessionId} 详情失败:`, singleSessionErr);
              // 继续处理下一个会话
            }
          }
          
          // 按开始时间排序（最新的在前）
          validSessions.sort((a, b) => new Date(b.start_time) - new Date(a.start_time));
          
          sessions.value = validSessions;
          console.log('最终会话数据:', validSessions);
        } else {
          sessions.value = [];
        }
      }
    } else {
      console.error('真正的错误:', err);
      sessions.value = [];
    }
  } finally {
    loading.value = false;
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