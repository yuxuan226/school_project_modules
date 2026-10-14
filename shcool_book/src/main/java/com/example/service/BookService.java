package com.example.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.BookEntity;

import java.util.List;

public interface BookService extends IService<BookEntity> {

    /** 分页获取书籍列表 */
    SaResult getBookPage(Integer pageNum, Integer pageSize, Integer type, String keyword);

    /** 获取推荐书籍列表 */
    SaResult getRecommendBooks(Integer limit);

    /** 获取轮播图列表 */
    SaResult getBannerBooks(Integer limit);

    /** 获取热门书籍列表 */
    SaResult getHotBooks(Integer limit);

    /** 根据id查询详细信息（浏览数+1） */
    SaResult getBookDetail(Long id);

    /** 保存书籍：submitTime 为空自动设为当前时间，submitUser 从当前登录用户获取 */
    SaResult saveBook(BookEntity book);

    /** 查询所有书籍的图片文件名（供 Quartz 清理未关联图片使用） */
    List<String> getImagesNames();
}
