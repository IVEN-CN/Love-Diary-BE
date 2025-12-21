package com.iven.memo.service;

import com.iven.memo.models.DTO.Achievement.AchievementDisplayDTO;

import java.util.List;

public interface AchievementService {
    /**
     * 查找所有成就
     * @return 成就列表，可以直接展示，返回前端
     */
    List<AchievementDisplayDTO> getAchievements();
}
