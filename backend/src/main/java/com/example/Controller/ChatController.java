package com.example.Controller;


import com.example.Pojo.Entity.AI.AIChatMessage;
import com.example.Repository.ChatHistoryRepository;
import com.example.Service.AIChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.Media;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;

import static com.example.Common.Utils.GetUserIdUtils.getCurrentUserId;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/ai")
@Tag(name = "AI聊天管理", description = "AI聊天相关接口，支持文本和多模态对话")
public class ChatController {

    private final ChatClient chatClient;
    
    private final ChatHistoryRepository chatHistoryRepository;
        
    private final AIChatMessageService aiChatMessageService;


    // 修改为返回JSON格式的同步API
    @Operation(summary = "同步AI聊天", description = "同步方式与AI对话，支持文本和多模态输入")
    @PostMapping("/chat-sync")
    public ResponseEntity<Map<String, Object>> chatSync(
            @Parameter(description = "用户输入的提示词") @RequestParam("prompt") String prompt,
            @Parameter(description = "会话ID") @RequestParam("chatId") String chatId,
            @Parameter(description = "上传的文件列表，可选") @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        try {
            Long userId = getCurrentUserId();
            // 1.保存会话id和当前用户Id
            log.info("保存会话id和当前用户id：{},{}", userId,chatId);
            chatHistoryRepository.save("chat", chatId,userId);
            
            String response;
            // 2.请求模型
            if (files == null || files.isEmpty()) {
                // 没有附件，纯文本聊天
                response = textChatSync(prompt, chatId);
            } else {
                // 有附件，多模态聊天
                response = multiModalChatSync(prompt, chatId, files);
            }

            // 构建响应对象
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", response);
            responseData.put("chatId", chatId);
            
            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            log.error("同步聊天失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 保存对话到 MySQL（双写模式）
     */
   private void saveMessagesToDatabase(String chatId, Long userId, String userPrompt, String aiResponse) {
        try {
            // 提取数字类型的 chatId
           Long numericChatId = extractNumericChatId(chatId);
           
            // 保存用户消息
           AIChatMessage userMessage = AIChatMessage.builder()
                   .chatId(numericChatId)
                    .role("USER")
                    .content(userPrompt)
                    .messageType("TEXT")
                    .createTime(LocalDateTime.now())
                    .build();
            aiChatMessageService.saveMessage(userMessage);
            
            // 保存 AI 回复
           AIChatMessage aiMessage = AIChatMessage.builder()
                   .chatId(numericChatId)
                    .role("ASSISTANT")
                    .content(aiResponse)
                    .messageType("TEXT")
                    .createTime(LocalDateTime.now())
                    .build();
            aiChatMessageService.saveMessage(aiMessage);
            
            log.info("保存对话到 MySQL 成功，chatId: {}", chatId);
        } catch (Exception e) {
            log.error("保存对话到 MySQL 失败，chatId: {}", chatId, e);
            // 不抛出异常，避免影响用户体验
        }
    }

    /**
     * 从 sessionId 中提取数字部分
     * 格式：chat_timestamp_random → 提取 timestamp
     */
  private Long extractNumericChatId(String sessionId) {
        try {
            if (sessionId.startsWith("chat_")) {
                String[] parts = sessionId.substring(5).split("_");
                if (parts.length > 0) {
                    return Long.parseLong(parts[0]);
                }
            }
            // 如果已经是纯数字，直接转换
            return Long.parseLong(sessionId);
        } catch (Exception e) {
            log.error("解析 chatId 失败：{}", sessionId, e);
            return 0L;
        }
    }

    // 同步版本的文本聊天
    private String textChatSync(String prompt, String chatId) {
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .call()
                .content();
    }

    // 同步版本的多模态聊天
    private String multiModalChatSync(String prompt, String chatId, List<MultipartFile> files) {
        // 1.解析多媒体
        List<Media> medias = files.stream()
                .map(file -> new Media(
                                MimeType.valueOf(Objects.requireNonNull(file.getContentType())),
                                file.getResource()
                        )
                )
                .toList();
        // 2.请求模型
        return chatClient.prompt()
                .user(p -> p.text(prompt).media(medias.toArray(Media[]::new)))
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .call()
                .content();
    }

    // 保持原有的流式API，如果需要实时响应功能
    @Operation(summary = "流式AI聊天", description = "流式方式与AI对话，支持实时响应和多模态输入")
    @PostMapping(value = "/chat", produces = "text/plain;charset=utf-8")
    public Flux<String> chat(
            @Parameter(description = "用户输入的提示词") @RequestParam("prompt") String prompt,
            @Parameter(description = "会话ID") @RequestParam("chatId") String chatId,
            @Parameter(description = "上传的文件列表，可选") @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        // 1.保存会话id和用户Id
        Long userId = getCurrentUserId();
        log.info("多模态保存会话id和当前用户id：{},{}", userId,chatId);
        chatHistoryRepository.save("chat", chatId,userId);
        // 2.请求模型
        if (files == null || files.isEmpty()) {
            // 没有附件，纯文本聊天
            return textChat(prompt, chatId);
        } else {
            // 有附件，多模态聊天
            return multiModalChat(prompt, chatId, files);
        }

    }
    //这是纯文本聊天的实现
    private Flux<String> multiModalChat(String prompt, String chatId, List<MultipartFile> files) {
        // 1.解析多媒体
        List<Media> medias = files.stream()
                .map(file -> new Media(
                                MimeType.valueOf(Objects.requireNonNull(file.getContentType())),
                                file.getResource()
                        )
                )
                .toList();
        // 2.请求模型
        return chatClient.prompt()
                .user(p -> p.text(prompt).media(medias.toArray(Media[]::new)))
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()
                .content();
    }

    private Flux<String> textChat(String prompt, String chatId) {
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()
                .content();
    }



}
