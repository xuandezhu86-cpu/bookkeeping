package com.youran.bookkeeping.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新个人信息请求")
public class UpdateProfileDto {

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;
}
