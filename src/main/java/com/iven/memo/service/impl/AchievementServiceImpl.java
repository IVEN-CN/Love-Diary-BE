package com.iven.memo.service.impl;

import com.iven.memo.exceptions.LoginFail;
import com.iven.memo.models.DO.User;
import com.iven.memo.models.DTO.Achievement.AchievementDisplayDTO;
import com.iven.memo.models.achievement.SystemAchievements;
import com.iven.memo.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {
    private final SystemAchievements systemAchievements;

    @Override
    public List<AchievementDisplayDTO> getAchievements() {
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (currentUser == null) {
            throw new LoginFail("用户未登录");
        }

        return systemAchievements.getAchievements().stream()
                .map(achievement -> {
                    AchievementDisplayDTO displayDTO = new AchievementDisplayDTO(achievement, currentUser);
                    return displayDTO.getAchieved() == null ? null : displayDTO;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
