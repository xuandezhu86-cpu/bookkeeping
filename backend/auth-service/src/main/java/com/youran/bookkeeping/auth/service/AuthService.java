package com.youran.bookkeeping.auth.service;

import com.youran.bookkeeping.auth.dto.ChangePasswordDto;
import com.youran.bookkeeping.auth.dto.LoginDto;
import com.youran.bookkeeping.auth.dto.RegisterDto;
import com.youran.bookkeeping.auth.dto.UpdateProfileDto;
import com.youran.bookkeeping.auth.entity.SysUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface AuthService {
    Map<String, Object> register(RegisterDto dto);
    Map<String, Object> login(LoginDto dto);
    SysUser getCurrentUser(Long userId);
    SysUser updateProfile(Long userId, UpdateProfileDto dto);
    String updateAvatar(Long userId, MultipartFile file);
    void changePassword(Long userId, ChangePasswordDto dto);
}
