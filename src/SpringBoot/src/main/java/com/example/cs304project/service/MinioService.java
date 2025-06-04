package com.example.cs304project.service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    private MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.urlExpiry}")
    private Integer urlExpiry; // 秒

    /**
     * 初始化 MinIO 客户端，并确保 Bucket 存在
     */
    @PostConstruct
    public void init() throws Exception {
        // 1. 构建 MinioClient
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        // 2. 检查 Bucket 是否存在，不存在则创建
        boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );
        if (!found) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
            );
        }
    }

    /**
     * 上传一个 MultipartFile（如从前端传过来的 PDF 或图片），并返回上传后的 objectName
     *
     * @param file  前端上传的文件
     * @param objectName  存储在 MinIO 中的名称（可包含路径“documents/xxx.pdf”）
     * @return   objectName
     */
    public String uploadFile(MultipartFile file, String objectName) throws Exception {
        // 1. 获取文件流
        try (InputStream inputStream = file.getInputStream()) {
            long size = file.getSize();
            String contentType = file.getContentType(); // application/pdf 等

            // 2. 调用 MinIO 的 putObject，将文件存储到指定 Bucket
            PutObjectArgs putArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(putArgs);

            return objectName;
        }
    }

    /**
     * 生成一个对外带签名的 URL，用于前端预览或下载文件
     * @param objectName  MinIO 中保存的 object 名称
     * @return  带签名的 HTTP URL
     */
    public String generatePresignedUrl(String objectName) throws Exception {
        GetPresignedObjectUrlArgs urlArgs = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(urlExpiry, TimeUnit.SECONDS) // 1 小时有效
                .build();

        return minioClient.getPresignedObjectUrl(urlArgs);
    }
}
