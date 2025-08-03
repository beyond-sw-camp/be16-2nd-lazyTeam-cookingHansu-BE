package lazyteam.cooking_hansu.global.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, String dirName) {
        String key = dirName + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(key)).toExternalForm();

        } catch (IOException e) {
            throw new IllegalArgumentException("S3 업로드 실패", e);
        }
    }

    // 삭제 메서드
    public void delete(String fileUrl) {
        try {
            // URL에서 key 추출 (예: https://bucket-name.s3.amazonaws.com/products/UUID-파일명.jpg)
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);

        } catch (Exception e) {
            throw new IllegalArgumentException("S3 삭제 실패", e);
        }
    }

    // URL에서 key 추출하는 유틸 함수
    private String extractKeyFromUrl(String fileUrl) {
        try {
            int index = fileUrl.indexOf(".amazonaws.com/");
            if (index == -1) {
                throw new IllegalArgumentException("잘못된 S3 URL 형식입니다.");
            }
            
            String encodedKey = fileUrl.substring(index + ".amazonaws.com/".length());
            
            // URL 디코딩을 수행하여 한글 파일명을 원래대로 복원
            return URLDecoder.decode(encodedKey, "UTF-8");
            
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("URL 디코딩 실패", e);
        }
    }
}