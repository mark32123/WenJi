package com.example.VO.AI;

import com.example.Pojo.AIChatMessage;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

@NoArgsConstructor
@Data
public class MessageVO {
    private String role;
    private String content;

    public MessageVO(Message message) {
        this.role = switch (message.getMessageType()) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
            case SYSTEM -> "system";
            default -> "";
        };
        this.content = message.getText();
    }
    
    /**
     * 从数据库实体 AIChatMessage 转换
     */
  public MessageVO(AIChatMessage aiChatMessage) {
        this.role = aiChatMessage.getRole().toLowerCase();
        this.content = aiChatMessage.getContent();
    }
}