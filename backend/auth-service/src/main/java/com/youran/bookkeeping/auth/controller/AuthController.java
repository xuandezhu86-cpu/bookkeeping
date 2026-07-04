package com.youran.bookkeeping.auth.controller;

import com.youran.bookkeeping.auth.dto.ChangePasswordDto;
import com.youran.bookkeeping.auth.dto.LoginDto;
import com.youran.bookkeeping.auth.dto.RegisterDto;
import com.youran.bookkeeping.auth.dto.UpdateProfileDto;
import com.youran.bookkeeping.auth.entity.SysUser;
import com.youran.bookkeeping.auth.service.AuthService;
import com.youran.bookkeeping.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public Result<SysUser> me(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(authService.getCurrentUser(userId));
    }

    @PutMapping("/profile")
    @Operation(summary = "更新个人信息")
    public Result<SysUser> updateProfile(Authentication auth, @Valid @RequestBody UpdateProfileDto dto) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(authService.updateProfile(userId, dto));
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传头像")
    public Result<String> uploadAvatar(Authentication auth, @RequestParam("file") MultipartFile file) {
        Long userId = (Long) auth.getPrincipal();
        String avatarPath = authService.updateAvatar(userId, file);
        return Result.success(avatarPath);
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(Authentication auth, @Valid @RequestBody ChangePasswordDto dto) {
        Long userId = (Long) auth.getPrincipal();
        authService.changePassword(userId, dto);
        return Result.success();
    }
}
