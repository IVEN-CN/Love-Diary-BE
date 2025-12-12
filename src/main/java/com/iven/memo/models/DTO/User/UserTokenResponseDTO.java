package com.iven.memo.models.DTO.User;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTokenResponseDTO {
    private String token;
    private String userName;
    private String nickName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
}
