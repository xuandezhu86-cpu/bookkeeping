package com.youran.bookkeeping.auth.controller;

import com.youran.bookkeeping.auth.dto.LoginDto;
import com.youran.bookkeeping.auth.dto.RegisterDto;
import com.youran.bookkeeping.auth.service.AuthService;
import com.youran.bookkeeping.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDto dto) {
        return Result.success(authService.register(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDto dto) {
        return Result.success(authService.login(dto));
    }
}
