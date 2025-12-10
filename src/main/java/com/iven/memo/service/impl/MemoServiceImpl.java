package com.iven.memo.service.impl;

import com.iven.memo.mapper.MemoMapper;
import com.iven.memo.models.DO.Memo;
import com.iven.memo.models.DTO.Memo.MemoInfoDTO;
import com.iven.memo.models.DTO.Memo.MemoInfoWithIdDTO;
import com.iven.memo.service.MemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemoServiceImpl implements MemoService {
    private final MemoMapper memoMapper;

    @Override
    public List<MemoInfoWithIdDTO> findAll() {
        List<Memo> memos = memoMapper.findAll();
        log.info("mybatis find memos {}", memos);
        return memos.stream().map(MemoInfoWithIdDTO::new).toList();
    }

    @Override
    public void add(MemoInfoDTO memoInfoDTO) {
        Memo memo = Memo.builder()
                .type(memoInfoDTO.getType())
                .details(memoInfoDTO.getDetails())
                .date(memoInfoDTO.getDate())
                .userId(1L)     // 目前版本，将userId设置为1，
                .build();

        memoMapper.insert(memo);
        log.info("mybatis insert memo {}", memo);
    }
}
