package com.example.quartz;

import com.example.service.BookService;
import com.example.utils.FileUrlUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 图片清理定时任务
 * 每天凌晨1点执行，清理磁盘上未被任何书籍引用的图片文件
 */
@Component
public class ImgQuartzJob implements Job {

    @Autowired
    private BookService bookService;

    @Value("${file.path}")
    private String filePath;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 1. 查询数据库中所有书籍引用的图片名
        List<String> dbImageNames = bookService.getImagesNames();
        // 2. 查询磁盘上的所有文件名
        List<String> diskFileNames = FileUrlUtils.listFileNames(filePath);
        // 3. 求差集：磁盘存在但数据库未引用的文件，予以删除
        for (String fileName : diskFileNames) {
            if (!dbImageNames.contains(fileName)) {
                FileUrlUtils.deleteFile(filePath + fileName);
            }
        }
    }
}
