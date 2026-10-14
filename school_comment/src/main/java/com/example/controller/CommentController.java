package com.example.controller;

import cn.dev33.satoken.util.SaResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Api(tags = "通用接口")
@RestController
@RequestMapping("/common")
public class CommentController {

    @Value("${file.path}")
    private String filePath;

    /**
     * 上传图片
     */
    @ApiOperation(value = "上传图片", notes = "上传书籍封面图片，返回图片ID和文件名")
    @PostMapping("/upload")
    public SaResult upload(
            @ApiParam(value = "上传的图片文件", required = true) @RequestParam("file") MultipartFile file) {
        Map<String, Object> map = new HashMap<>();
        // 设置保存路径
        String uploadDir = filePath;
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        // 为了防止文件名冲突，使用UUID生成一个唯一的文件名
        String newFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        // 创建保存文件的路径
        File destFile = new File(uploadDir + newFilename);
        // 检查目标目录是否存在，如果不存在则创建
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        try {
            // 将上传的文件保存到目标路径
            file.transferTo(destFile);
            map.put("imageName", newFilename);
            return SaResult.data(map);
        } catch (IOException e) {
            e.printStackTrace();
            return SaResult.error("上传失败：" + e.getMessage());
        }
    }
}
