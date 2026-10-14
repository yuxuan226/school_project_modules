package com.example.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件路径工具类
 */
@Component
public class FileUrlUtils {

    private static String urlPrefix;

    @Value("${file.url-prefix:/images/}")
    public void setUrlPrefix(String urlPrefix) {
        FileUrlUtils.urlPrefix = urlPrefix;
    }

    /**
     * 获取图片完整访问路径
     * @param imageName 图片文件名
     * @return 完整访问路径，如 /images/xxx.png
     */
    public static String getImageUrl(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            return null;
        }
        // 如果已经是完整路径，直接返回
        if (imageName.startsWith("http://") || imageName.startsWith("https://") || imageName.startsWith("/images/")) {
            return imageName;
        }
        return urlPrefix + imageName;
    }

    /**
     * 列出指定目录下所有文件名
     * @param dirPath 目录路径
     * @return 文件名列表，目录不存在或为空返回空列表
     */
    public static List<String> listFileNames(String dirPath) {
        List<String> names = new ArrayList<>();
        if (dirPath == null || dirPath.isEmpty()) {
            return names;
        }
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return names;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return names;
        }
        for (File file : files) {
            if (file.isFile()) {
                names.add(file.getName());
            }
        }
        return names;
    }

    /**
     * 删除指定路径的文件
     * @param filePath 文件完整路径
     * @return true 删除成功，false 文件不存在或删除失败
     */
    public static boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        File file = new File(filePath);
        return file.exists() && file.delete();
    }
}
