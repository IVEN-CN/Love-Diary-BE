package com.iven.memo.service.impl;

import com.iven.memo.exceptions.DataNotFound;
import com.iven.memo.exceptions.LoginFail;
import com.iven.memo.mapper.LoverGroupMapper;
import com.iven.memo.mapper.MemoMapper;
import com.iven.memo.models.DO.Memo;
import com.iven.memo.models.DO.User;
import com.iven.memo.models.DTO.Memo.MemoInfoDTO;
import com.iven.memo.models.DTO.Memo.MemoInfoWithIdDTO;
import com.iven.memo.service.MemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemoServiceImpl implements MemoService {
    private final MemoMapper memoMapper;
    private final LoverGroupMapper loverGroupMapper;

    @Override
    public List<MemoInfoWithIdDTO> findAll() {
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (currentUser == null) {
            throw new LoginFail("用户未登录");
        }

        Long loverId = loverGroupMapper.findLoverIdByUserId(currentUser.getId());
        List<Memo> memos = memoMapper.findAllByOwnerAndLoverId(currentUser.getId(), loverId);
        log.info("mybatis find memos {}", memos);
        return memos.stream().map(MemoInfoWithIdDTO::new).toList();
    }

    @Override
    public void add(MemoInfoDTO memoInfoDTO) {
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (currentUser == null) {
            throw new LoginFail("用户未登录");
        }

        Memo memo = Memo.builder()
                .type(memoInfoDTO.getType())
                .details(memoInfoDTO.getDetails())
                .date(memoInfoDTO.getDate())
                .userId(currentUser.getId())
                .build();

        memoMapper.insert(memo);
        log.info("mybatis insert memo {}", memo);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (currentUser == null) {
            throw new LoginFail("用户未登录");
        }

        if (!checkOwner(currentUser, id)) {
            log.info("mybatis delete memo {} failed, not owner", id);
            throw new DataNotFound("删除失败，备忘录不存在");
        }

        memoMapper.deleteById(id);
        log.info("mybatis delete memo {}", id);
    }

    @Override
    public void update(Long id, MemoInfoDTO memoInfoDTO) {
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (currentUser == null) {
            throw new LoginFail("用户未登录");
        }

        if (!checkOwner(currentUser, id)) {
            log.info("mybatis delete memo {} failed, not owner", id);
            throw new DataNotFound("删除失败，备忘录不存在");
        }

        Memo newMemo = Memo.builder()
                .id(id)         // 设置Id，用于更新
                .type(memoInfoDTO.getType())
                .details(memoInfoDTO.getDetails())
                .date(memoInfoDTO.getDate())
                .userId(1L)     // 目前版本，将userId设置为1，
                .build();
        int rowsAffected = memoMapper.update(newMemo);
        if (rowsAffected == 0) {
            log.warn("mybatis update rowsAffected {}", rowsAffected);
            throw new DataNotFound("更新失败，备忘录不存在");
        }
        log.info("mybatis update memo {}", newMemo);
    }

    private boolean checkOwner(@NotNull Long memoId, @NotNull Long userId) {
        Optional<Memo> memoOpt = memoMapper.findById(memoId);
        if (memoOpt.isEmpty()) {
            return false;
        }
        Memo memo = memoOpt.get();

        if (Objects.equals(memo.getUserId(), userId)) {
            return true;
        }

        // 检查是否为伴侣的备忘录
        Long loverId = loverGroupMapper.findLoverIdByUserId(userId);
        return Objects.equals(memo.getUserId(), loverId);
    }

    private boolean checkOwner(@NotNull Memo memo, Long userId) {
        return checkOwner(memo.getUserId(), userId);
    }

    private boolean checkOwner(@NotNull User currentUser, Long memoId) {
        return checkOwner(memoId, currentUser.getId());
    }
}
