package com.iven.memo.controller;

import com.iven.memo.models.DTO.Achievement.AchievementDisplayDTO;
import com.iven.memo.models.Message.ResponseMessage;
import com.iven.memo.service.AchievementService;
import com.iven.memo.service.impl.AchievementServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
@RequestMapping("/achievements")
@RequiredArgsConstructor
public class AchievementController {
    private final AchievementService achievementService;

    @GetMapping
    public ResponseMessage<List<AchievementDisplayDTO>> getAchievements() {
        List<AchievementDisplayDTO> achievementDisplayDTOList = achievementService.getAchievements();
        return ResponseMessage.success(achievementDisplayDTOList);
    }
}
