package com.example.Controller;


import com.example.Repository.ChatHistoryRepository;
import com.example.Service.AIChatMessageService;
import com.example.Pojo.Entity.AI.AIChatMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.Media;
import org.springframework.util.MimeType;
import org.springframework.http.MediaType;
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
@Tag(name = "AI 聊天管理", description = "AI 聊天相关接口，支持文本和多模态对话")
public class ChatController {

    private final ChatClient chatClient;
    
    private final ChatHistoryRepository chatHistoryRepository;
    
    private final AIChatMessageService chatMessageService;

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
        List<Media> medias = files.stream()
                .map(file -> new Media(
                                MimeType.valueOf(Objects.requireNonNull(file.getContentType())),
                                file.getResource()
                        )
                )
                .toList();
        
        return chatClient.prompt()
                .user(p -> p.text(prompt).media(medias.toArray(Media[]::new)))
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .call()
                .content();
    }

    // 流式版本的文本聊天
    private Flux<String> textChat(String prompt, String chatId) {
        StringBuilder fullResponse = new StringBuilder();
        Long userId = getCurrentUserId();
        
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()
                .content()
                .doOnNext(chunk -> fullResponse.append(chunk))
                .doOnComplete(() -> {
                    // 保存AI回复
                    if (userId != null && !fullResponse.isEmpty()) {
                        try {
                            AIChatMessage aiMessage = AIChatMessage.builder()
                                    .chatId(chatId)
                                    .sessionId(chatId)
                                    .role("assistant")
                                    .content(fullResponse.toString())
                                    .messageType("text")
                                    .createTime(LocalDateTime.now())
                                    .build();
                            chatMessageService.saveMessage(aiMessage);
                            log.debug("保存AI回复成功，chatId: {}", chatId);
                        } catch (Exception e) {
                            log.error("保存AI回复失败", e);
                        }
                    }
                });
    }
    
    // 流式版本的多模态聊天
    private Flux<String> multiModalChat(String prompt, String chatId, List<MultipartFile> files) {
        List<Media> medias = files.stream()
                .map(file -> new Media(
                                MimeType.valueOf(Objects.requireNonNull(file.getContentType())),
                                file.getResource()
                        )
                )
                .toList();
        
        StringBuilder fullResponse = new StringBuilder();
        Long userId = getCurrentUserId();
        
        return chatClient.prompt()
                .user(p -> p.text(prompt).media(medias.toArray(Media[]::new)))
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()
                .content()
                .doOnNext(chunk -> fullResponse.append(chunk))
                .doOnComplete(() -> {
                    // 保存AI回复
                    if (userId != null && !fullResponse.isEmpty()) {
                        try {
                            AIChatMessage aiMessage = AIChatMessage.builder()
                                    .chatId(chatId)
                                    .sessionId(chatId)
                                    .role("assistant")
                                    .content(fullResponse.toString())
                                    .messageType("multi-modal")
                                    .createTime(LocalDateTime.now())
                                    .build();
                            chatMessageService.saveMessage(aiMessage);
                            log.debug("保存AI回复成功，chatId: {}", chatId);
                        } catch (Exception e) {
                            log.error("保存AI回复失败", e);
                        }
                    }
                });
    }

    /**
     * AI 聊天接口（支持文本和多模态）
     * @param prompt 用户问题
     * @param chatId 会话 ID
     * @param images 图片文件列表（可选，用于多模态对话）
     * @return AI 回复的文本内容（流式响应）
     */
    @Operation(summary = "AI 聊天", description = "支持文本和多模态对话，返回流式响应")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @Parameter(description = "用户问题") @RequestParam("prompt") String prompt,
            @Parameter(description = "会话 ID") @RequestParam(value = "chatId", required = false) String chatId,
            @Parameter(description = "图片文件列表") @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        
        log.info("收到聊天请求，prompt: {}, chatId: {}, images: {}", 
                prompt, chatId, images != null ? images.size() : 0);
        
        // 如果没有传入 chatId，生成一个新的
        if (chatId == null || chatId.isEmpty()) {
            chatId = "chat_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
            log.info("生成新会话 ID: {}", chatId);
        }
        
        // 保存会话记录到数据库
        try {
            Long userId = getCurrentUserId();
            if (userId != null) {
                chatHistoryRepository.save("chat", chatId, userId);
                log.debug("保存会话记录成功，userId: {}, chatId: {}", userId, chatId);
                
                // 保存用户消息
                AIChatMessage userMessage = AIChatMessage.builder()
                        .chatId(chatId)
                        .sessionId(chatId)
                        .role("user")
                        .content(prompt)
                        .messageType(images != null && !images.isEmpty() ? "multi-modal" : "text")
                        .createTime(LocalDateTime.now())
                        .build();
                log.info("准备保存用户消息，chatId: {}, content: {}", chatId, prompt);
                boolean saved = chatMessageService.saveMessage(userMessage);
                if (saved) {
                    log.info("保存用户消息成功，chatId: {}", chatId);
                } else {
                    log.warn("保存用户消息失败，chatId: {}", chatId);
                }
            } else {
                log.debug("用户未登录，跳过消息存储");
            }
        } catch (Exception e) {
            log.error("保存会话记录或用户消息失败: {}", e.getMessage());
        }
        
        // 根据是否有图片选择对应的聊天方法
        if (images != null && !images.isEmpty()) {
            log.info("使用多模态聊天模式");
            return multiModalChat(prompt, chatId, images);
        } else {
            log.info("使用文本聊天模式");
            return textChat(prompt, chatId);
        }
    }
}
