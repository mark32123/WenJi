package com.example.Pojo.Entity.AI;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("ai_chat_session")
public class AIChatSession {
    private String sessionId;        // 会话ID
    private Integer userId;          // 关联User.userId
    private String currentLocation;  // 用户当前位置(经纬度)
    private String sessionContext;   // 会话上下文(JSON格式)
    private Integer messageCount;    // 消息数量
    private LocalDateTime startTime; // 会话开始时间
    private LocalDateTime lastActiveTime; // 最后活跃时间
    private String status;           // 状态: active/ended
}
