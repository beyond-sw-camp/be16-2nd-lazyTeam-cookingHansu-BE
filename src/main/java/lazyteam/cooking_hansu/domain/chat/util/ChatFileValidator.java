package lazyteam.cooking_hansu.domain.chat.util;

import lazyteam.cooking_hansu.domain.chat.dto.ChatMessageReqDto;
import lazyteam.cooking_hansu.domain.chat.entity.FileType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ChatFileValidator {

    // 메시지와 파일 유효성 검사
    public static void validateMessageAndFile(ChatMessageReqDto chatMessageReqDto) {
        boolean hasMessage = chatMessageReqDto.getMessage() != null && !chatMessageReqDto.getMessage().trim().isEmpty();
        boolean hasFile = chatMessageReqDto.getFiles() != null && !chatMessageReqDto.getFiles().isEmpty();

        // 메시지만 있는 경우
        if (hasMessage && !hasFile) {
            // 메시지는 비어있으면 안됨 (이미 위에서 체크했지만 한번 더)
            if (chatMessageReqDto.getMessage().trim().isEmpty()) {
                throw new IllegalArgumentException("메시지 내용은 비어있을 수 없습니다.");
            }
        }
        // 파일만 있는 경우
        else if (!hasMessage && hasFile) {
            // 파일은 1개 이상 있어야 함
            if (chatMessageReqDto.getFiles().size() < 1) {
                throw new IllegalArgumentException("파일은 최소 1개 이상 있어야 합니다.");
            }
        }
        // 둘 다 있는 경우 (허용하지 않음)
        else if (hasMessage && hasFile) {
            throw new IllegalArgumentException("메시지와 파일을 동시에 전송할 수 없습니다. 메시지 또는 파일 중 하나만 전송해주세요.");
        }
        // 둘 다 없는 경우
        else {
            throw new IllegalArgumentException("메시지 또는 파일 중 하나는 반드시 있어야 합니다.");
        }
    }

    // 파일 타입 검증
    public static void validateFileType(MultipartFile file, FileType fileType) {
        // 빈 파일 검사
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        switch (fileType) {
            case IMAGE:
                if (!extension.matches("(jpg|jpeg|png|gif|webp)")) {
                    throw new IllegalArgumentException("이미지 파일은 jpg, jpeg, png, gif, webp 형식만 허용됩니다.");
                }
                break;
            case VIDEO:
                if (!extension.matches("(mp4|avi|mov)")) {
                    throw new IllegalArgumentException("비디오 파일은 mp4, avi, mov 형식만 허용됩니다.");
                }
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 파일 타입입니다.");
        }
    }

    // 파일 개수 검증
    public static void validateFileCount(List<MultipartFile> files, List<FileType> fileTypes) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        if (files.size() < 1) {
            throw new IllegalArgumentException("파일은 최소 1개 이상 있어야 합니다.");
        }
        if (files.size() > 10) {
            throw new IllegalArgumentException("파일은 최대 10개까지 업로드할 수 있습니다. 현재 " + files.size() + "개의 파일이 선택되었습니다.");
        }
        if (fileTypes != null && files.size() != fileTypes.size()) {
            throw new IllegalArgumentException("파일과 파일 타입의 개수가 일치하지 않습니다. 파일: " + files.size() + "개, 파일타입: " + fileTypes.size() + "개");
        }
    }

    // 파일 크기 검증
    public static void validateFileSize(MultipartFile file) {
        final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 100MB를 초과할 수 없습니다.");
        }
    }

    // 전체 요청 크기 검증
    public static void validateTotalRequestSize(List<MultipartFile> files) {
        final long MAX_REQUEST_SIZE = 100 * 1024 * 1024;

        long totalSize = files.stream()
                .mapToLong(MultipartFile::getSize)
                .sum();

        if (totalSize > MAX_REQUEST_SIZE) {
            throw new IllegalArgumentException("전체 파일 크기는 100MB를 초과할 수 없습니다.");
        }
    }

    // 파일 확장자 검증
    public static void validateFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("파일 확장자가 없습니다.");
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        String allowedExtensions = "jpg,jpeg,png,gif,webp,mp4,avi,mov";

        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다: " + extension);
        }
    }

    // 파일명 검증
    public static void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        // 파일명 길이 제한
        if (fileName.length() > 255) {
            throw new IllegalArgumentException("파일명이 너무 깁니다. (최대 255자)");
        }

        // 특수문자 제한
        if (fileName.matches(".*[<>:\"/\\\\|?*].*")) {
            throw new IllegalArgumentException("파일명에 허용되지 않는 특수문자가 포함되어 있습니다.");
        }
    }

    // 종합 파일 검증 (편의 메서드)
    public static void validateFile(MultipartFile file, FileType fileType) {
        validateFileName(file.getOriginalFilename());
        validateFileExtension(file.getOriginalFilename());
        validateFileSize(file);
        validateFileType(file, fileType);
    }

    // 업로드 요청 전체 검증 (편의 메서드)
    public static void validateUploadRequest(List<MultipartFile> files, List<FileType> fileTypes) {
        validateFileCount(files, fileTypes);
        validateTotalRequestSize(files);

        // 각 파일 개별 검증
        for (int i = 0; i < files.size(); i++) {
            validateFile(files.get(i), fileTypes.get(i));
        }
    }
}
