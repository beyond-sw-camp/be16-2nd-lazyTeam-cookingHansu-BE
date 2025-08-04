package lazyteam.cooking_hansu.domain.mypage.service;


import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.interaction.repository.BookmarkRepository;
import lazyteam.cooking_hansu.domain.mypage.dto.MyBookmarkLikedListDto;
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
    public List<MyBookmarkLikedListDto> myBookmarkedPosts() {
        UUID userId = UUID.fromString(testUserIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));

        return bookmarkRepository.findAllByUser(user).stream()
                .map(bookmark -> {
                    Post post = bookmark.getPost();
                    return MyBookmarkLikedListDto.builder()
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

