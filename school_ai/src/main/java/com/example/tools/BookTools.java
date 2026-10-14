package com.example.tools;

import cn.dev33.satoken.util.SaResult;
import com.alibaba.fastjson.JSON;
import com.example.service.BookService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 图书工具类
 * 提供给 AI 大模型调用的工具方法，用于查询图书信息
 */
@Component
public class BookTools {

    @Autowired
    private BookService bookService;

    /**
     * 搜索图书
     * 根据关键词搜索图书（书名或作者模糊匹配）
     */
    @Tool(name = "searchBooks", value = {"根据关键词搜索图书，匹配书名或作者。返回图书列表的JSON字符串。"})
    public String searchBooks(@P("搜索关键词") String keywords) {
        SaResult result = bookService.getBookPage(1, 10, null, keywords);
        return JSON.toJSONString(result.getData());
    }

    /**
     * 获取图书详情
     * 根据图书ID获取详细信息
     */
    @Tool(name = "bookDetail", value = {"根据图书ID获取图书详细信息。返回图书详情的JSON字符串。"})
    public String bookDetail(@P("图书ID") Long id) {
        SaResult result = bookService.getBookDetail(id);
        return JSON.toJSONString(result.getData());
    }

    /**
     * 获取热门图书
     * 返回浏览次数最高的图书列表
     */
    @Tool(name = "hotBooks", value = {"获取热门图书列表。返回热门图书的JSON字符串。"})
    public String hotBooks(@P("返回数量限制") int limit) {
        SaResult result = bookService.getHotBooks(limit);
        return JSON.toJSONString(result.getData());
    }
}
