package com.iven.memo.models.achievement;

import com.iven.memo.models.DO.User;
import jakarta.annotation.Nullable;

public interface Achievement {
    /**
     * 查看成就是否达成
     * @param currentUser 当前用户对象实体
     * @return 如果成就达成，返回true，反之为false，返回null则表示用户没有这个成就
     */
    @Nullable
    Boolean checkAchieve(User currentUser);

    /**
     * 获取成就标题，展示在app成就卡片的上方
     * @return 成就标题
     */
    String getTitle();

    /**
     * 获取成就内容，展示在app成就卡片的中心位置
     * @return 成就内容
     */
    String getContent();
}
