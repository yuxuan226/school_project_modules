package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.BookEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BookMapper extends BaseMapper<BookEntity> {

    /**
     * 获取推荐书籍列表
     */
    @Select("SELECT * FROM book WHERE is_recommend = 1 ORDER BY sort_order DESC, submit_time DESC LIMIT #{limit}")
    List<BookEntity> selectRecommendBooks(Integer limit);

    /**
     * 获取轮播图列表
     */
    @Select("SELECT * FROM book WHERE is_banner = 1 ORDER BY sort_order DESC, submit_time DESC LIMIT #{limit}")
    List<BookEntity> selectBannerBooks(Integer limit);

    /**
     * 获取热门书籍列表（按浏览次数降序）
     */
    @Select("SELECT * FROM book ORDER BY view_count DESC LIMIT #{limit}")
    List<BookEntity> selectHotBooks(Integer limit);

    /**
     * 浏览数+1
     */
    @Update("UPDATE book SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);
}
