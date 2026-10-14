package com.example.service;

import com.alibaba.fastjson.JSON;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.CosineSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Embedding 向量服务
 * 实现 RAG 检索增强生成：文档切分 → 向量化 → Redis 存储 → 相似度检索
 */
@Service
public class EmbeddingService {

    @Autowired
    @Qualifier("embeddingRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private EmbeddingModel embeddingModel;

    private static final String KEY_PREFIX = "embedding:";

    /**
     * 文档摄入：将文本切分为段落、生成向量、存入 Redis
     *
     * @param text  原始文本内容
     * @param docId 文档ID，用于隔离不同文档的向量
     */
    public void ingest(String text, String docId) {
        // 1. 按段落切分文档（每段最大 500 字符，重叠 50 字符）
        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(500, 50);
        Document document = Document.from(text);
        List<TextSegment> segments = splitter.split(document);

        if (segments.isEmpty()) {
            return;
        }

        // 2. 批量生成向量
        Response<List<Embedding>> response = embeddingModel.embedAll(segments);
        List<Embedding> embeddings = response.content();

        // 3. 存入 Redis Hash（key = embedding:{docId}:{index}）
        for (int i = 0; i < segments.size(); i++) {
            String key = KEY_PREFIX + docId + ":" + i;
            Embedding embedding = embeddings.get(i);

            // 向量转为 JSON 字符串存储
            redisTemplate.opsForHash().put(key, "vector", JSON.toJSONString(embedding.vectorAsList()));
            redisTemplate.opsForHash().put(key, "text", segments.get(i).text());
            redisTemplate.opsForHash().put(key, "docId", docId);
        }
    }

    /**
     * 语义检索：根据查询文本检索最相关的文本片段
     *
     * @param query      查询文本
     * @param docId      文档ID（限定检索范围）
     * @param maxResults 返回结果数量
     * @return 最相关的文本片段列表（按相似度降序）
     */
    public List<String> search(String query, String docId, int maxResults) {
        // 1. 生成查询向量
        Response<Embedding> queryResponse = embeddingModel.embed(TextSegment.from(query));
        Embedding queryEmbedding = queryResponse.content();

        // 2. 扫描 Redis 中该文档的所有向量
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + docId + ":*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 计算每条记录与查询的余弦相似度
        List<Map.Entry<String, Double>> scored = new ArrayList<>();
        for (String key : keys) {
            String vectorJson = (String) redisTemplate.opsForHash().get(key, "vector");
            String text = (String) redisTemplate.opsForHash().get(key, "text");
            if (vectorJson == null || text == null) {
                continue;
            }

            // 解析向量并计算相似度
            List<Float> vectorList = JSON.parseArray(vectorJson, Float.class);
            float[] vector = new float[vectorList.size()];
            for (int i = 0; i < vectorList.size(); i++) {
                vector[i] = vectorList.get(i);
            }
            double similarity = CosineSimilarity.between(queryEmbedding, Embedding.from(vector));
            scored.add(new AbstractMap.SimpleEntry<>(text, similarity));
        }

        // 4. 按相似度降序排序，取 Top-K
        scored.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        return scored.stream()
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
