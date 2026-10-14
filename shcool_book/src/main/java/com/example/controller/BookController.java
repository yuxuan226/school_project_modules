package com.example.controller;

import cn.dev33.satoken.util.SaResult;
import com.example.entity.BookEntity;
import com.example.service.BookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@Api(tags = "书籍管理")
@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * 分页获取书籍列表
     */
    @ApiOperation(value = "分页获取书籍列表", notes = "分页查询所有书籍，支持按分类筛选和关键词搜索")
    @GetMapping("/list")
    public SaResult getBookList(
            @ApiParam(value = "页码，默认1") @RequestParam(defaultValue = "1", required = false) Integer pageNum,
            @ApiParam(value = "每页数量，默认10") @RequestParam(defaultValue = "10", required = false) Integer pageSize,
            @ApiParam(value = "分类（1:编程 2:文学 3:科技 4:历史 5:其他）") @RequestParam(value = "type", required = false) Integer type,
            @ApiParam(value = "搜索关键词，可搜索书名或作者") @RequestParam(value = "keyword", required = false) String keyword) {
        return bookService.getBookPage(pageNum, pageSize, type, keyword);
    }

    /**
     * 获取推荐书籍列表
     */
    @ApiOperation(value = "获取推荐书籍列表")
    @GetMapping("/recommend")
    public SaResult getRecommendBooks(
            @ApiParam(value = "数量，默认10") @RequestParam(defaultValue = "10", required = false) Integer limit) {
        return bookService.getRecommendBooks(limit);
    }

    /**
     * 获取轮播图列表
     */
    @ApiOperation(value = "获取轮播图列表")
    @GetMapping("/banner")
    public SaResult getBannerBooks(
            @ApiParam(value = "数量，默认5") @RequestParam(defaultValue = "5", required = false) Integer limit) {
        return bookService.getBannerBooks(limit);
    }

    /**
     * 获取热门书籍列表
     */
    @ApiOperation(value = "获取热门书籍列表")
    @GetMapping("/hot")
    public SaResult getHotBooks(
            @ApiParam(value = "数量，默认10") @RequestParam(defaultValue = "10", required = false) Integer limit) {
        return bookService.getHotBooks(limit);
    }

    /**
     * 根据id查询书籍详细信息（浏览数+1）
     */
    @ApiOperation(value = "根据ID查询书籍详情", notes = "查询书籍详情，同时将浏览数 view_count +1，imageUrl 已拼接完整访问路径")
    @GetMapping("/detail/{id}")
    @Cacheable(value = "bookCache", key = "#id")  //查询走缓存
    public SaResult getBookDetail(@ApiParam("书籍ID") @PathVariable Long id) {
        return bookService.getBookDetail(id);
    }

    /**
     * 保存书籍
     * submitTime 为空自动设为当前时间，submitUser 从当前登录用户获取
     */
    @ApiOperation(value = "保存书籍", notes = "新增书籍信息，submitTime为空则自动设为当前时间，submitUser自动从当前登录用户获取")
    @PostMapping("/save")
    public SaResult saveBook(@RequestBody BookEntity book) {
        return bookService.saveBook(book);
    }
}
