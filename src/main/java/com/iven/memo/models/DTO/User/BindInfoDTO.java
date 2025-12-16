package com.iven.memo.models.DTO.User;

import com.iven.memo.models.DO.User;
import com.iven.memo.models.Enumerate.BindType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BindInfoDTO {
    private BindType bindType;
    private User fromUser;
    private User toUser;
    private String link;
}
