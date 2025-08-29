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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 파일을 S3에 업로드 (기존 메서드 - 수정하지 않음)
     */
    public String upload(MultipartFile file, String dirName) {
        // 파일 유효성 검증 및 난수화 파일명 생성
        validateFile(file);
        String randomFileName = generateRandomFileName(file.getOriginalFilename());
        String key = dirName + randomFileName;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(getContentTypeFromFile(file))
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(key)).toExternalForm();

        } catch (IOException e) {
            throw new IllegalArgumentException("S3 업로드 실패", e);
        }
    }

    public String uploadForChat(MultipartFile file, String dirName) {
        // 파일 유효성 검증 및 난수화 파일명 생성
        String randomFileName = generateRandomFileName(file.getOriginalFilename());
        String key = dirName + randomFileName;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(getContentTypeFromFile(file))
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(key)).toExternalForm();

        } catch (IOException e) {
            throw new IllegalArgumentException("S3 업로드 실패", e);
        }
    }

    /**
     * S3에서 파일 삭제 (기존 메서드 - 수정하지 않음)
     */
    public void delete(String fileUrl) {
        try {
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

    /**
     * URL에서 key 추출 (기존 메서드 - 수정하지 않음)
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
     *
     * @param imageUrl 소셜 로그인에서 제공하는 프로필 이미지 URL
     * @param dirName  S3 버킷 내 디렉토리명
     * @param fileName 저장할 파일명 (확장자 없이)
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
                String fileExtension = extractFileExtensionFromUrl(imageUrl);
                if (fileExtension.isEmpty()) {
                    fileExtension = ".jpg"; // 기본 확장자
                }

                // 난수화된 파일명 생성 (프로필 이미지용)
                String randomFileName = generateProfileFileName(fileName, fileExtension);
                String key = dirName + randomFileName;
                String contentType = getContentTypeFromExtension(fileExtension);

                // 파일 크기 검증 (프로필 이미지는 100MB 제한)
                long maxProfileImageSize = 100 * 1024 * 1024; // 100MB
                int contentLength = connection.getContentLength();
                if (contentLength > maxProfileImageSize) {
                    throw new IllegalArgumentException("프로필 이미지 크기는 10MB를 초과할 수 없습니다.");
                }

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
     * 파일 유효성 검증 (개선됨)
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 파일 크기 검증 (100MB 제한)
        long maxFileSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기는 100MB를 초과할 수 없습니다.");
        }

        // 파일 확장자 검증
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        String extension = getFileExtension(fileName);
        if (!isAllowedExtension(extension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif, pdf만 허용)");
        }
    }

    /**
     * 난수화된 파일명 생성 (개선됨)
     */
    private String generateRandomFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        // 파일 확장자 추출
        String extension = getFileExtension(originalFileName);

        // 현재 시간 + UUID + 확장자로 난수화된 파일명 생성
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
        try {
            // URL에서 쿼리 파라미터 제거
            String cleanUrl = url.split("\\?")[0];
            int lastDot = cleanUrl.lastIndexOf('.');
            if (lastDot > 0) {
                return cleanUrl.substring(lastDot).toLowerCase();
            }
        } catch (Exception e) {
            // 확장자 추출 실패 시 빈 문자열 반환
        }
        return "";
    }

    /**
     * 허용된 확장자인지 확인
     */
    private boolean isAllowedExtension(String extension) {
        return extension.equals(".jpg") || extension.equals(".jpeg") ||
                extension.equals(".png") || extension.equals(".gif") ||
                extension.equals(".webp") || extension.equals(".pdf") ||
                extension.equals(".mp4") || extension.equals(".mov") || //영상 확장자 허용(강의 업로드 시 필요)
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

        // Content-Type이 없는 경우 확장자로 추정
        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            String extension = getFileExtension(fileName);
            return switch (extension) {
                case ".jpg", ".jpeg" -> "image/jpeg";
                case ".png" -> "image/png";
                case ".gif" -> "image/gif";
                case ".webp" -> "image/webp";
                case ".pdf" -> "application/pdf";
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