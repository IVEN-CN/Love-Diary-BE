package com.iven.memo.models.achievement;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
public class SystemAchievements {
    private final List<Achievement> achievements;

    @Autowired
    public SystemAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
    }
}
