package com.hongik.projectTNP.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateDto {

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private String nickname;

    @NotNull(message = "관심분야를 선택해주세요.")
    @Size(min = 3, max = 3, message = "관심분야는 정확히 3개를 선택해야 합니다.")
    private List<Long> interestIds;
}
