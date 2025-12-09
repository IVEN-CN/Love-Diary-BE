package com.iven.memo.models.DTO.Memo;

import com.iven.memo.models.DO.Memo;
import com.iven.memo.models.Enumerate.MemoType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MemoInfoDTO {
    @NotNull(message = "日期不能为空")
    LocalDate date;
    @NotNull(message = "备忘类型不能为空")
    MemoType type;
    String details;

    public MemoInfoDTO(LocalDate date, MemoType type, String details) {
        this.date = date;
        this.type = type;
        this.details = details;
    }

    public MemoInfoDTO(Memo memo) {
        this(memo.getDate(), memo.getType(), memo.getDetails());
    }
}
