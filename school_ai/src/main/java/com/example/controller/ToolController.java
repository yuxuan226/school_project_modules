package com.example.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.example.service.EmbeddingService;
import com.example.tools.BookTools;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 工具调用 + RAG 检索增强 控制器
 * 集成 BookTools 工具调用和 EmbeddingService 语义检索
 */
@Api(tags = "AI 工具与RAG")
@RestController
@RequestMapping("/book/ai")
public class ToolController {

    @Autowired
    private QwenChatModel qwenChatModel;

    @Autowired
    private BookTools bookTools;

    @Autowired
    private EmbeddingService embeddingService;

    /** 工具执行器映射：toolName -> ToolExecutor */
    private final Map<String, ToolExecutor> toolExecutors = new HashMap<>();

    /** 工具规格列表 */
    private List<ToolSpecification> toolSpecifications;

    /**
     * 初始化：扫描 BookTools 中 @Tool 注解的方法，创建规格和执行器
     */
    @PostConstruct
    public void init() {
        // 从 BookTools 对象提取所有工具规格
        toolSpecifications = ToolSpecifications.toolSpecificationsFrom(bookTools);

        // 为每个 @Tool 方法创建 DefaultToolExecutor
        for (java.lang.reflect.Method method : bookTools.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Tool.class)) {
                Tool toolAnnotation = method.getAnnotation(Tool.class);
                String toolName = toolAnnotation.name().isEmpty()
                        ? method.getName() : toolAnnotation.name();
                toolExecutors.put(toolName, new DefaultToolExecutor(bookTools, method));
            }
        }
    }

    /**
     * 工具调用对话（带 RAG 回退）
     * 先尝试调用图书工具，若 AI 未调用工具则走 RAG 语义检索补充上下文
     */
    @ApiOperation(value = "工具调用对话", notes = "先尝试工具调用，未调用工具时回退到 RAG 语义检索")
    @GetMapping("/tool-chat")
    public String toolChat(
            @ApiParam("用户消息") @RequestParam String message,
            @ApiParam("RAG文档ID") @RequestParam(required = false, defaultValue = "default") String docId) {

        StpUtil.checkLogin();

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(UserMessage.from(message));

        boolean ragUsed = false;
        int maxIterations = 5;

        for (int i = 0; i < maxIterations; i++) {
            // 构建请求（RAG 阶段不传工具规格，避免循环调用）
            ChatRequest.Builder builder = ChatRequest.builder().messages(messages);
            if (!ragUsed) {
                builder.toolSpecifications(toolSpecifications);
            }

            ChatResponse response = qwenChatModel.chat(builder.build());
            AiMessage aiMessage = response.aiMessage();

            // 1. AI 请求调用工具 → 执行工具并继续循环
            if (aiMessage.hasToolExecutionRequests()) {
                messages.add(aiMessage);
                for (ToolExecutionRequest toolReq : aiMessage.toolExecutionRequests()) {
                    ToolExecutor executor = toolExecutors.get(toolReq.name());
                    if (executor != null) {
                        String result = executor.execute(toolReq, null);
                        messages.add(ToolExecutionResultMessage.from(toolReq, result));
                    }
                }
                continue;
            }

            // 2. AI 未调用工具 → 尝试 RAG 检索补充上下文
            if (!ragUsed) {
                List<String> ragResults = embeddingService.search(message, docId, 3);
                if (!ragResults.isEmpty()) {
                    String context = String.join("\n\n", ragResults);
                    // 重置消息，注入 RAG 上下文后重新提问
                    messages = new ArrayList<>();
                    messages.add(UserMessage.from(
                            "请根据以下参考资料回答用户的问题。\n\n参考资料：\n" + context +
                                    "\n\n用户问题：" + message
                    ));
                    ragUsed = true;
                    continue;
                }
            }

            // 3. 返回最终回复
            return aiMessage.text();
        }

        return "处理超时，请重试";
    }

    /**
     * RAG 文档上传：将上传的文本文件切分、向量化并存入 Redis
     */
    @ApiOperation(value = "RAG文档上传", notes = "上传文本文件，自动切分并生成向量存储到Redis")
    @PostMapping("/rag/upload")
    public SaResult uploadRagDocument(
            @ApiParam("文本文件") @RequestParam("file") MultipartFile file,
            @ApiParam("文档ID") @RequestParam(required = false, defaultValue = "default") String docId) {

        StpUtil.checkLogin();

        if (file.isEmpty()) {
            return SaResult.error("文件不能为空");
        }

        try {
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);
            embeddingService.ingest(text, docId);
            return SaResult.ok("文档摄入成功，docId=" + docId);
        } catch (IOException e) {
            return SaResult.error("文件读取失败：" + e.getMessage());
        }
    }

    /**
     * RAG 语义检索：根据查询文本检索最相关的文本片段
     */
    @ApiOperation(value = "RAG语义检索", notes = "根据查询文本检索相关文档片段")
    @GetMapping("/rag/search")
    public SaResult ragSearch(
            @ApiParam("查询文本") @RequestParam String query,
            @ApiParam("文档ID") @RequestParam(required = false, defaultValue = "default") String docId,
            @ApiParam("返回数量") @RequestParam(required = false, defaultValue = "3") int topK) {

        StpUtil.checkLogin();

        List<String> results = embeddingService.search(query, docId, topK);
        return SaResult.data(results);
    }
}
