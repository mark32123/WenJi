package com.example.Controller.AI;


import com.example.Common.Result;
import com.example.VO.AI.MessageVO;
import com.example.VO.AI.ChatSessionVO;
import com.example.Repository.ChatHistoryRepository;
import com.example.Mapper.AIChatSessionMapper;
import com.example.Pojo.Entity.AI.AIChatSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.Common.Utils.GetUserIdUtils.getCurrentUserId;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/history")
@Tag(name = "AI聊天历史管理", description = "AI聊天历史记录相关接口")
public class ChatHistoryController {

    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatMemory chatMemory;
    private final AIChatSessionMapper chatSessionMapper;

    @Operation(summary = "获取聊天 ID 列表", description = "根据类型获取所有聊天会话 ID")
    @GetMapping("/{type}/list")
    public Result<List<String>> getChatIds(@Parameter(description = "聊天类型") @PathVariable("type") String type){
        log.info("=== 收到获取聊天历史列表请求，type: {} ===", type);
        List<String> chatIds = chatHistoryRepository.getChatIds(type);
        log.info("获取询问历史 id{}",chatIds);
        log.info("返回数据：{}", chatIds);
        return Result.success(chatIds);
    }

    @Operation(summary = "获取聊天历史详情", description = "根据类型和会话 ID 获取完整的聊天历史记录")
    @GetMapping("/{type}/{chatId}")//为了与上面的方法进行区分，先执行上面的方法，再执行下面的方法
    public Result<List<ChatSessionVO>> getChatHistory(
            @Parameter(description = "聊天类型") @PathVariable("type") String type, 
            @Parameter(description = "会话 ID") @PathVariable("chatId") String chatId){
        log.info("=== 收到获取聊天历史详情请求，type: {}, chatId: {} ===", type, chatId);
        
        // 1. 获取当前登录用户 ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录", null);
        }
        
        // 2. 验证 chatId 是否属于当前用户
        AIChatSession session = chatSessionMapper.selectById(chatId);
        if (session == null || !session.getUserId().equals(userId)) {
            log.error("无权访问该会话，chatId: {}, userId: {}", chatId, userId);
            return Result.error("无权访问该会话", null);
        }
        
        // 3. 验证通过后才从 Redis 中获取消息
        List<Message> messages = chatMemory.get(chatId, Integer.MAX_VALUE);
        if(messages==null){
            messages = List.of();
        }
        log.info("从 Redis 获取到 {} 条消息", messages.size());
    
        List<MessageVO> messageVOs = messages.stream().map(MessageVO::new).toList();
        ChatSessionVO sessionVO = new ChatSessionVO(chatId, LocalDateTime.now(), messageVOs);
        List<ChatSessionVO> sessionList = List.of(sessionVO);
        log.info("返回会话详情：{}", sessionList);
        return Result.success(sessionList);
    }
}