package com.iven.memo.models.achievement;

import com.iven.memo.mapper.LoverGroupMapper;
import com.iven.memo.models.DO.LoverGroup;
import com.iven.memo.models.DO.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Getter
@RequiredArgsConstructor
public class FirstUseAchievement implements Achievement {
    private final String title = "首次使用";
    private final String content = "你已经绑定伴侣，现在可以开始记录你的伴侣日常啦";
    private final LoverGroupMapper loverGroupMapper;

    @Nullable
    @Override
    public Boolean checkAchieve(User currentUser) {
        Optional<LoverGroup> loverGroup = loverGroupMapper.findByLoverId(currentUser.getId());
        return loverGroup.isPresent() ? true : null;
    }
}
