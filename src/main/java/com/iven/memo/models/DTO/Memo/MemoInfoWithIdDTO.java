package com.iven.memo.models.DTO.Memo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iven.memo.models.DO.Memo;
import com.iven.memo.models.Enumerate.MemoType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoInfoWithIdDTO {
    private Long id;
    @NotNull(message = "日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date;
    @NotNull(message = "备忘类型不能为空")
    MemoType type;
    String details;

    public MemoInfoWithIdDTO(Memo memo) {
        this(memo.getId(), memo.getDate(), memo.getType(), memo.getDetails());
    }

}
