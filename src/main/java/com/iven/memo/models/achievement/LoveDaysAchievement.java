package com.iven.memo.models.achievement;

import com.iven.memo.mapper.LoverGroupMapper;
import com.iven.memo.models.DO.LoverGroup;
import com.iven.memo.models.DO.User;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * 恋爱时间成就
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoveDaysAchievement implements Achievement {
    private final LoverGroupMapper loverGroupMapper;

    @Getter
    private final String title = "坠入爱河";
    @Getter
    private String content;

    @Override
    @Nullable
    public Boolean checkAchieve(User currentUser) {
        // 从当前对象取出伴侣组
        Optional<LoverGroup> group = loverGroupMapper.findByLoverId(currentUser.getId());
        if (group.isPresent()) {
            long loveDays = ChronoUnit.DAYS.between(group.get().getCreateTime(), LocalDate.now());
            content = "已经在一起" + loveDays + "天了";
            return true;
        }
        log.warn("当前用户没有伴侣，在确认恋爱成就成就的时候将返回null");
        return null;
    }
}
