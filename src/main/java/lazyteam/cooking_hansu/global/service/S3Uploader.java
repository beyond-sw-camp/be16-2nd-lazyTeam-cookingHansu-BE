package lazyteam.cooking_hansu.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 파일을 S3에 업로드 (수정됨 - 파일명 중복 문제 해결)
     */
    public String upload(MultipartFile file, String dirName) {
        // 파일 유효성 검증
        validateFile(file);

        // ✅ 파일명 중복 문제 해결: dirName이 이미 완전한 파일명인 경우 난수화하지 않음
        String key;
        if (dirName.contains(".")) {
            // 이미 확장자가 포함된 완전한 파일명인 경우
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String randomId = UUID.randomUUID().toString().substring(0, 8);
            String extension = getFileExtension(dirName);
            String baseName = dirName.substring(0, dirName.lastIndexOf("."));
            key = baseName + "_" + timestamp + "_" + randomId + extension;
        } else {
            // 기존 방식 (디렉토리명 + 난수화된 파일명)
            String randomFileName = generateRandomFileName(file.getOriginalFilename());
            key = dirName + randomFileName;
        }

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(getContentTypeFromFile(file))
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            String s3Url = s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(key)).toExternalForm();
            log.info("S3 업로드 성공 - Key: {}, URL: {}", key, s3Url);

            return s3Url;

        } catch (IOException e) {
            throw new IllegalArgumentException("S3 업로드 실패", e);
        }
    }

    /**
     * 채팅용 파일 업로드 (수정됨 - 파일명 중복 문제 해결)
     */
    public String uploadForChat(MultipartFile file, String dirName) {
        // 파일 유효성 검증
        validateFile(file);

        // ✅ 파일명 중복 문제 해결: dirName이 이미 완전한 파일명인 경우 난수화하지 않음
        String key;
        if (dirName.contains(".")) {
            // 이미 확장자가 포함된 완전한 파일명인 경우
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String randomId = UUID.randomUUID().toString().substring(0, 8);
            String extension = getFileExtension(dirName);
            String baseName = dirName.substring(0, dirName.lastIndexOf("."));
            key = baseName + "_" + timestamp + "_" + randomId + extension;
        } else {
            // 기존 방식 (디렉토리명 + 난수화된 파일명)
            String randomFileName = generateRandomFileName(file.getOriginalFilename());
            key = dirName + randomFileName;
        }

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(getContentTypeFromFile(file))
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            String s3Url = s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(key)).toExternalForm();
            log.info("S3 채팅 업로드 성공 - Key: {}, URL: {}", key, s3Url);

            return s3Url;

        } catch (IOException e) {
            throw new IllegalArgumentException("S3 채팅 업로드 실패", e);
        }
    }

    /**
     * S3에서 파일 삭제
     */
    public void delete(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("S3 파일 삭제 성공 - Key: {}", key);

        } catch (Exception e) {
            throw new IllegalArgumentException("S3 삭제 실패", e);
        }
    }

    /**
     * URL에서 key 추출
     */
    private String extractKeyFromUrl(String fileUrl) {
        try {
            int index = fileUrl.indexOf(".amazonaws.com/");
            if (index == -1) {
                throw new IllegalArgumentException("잘못된 S3 URL 형식입니다.");
            }

            String encodedKey = fileUrl.substring(index + ".amazonaws.com/".length());
            return URLDecoder.decode(encodedKey, java.nio.charset.StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new IllegalArgumentException("URL 디코딩 실패", e);
        }
    }

    /**
     * URL에서 이미지를 다운로드하여 S3에 업로드
     */
    public String uploadFromUrl(String imageUrl, String dirName, String fileName) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            String decodedUrl = URLDecoder.decode(imageUrl, "UTF-8");
            URL url = new URL(decodedUrl);

            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            connection.setRequestProperty("Accept", "image/*");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(60000);

            try (InputStream inputStream = connection.getInputStream()) {
                byte[] imageBytes = inputStream.readAllBytes();

                long maxProfileImageSize = 100 * 1024 * 1024;
                if (imageBytes.length > maxProfileImageSize) {
                    throw new IllegalArgumentException("프로필 이미지 크기는 100MB를 초과할 수 없습니다. 현재 크기: " + (imageBytes.length / 1024 / 1024) + "MB");
                }

                String fileExtension = extractFileExtensionFromUrl(imageUrl);
                if (fileExtension.isEmpty()) {
                    fileExtension = ".jpg";
                }

                String randomFileName = generateProfileFileName(fileName, fileExtension);
                String key = dirName + randomFileName;
                String contentType = getContentTypeFromExtension(fileExtension);

                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build();

                s3Client.putObject(request, RequestBody.fromBytes(imageBytes));

                String s3Url = s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(key)).toExternalForm();
                log.info("S3 URL 업로드 성공 - Key: {}, URL: {}", key, s3Url);

                return s3Url;
            }
        } catch (Exception e) {
            log.error("URL에서 S3 업로드 실패: {}", imageUrl, e);
            throw new IllegalArgumentException("프로필 이미지 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        long maxFileSize = 100 * 1024 * 1024;
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기는 100MB를 초과할 수 없습니다.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        String extension = getFileExtension(fileName);
        if (!isAllowedExtension(extension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif, pdf, mp4, mov, avi만 허용)");
        }
    }

    /**
     * 난수화된 파일명 생성
     */
    private String generateRandomFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        String extension = getFileExtension(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String randomId = UUID.randomUUID().toString().substring(0, 8);

        return timestamp + "_" + randomId + extension;
    }

    /**
     * 프로필 이미지용 난수화된 파일명 생성
     */
    private String generateProfileFileName(String baseName, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String randomId = UUID.randomUUID().toString().substring(0, 8);

        return "profile_" + baseName + "_" + timestamp + "_" + randomId + extension;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("파일 확장자가 없습니다.");
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    /**
     * URL에서 파일 확장자 추출
     */
    private String extractFileExtensionFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        try {
            String cleanUrl = url.split("\\?")[0];
            cleanUrl = cleanUrl.split("#")[0];

            int lastSlash = cleanUrl.lastIndexOf('/');
            if (lastSlash >= 0) {
                cleanUrl = cleanUrl.substring(lastSlash + 1);
            }

            int lastDot = cleanUrl.lastIndexOf('.');
            if (lastDot > 0 && lastDot < cleanUrl.length() - 1) {
                String extension = cleanUrl.substring(lastDot).toLowerCase();

                if (isAllowedImageExtension(extension)) {
                    return extension;
                }
            }

            log.debug("URL에서 확장자를 추출할 수 없음: {}", url);
            return "";

        } catch (Exception e) {
            log.warn("URL에서 확장자 추출 실패: {}, error: {}", url, e.getMessage());
            return "";
        }
    }

    /**
     * 허용된 이미지 확장자인지 확인
     */
    private boolean isAllowedImageExtension(String extension) {
        return extension.equals(".jpg") || extension.equals(".jpeg") ||
                extension.equals(".png") || extension.equals(".gif") ||
                extension.equals(".webp");
    }

    /**
     * 허용된 확장자인지 확인
     */
    private boolean isAllowedExtension(String extension) {
        return extension.equals(".jpg") || extension.equals(".jpeg") ||
                extension.equals(".png") || extension.equals(".gif") ||
                extension.equals(".webp") || extension.equals(".pdf") ||
                extension.equals(".mp4") || extension.equals(".mov") ||
                extension.equals(".avi");
    }

    /**
     * MultipartFile에서 Content-Type 추출
     */
    private String getContentTypeFromFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isEmpty()) {
            return contentType;
        }

        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            String extension = getFileExtension(fileName);
            return switch (extension) {
                case ".jpg", ".jpeg" -> "image/jpeg";
                case ".png" -> "image/png";
                case ".gif" -> "image/gif";
                case ".webp" -> "image/webp";
                case ".pdf" -> "application/pdf";
                case ".mp4" -> "video/mp4";
                case ".mov" -> "video/quicktime";
                case ".avi" -> "video/x-msvideo";
                default -> "application/octet-stream";
            };
        }

        return "application/octet-stream";
    }

    /**
     * 파일 확장자에 따른 Content-Type 반환
     */
    private String getContentTypeFromExtension(String fileExtension) {
        return switch (fileExtension.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }
}