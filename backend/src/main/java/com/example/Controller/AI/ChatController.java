package com.example.Controller.AI;


import com.example.Repository.ChatHistoryRepository;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/ai")
@Tag(name = "AI聊天管理", description = "AI聊天相关接口，支持文本和多模态对话")
public class ChatController {

    private final ChatClient chatClient;

    private  final ChatHistoryRepository chatHistoryRepository;

    // 修改为返回JSON格式的同步API
    @Operation(summary = "同步AI聊天", description = "同步方式与AI对话，支持文本和多模态输入")
    @PostMapping("/chat-sync")
    public ResponseEntity<Map<String, Object>> chatSync(
            @Parameter(description = "用户输入的提示词") @RequestParam("prompt") String prompt,
            @Parameter(description = "会话ID") @RequestParam("chatId") String chatId,
            @Parameter(description = "上传的文件列表，可选") @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        try {
            // 1.保存会话id
            log.info("保存会话id：{}", chatId);
            chatHistoryRepository.save("chat", chatId);
            
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
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
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
        // 1.保存会话id
        chatHistoryRepository.save("chat", chatId);
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
