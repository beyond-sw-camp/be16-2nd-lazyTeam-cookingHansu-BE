package lazyteam.cooking_hansu.domain.post.service;

import lazyteam.cooking_hansu.domain.post.dto.MyPostListDto;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.post.repository.PostRepository;
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
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Value("${my.test.user-id}")
    private String testUserIdStr;

    @Transactional(readOnly = true)
    public List<MyPostListDto> myPostList() {
        UUID testUserId = UUID.fromString(testUserIdStr);
        User user = userRepository.findById(testUserId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        List<Post> posts = postRepository.findAllByUser(user);

        return posts.stream()
                .map(post -> MyPostListDto.builder()
                        .title(post.getTitle())
                        .description(post.getDescription())
                        .thumbnailUrl(post.getThumbnailUrl())
                        .createdAt(post.getCreatedAt())
                        .likeCount(post.getLikeCount())
                        .bookmarkCount(post.getBookmarkCount())
                        .build())
                .collect(Collectors.toList());
    }
}