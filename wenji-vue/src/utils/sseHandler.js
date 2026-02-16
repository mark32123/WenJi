// utils/sseHandler.js
export class SSEHandler {
  constructor() {
    this.onData = null;
    this.onError = null;
    this.onComplete = null;
  }

  async sendStreamRequest(url, formData, onDataCallback) {
    try {
      // 获取存储的认证令牌 - 修改这里，使用 'token' 而不是 'authToken'
      const token = localStorage.getItem('token'); // 改为 'token'
      
      const options = {
        method: 'POST',
        body: formData,
      };
      
      // 如果存在认证令牌，则添加到请求头
      if (token) {
        options.headers = {
          'Authorization': `Bearer ${token}`,
        };
      }

      const response = await fetch(url, options);

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      // 检查响应是否支持ReadableStream
      if (!response.body) {
        throw new Error('ReadableStream not supported');
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();

      while (true) {
        const { done, value } = await reader.read();
        
        if (done) {
          if (this.onComplete) this.onComplete();
          break;
        }

        const chunk = decoder.decode(value, { stream: true });
        const lines = chunk.split('\n');

        for (const line of lines) {
          // 处理SSE格式的数据
          if (line.startsWith('data: ')) {
            let data = line.substring(6);
            
            if (data === '[DONE]') {
              if (this.onComplete) this.onComplete();
              break;
            }
            
            // 如果是JSON格式，尝试解析
            try {
              data = JSON.parse(data);
            } catch(e) {
              // 如果不是JSON格式，保持原样
            }
            
            if (this.onData) this.onData(data);
            if (onDataCallback) onDataCallback(data);
          }
        }
      }
      
      reader.releaseLock();
    } catch (error) {
      console.error('SSE Error:', error);
      if (this.onError) this.onError(error);
    } finally {
      // 确保reader被释放
    }
  }
}