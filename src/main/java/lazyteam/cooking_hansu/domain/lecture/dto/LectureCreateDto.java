package lazyteam.cooking_hansu.domain.lecture.dto;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.LevelEnum;
import lazyteam.cooking_hansu.domain.common.StatusEnum;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class LectureCreateDto {

    private Long submittedId;

    private User user;

    private String title;

    private String description;

    private LevelEnum level;

    private CategoryEnum category;

    private Integer price;

    private MultipartFile imageFile;

    private StatusEnum status;


    private String createdAt;

    private String updatedAt;

}
