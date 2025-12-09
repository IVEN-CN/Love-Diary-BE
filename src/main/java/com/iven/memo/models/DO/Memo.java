package com.iven.memo.models.DO;

import com.iven.memo.models.Enumerate.MemoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Memo {
    Long id;
    Long userId;
    MemoType type;
    String details;
    LocalDate date;
}
