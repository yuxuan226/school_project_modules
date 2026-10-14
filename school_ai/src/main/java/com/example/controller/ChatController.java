package com.example.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.example.enums.RoleType;
import com.example.store.RedisChatMemoryStore;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 聊天控制器
 * 集成阿里通义千问大模型，支持同步、流式输出与聊天记忆
 */
@Api(tags = "AI 聊天")
@RestController
@RequestMapping("/book/ai")
public class ChatController {

    @Autowired
    private QwenChatModel qwenChatModel;

    @Autowired
    private QwenStreamingChatModel streamingChatModel;

    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    /**
     * 最大消息条数（必须是偶数）
     * 一轮对话 = 2 条消息（用户消息 + AI 响应），40 条 = 20 轮对话
     */
    private static final int MAX_MESSAGES = 40;

    /**
     * 同步聊天：等待 AI 全部返回后再响应
     */
    @ApiOperation(value = "同步聊天", notes = "发送消息给通义千问，等待完整响应后返回")
    @GetMapping("/chat")
    public SaResult chat(@ApiParam("消息内容") @RequestParam(defaultValue = "你是谁") String message) {
        StpUtil.checkLogin();
        String response = qwenChatModel.chat(message);
        return SaResult.data(response);
    }

    /**
     * 流式聊天：逐字返回 AI 响应内容
     * text/event-stream;charset=UTF-8 浏览器使用
     * text/plain;charset=UTF-8 swagger测试使用
     */
    @ApiOperation(value = "流式聊天", notes = "流式输出 AI 响应内容，逐字返回")
    @GetMapping(value = "/stream", produces = {"text/event-stream;charset=UTF-8", "text/plain;charset=UTF-8"})
    public Flux<String> stream(@ApiParam("消息内容") @RequestParam(defaultValue = "你是谁") String message) {
        // 1.必须加！主线程校验登录，最安全
        StpUtil.checkLogin();
        // 2. 再返回流式 Flux
        return Flux.create(fluxSink -> {
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(UserMessage.from(message))
                    .build();

            streamingChatModel.chat(chatRequest, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    fluxSink.next(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse chatResponse) {
                    fluxSink.complete();
                }

                @Override
                public void onError(Throwable throwable) {
                    fluxSink.error(throwable);
                }
            });
        });
    }

    /**
     * 带聊天记忆的对话接口
     * 使用 Redis 存储聊天历史，支持多轮对话，自动保留最近 20 轮对话
     */
    @ApiOperation(value = "带聊天记忆的对话",
            notes = "使用 Redis 存储聊天历史，支持多轮对话。自动保留最近 20 轮对话")
    @GetMapping("/chat-memory")
    public String chatWithMemory(
            @ApiParam("用户消息") @RequestParam(defaultValue = "你是谁，你能做什么？") String message) {

        // 1. 获取当前登录用户 ID 作为会话标识
        String loginId = StpUtil.getLoginId().toString();

        // 2. 获取历史消息（创建可变副本）
        List<ChatMessage> messages = new ArrayList<>(
                redisChatMemoryStore.getMessages(loginId)
        );

        // 3. 添加当前用户消息
        messages.add(UserMessage.from(message));

        // 4. 构建请求并调用 AI 模型
        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .build();
        ChatResponse response = qwenChatModel.chat(request);
        String responseText = response.aiMessage().text();

        // 5. 添加 AI 响应到历史消息
        messages.add(AiMessage.from(responseText));

        // 6. 限制消息数量（只保留最近的 MAX_MESSAGES 条）
        if (messages.size() > MAX_MESSAGES) {
            messages = messages.subList(messages.size() - MAX_MESSAGES, messages.size());
        }

        // 7. 保存更新后的历史到 Redis
        redisChatMemoryStore.updateMessages(loginId, messages);

        return responseText;
    }

    /**
     * 清空当前用户的聊天记忆
     */
    @ApiOperation(value = "清空聊天记忆", notes = "删除当前登录用户的全部聊天历史")
    @GetMapping("/chat-memory/clear")
    public String clearChatMemory() {
        String loginId = StpUtil.getLoginId().toString();
        redisChatMemoryStore.deleteMessages(loginId);
        return "聊天记忆已清空";
    }

    /**
     * 带模型参数的对话接口
     * temperature 控制随机性，topP 控制词汇多样性，maxTokens 控制最大输出长度
     */
    @ApiOperation(value = "带参数的对话",
            notes = "可自定义 temperature/topP/maxTokens 参数，同时保留聊天记忆")
    @GetMapping("/chat-with-params")
    public String chatWithParams(
            @ApiParam("用户消息") @RequestParam String message,
            @ApiParam("温度系数 0.0~1.0，越小越确定") @RequestParam(required = false, defaultValue = "0.7") Double temperature,
            @ApiParam("核采样参数 0.0~1.0") @RequestParam(required = false, defaultValue = "0.8") Double topP,
            @ApiParam("最大输出长度") @RequestParam(required = false, defaultValue = "2000") Integer maxTokens) {

        String loginId = StpUtil.getLoginId().toString();

        // 获取历史消息
        List<ChatMessage> messages = new ArrayList<>(
                redisChatMemoryStore.getMessages(loginId)
        );
        messages.add(UserMessage.from(message));

        // 构建参数对象
        ChatRequestParameters parameters = ChatRequestParameters.builder()
                .temperature(temperature)      // 温度系数
                .topP(topP)                    // 核采样参数
                .maxOutputTokens(maxTokens)    // 最大输出长度（1.0.0-alpha1 中方法名为 maxOutputTokens）
                .build();

        // 构建带参数的请求
        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .parameters(parameters)
                .build();

        ChatResponse response = qwenChatModel.chat(request);
        String responseText = response.aiMessage().text();

        // 保存历史
        messages.add(AiMessage.from(responseText));
        if (messages.size() > MAX_MESSAGES) {
            messages = messages.subList(messages.size() - MAX_MESSAGES, messages.size());
        }
        redisChatMemoryStore.updateMessages(loginId, messages);

        return responseText;
    }
}
