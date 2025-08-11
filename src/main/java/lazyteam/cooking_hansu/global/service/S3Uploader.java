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
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
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

    /**
     * URL에서 이미지를 다운로드하여 S3에 업로드
     * @param imageUrl 소셜 로그인에서 제공하는 프로필 이미지 URL
     * @param dirName S3 버킷 내 디렉토리명
     * @param fileName 저장할 파일명 (확장자 포함)
     * @return S3에 업로드된 이미지 URL
     */
    public String uploadFromUrl(String imageUrl, String dirName, String fileName) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            // URL에서 이미지 다운로드
            URL url = new URL(imageUrl);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            try (InputStream inputStream = connection.getInputStream()) {
                // 파일 확장자 추출 또는 기본값 설정
                String fileExtension = getFileExtension(imageUrl);
                if (fileExtension.isEmpty()) {
                    fileExtension = ".jpg"; // 기본 확장자
                }

                String key = dirName + UUID.randomUUID() + "-" + fileName + fileExtension;
                String contentType = getContentType(fileExtension);

                // S3에 업로드
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build();

                s3Client.putObject(request, RequestBody.fromInputStream(inputStream, inputStream.available()));

                return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(key)).toExternalForm();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("URL에서 S3 업로드 실패: " + imageUrl, e);
        }
    }

    /**
     * URL에서 파일 확장자 추출
     */
    private String getFileExtension(String url) {
        try {
            // URL에서 쿼리 파라미터 제거
            String cleanUrl = url.split("\\?")[0];
            int lastDot = cleanUrl.lastIndexOf('.');
            if (lastDot > 0) {
                return cleanUrl.substring(lastDot);
            }
        } catch (Exception e) {
            // 확장자 추출 실패 시 빈 문자열 반환
        }
        return "";
    }

    /**
     * 파일 확장자에 따른 Content-Type 반환
     */
    private String getContentType(String fileExtension) {
        return switch (fileExtension.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            default -> "image/jpeg";
        };
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
            return URLDecoder.decode(encodedKey, java.nio.charset.StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new IllegalArgumentException("URL 디코딩 실패", e);
        }
    }
}