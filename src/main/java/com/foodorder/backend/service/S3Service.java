// SERVICE UPLOAD FILE LÊN AWS S3

package com.foodorder.backend.service;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;


    // HÀM UPLOAD ẢNH
    public String uploadFile(MultipartFile file) throws IOException {
        // Tạo tên file ngẫu nhiên để tránh trùng
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
        // Metadata để set content-type
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        // Upload lên S3
        amazonS3.putObject(bucketName, fileName, file.getInputStream(), metadata);

        // Trả về public URL (giả sử bucket đang public)
        return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    // HÀM XOÁ ẢNH
    public void deleteFile(String fileUrl) {
        try {
            // Tách tên file từ URL
            URI uri = new URI(fileUrl);
            String key = uri.getPath().substring(1); // bỏ dấu "/" đầu tiên
            amazonS3.deleteObject(bucketName, key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image from S3", e);
        }
    }


}
