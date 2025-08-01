package com.wwy.Common;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
public class AliyunOSSUtil {

    private static final Logger log = LoggerFactory.getLogger(AliyunOSSUtil.class);

    @Autowired
    private AliyunOSSConfig aliyunOSSConfig;

    @Autowired
    private OSS ossClient;

    /**
     * 上传文件到OSS
     * @param file 文件
     * @param dir 目录
     * @return 文件URL
     */
    public String uploadFile(MultipartFile file, String dir) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 获取文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        
        // 生成新的文件名
        String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + System.currentTimeMillis() + suffix;
        
        // 完整的文件路径
        String objectName = dir + "/" + fileName;

        try (InputStream inputStream = file.getInputStream()) {
            // 创建元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // 上传文件
            ossClient.putObject(aliyunOSSConfig.getBucketName(), objectName, inputStream, metadata);

            // 返回文件访问路径
            return "https://" + aliyunOSSConfig.getBucketName() + "." + aliyunOSSConfig.getEndpoint().substring(8) + "/" + objectName;
        } catch (IOException e) {
            log.error("上传文件到OSS失败: {}", e.getMessage());
            throw new RuntimeException("上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 删除OSS中的文件
     * @param fileUrl 文件URL
     */
    public void deleteFile(String fileUrl) {
        try {
            // 从URL中提取objectName
            String objectName = fileUrl.substring(fileUrl.indexOf(aliyunOSSConfig.getBucketName() + ".") + aliyunOSSConfig.getBucketName().length() + 1);
            objectName = objectName.substring(objectName.indexOf("/") + 1);
            
            // 删除文件
            ossClient.deleteObject(aliyunOSSConfig.getBucketName(), objectName);
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("删除OSS文件失败: {}", e.getMessage());
        }
    }
} 