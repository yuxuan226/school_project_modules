package com.example.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.controller.UserEntityControllerParent;
import com.example.entity.BookEntity;
import com.example.mapper.BookMapper;
import com.example.service.BookService;
import com.example.utils.FileUrlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, BookEntity> implements BookService {

    @Autowired
    private UserEntityControllerParent userEntityControllerParent;

    @Override
    public SaResult getBookPage(Integer pageNum, Integer pageSize, Integer type, String keyword) {
        Page<BookEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BookEntity> wrapper = new LambdaQueryWrapper<>();

        if (type != null) {
            wrapper.eq(BookEntity::getType, type);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(BookEntity::getTitle, keyword)
                    .or()
                    .like(BookEntity::getAuthor, keyword));
        }
        wrapper.orderByDesc(BookEntity::getSubmitTime);

        Page<BookEntity> result = baseMapper.selectPage(page, wrapper);

        // 填充 imageUrl
        for (BookEntity book : result.getRecords()) {
            book.setImageUrl(FileUrlUtils.getImageUrl(book.getImage()));
        }

        return SaResult.data(result);
    }

    @Override
    public SaResult getRecommendBooks(Integer limit) {
        List<BookEntity> books = baseMapper.selectRecommendBooks(limit);
        for (BookEntity book : books) {
            book.setImageUrl(FileUrlUtils.getImageUrl(book.getImage()));
        }
        return SaResult.data(books);
    }

    @Override
    public SaResult getBannerBooks(Integer limit) {
        List<BookEntity> books = baseMapper.selectBannerBooks(limit);
        for (BookEntity book : books) {
            book.setImageUrl(FileUrlUtils.getImageUrl(book.getImage()));
        }
        return SaResult.data(books);
    }

    @Override
    public SaResult getHotBooks(Integer limit) {
        List<BookEntity> books = baseMapper.selectHotBooks(limit);
        for (BookEntity book : books) {
            book.setImageUrl(FileUrlUtils.getImageUrl(book.getImage()));
        }
        return SaResult.data(books);
    }

    @Override
    public SaResult getBookDetail(Long id) {
        BookEntity book = this.getById(id);
        if (book == null) {
            return SaResult.error("书籍不存在");
        }
        // 浏览数+1
        baseMapper.incrementViewCount(id);
        // 设置返回的 imageUrl
        book.setImageUrl(FileUrlUtils.getImageUrl(book.getImage()));
        // 重新查询获取最新 viewCount
        book = this.getById(id);
        book.setImageUrl(FileUrlUtils.getImageUrl(book.getImage()));
        return SaResult.data(book);
    }

    @Override
    public SaResult saveBook(BookEntity book) {
        // submitTime 为空则设为当前时间
        if (book.getSubmitTime() == null) {
            book.setSubmitTime(LocalDateTime.now());
        }
        // submitUser 从当前登录用户获取（通过 phone 查 id）
        // 登录时存储的是 phone，通过接口反查 user id
        String phone = StpUtil.getLoginIdAsString();
        Long userId = userEntityControllerParent.selectByPhone(phone);
        book.setSubmitUser(userId);
        boolean ok = this.save(book);
        return ok ? SaResult.ok("保存成功") : SaResult.error("保存失败");
    }

    @Override
    public List<String> getImagesNames() {
        List<BookEntity> books = baseMapper.selectList(
                new LambdaQueryWrapper<BookEntity>().select(BookEntity::getImage));
        List<String> names = new ArrayList<>();
        for (BookEntity book : books) {
            if (book.getImage() != null && !book.getImage().isEmpty()) {
                names.add(book.getImage());
            }
        }
        return names;
    }
}
