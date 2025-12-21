package com.iven.memo.models.DTO.Achievement;

import com.iven.memo.models.DO.User;
import com.iven.memo.models.achievement.Achievement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementDisplayDTO {
    private String title;
    private String content;
    private Boolean achieved;

    public AchievementDisplayDTO(Achievement achievement, User currentUser) {
        this.achieved = achievement.checkAchieve(currentUser);
        this.title = achievement.getTitle();
        this.content = achievement.getContent();
    }
}
