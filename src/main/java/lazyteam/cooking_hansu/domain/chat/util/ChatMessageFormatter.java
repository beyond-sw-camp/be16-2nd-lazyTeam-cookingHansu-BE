package lazyteam.cooking_hansu.domain.chat.util;

import lazyteam.cooking_hansu.domain.chat.entity.ChatFile;
import lazyteam.cooking_hansu.domain.chat.entity.ChatMessage;
import lazyteam.cooking_hansu.domain.chat.entity.FileType;

import java.util.List;
import java.util.Optional;

public class ChatMessageFormatter {
    public static String formatLastMessage(Optional<ChatMessage> lastMessageOpt) {
        if (lastMessageOpt.isEmpty()) {
            return null;
        }
        ChatMessage lastMessage = lastMessageOpt.get();
        
        // 텍스트 메시지가 있는 경우
        if (hasTextMessage(lastMessage)) {
            return lastMessage.getMessageText();
        }
        
        // 파일만 있는 경우
        if (hasFiles(lastMessage)) {
            return formatFileMessage(lastMessage.getFiles());
        }
        
        return null;
    }

//     텍스트 메시지가 있는지 확인.
    private static boolean hasTextMessage(ChatMessage message) {
        return message.getMessageText() != null && 
               !message.getMessageText().trim().isEmpty();
    }

//    파일이 있는지 확인.
    private static boolean hasFiles(ChatMessage message) {
        return message.getFiles() != null && 
               !message.getFiles().isEmpty();
    }

//    파일 메시지를 포맷팅.
    private static String formatFileMessage(List<ChatFile> files) {
        if (files == null || files.isEmpty()) {
            return "파일을 보냈습니다.";
        }

        long imageCount = countFilesByType(files, FileType.IMAGE);
        long videoCount = countFilesByType(files, FileType.VIDEO);

        // 이미지와 동영상이 모두 있는 경우
        if (imageCount > 0 && videoCount > 0) {
            return String.format("사진 %d장, 동영상 %d개를 보냈습니다.", imageCount, videoCount);
        }
        
        // 이미지만 있는 경우
        if (imageCount > 0) {
            return String.format("사진 %d장을 보냈습니다.", imageCount);
        }
        
        // 동영상만 있는 경우
        if (videoCount > 0) {
            return String.format("동영상 %d개를 보냈습니다.", videoCount);
        }
        
        // 기타 파일인 경우
        return String.format("파일 %d개를 보냈습니다.", files.size());
    }

//    특정 파일 타입의 개수를 계산
    private static long countFilesByType(List<ChatFile> files, FileType fileType) {
        return files.stream()
                .filter(file -> file.getFileType() == fileType)
                .count();
    }

//    채팅방 이름을 포맷팅합니다.
//    커스텀 이름이 있으면 커스텀 이름을, 없으면 상대방 닉네임을 반환
    public static String formatChatRoomName(String customRoomName, String otherUserNickname) {
        if (customRoomName != null && !customRoomName.trim().isEmpty()) {
            return customRoomName;
        }
        return otherUserNickname + "과의 채팅";
    }
}
