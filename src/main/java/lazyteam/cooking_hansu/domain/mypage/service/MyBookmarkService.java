package lazyteam.cooking_hansu.domain.mypage.service;


import lazyteam.cooking_hansu.domain.interaction.repository.BookmarkRepository;
import lazyteam.cooking_hansu.domain.mypage.dto.MyBookmarkListDto;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyBookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    @Value("${my.test.user-id}")
    private String testUserIdStr;

    @Transactional(readOnly = true)
    public List<MyBookmarkListDto> myBookmarkedPosts() {
        UUID userId = UUID.fromString(testUserIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return bookmarkRepository.findAllByUser(user).stream()
                .map(bookmark -> {
                    Post post = bookmark.getPost();
                    return MyBookmarkListDto.builder()
                            .title(post.getTitle())
                            .description(post.getDescription())
                            .thumbnailUrl(post.getThumbnailUrl())
                            .likeCount(post.getLikeCount())
                            .bookmarkCount(post.getBookmarkCount())
                            .writerNickname(post.getUser().getNickname())
                            .createdAt(post.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }
}

