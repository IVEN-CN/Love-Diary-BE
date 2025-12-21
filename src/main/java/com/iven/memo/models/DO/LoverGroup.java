package com.iven.memo.models.DO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoverGroup {
    private Long id;
    private Long user1Id;
    private Long user2Id;
    private LocalDate createTime;
}
