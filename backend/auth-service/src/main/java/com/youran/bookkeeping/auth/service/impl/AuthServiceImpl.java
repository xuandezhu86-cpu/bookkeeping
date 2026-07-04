package com.youran.bookkeeping.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.youran.bookkeeping.auth.dto.ChangePasswordDto;
import com.youran.bookkeeping.auth.dto.LoginDto;
import com.youran.bookkeeping.auth.dto.RegisterDto;
import com.youran.bookkeeping.auth.dto.UpdateProfileDto;
import com.youran.bookkeeping.auth.entity.SysUser;
import com.youran.bookkeeping.auth.mapper.SysUserMapper;
import com.youran.bookkeeping.auth.service.AuthService;
import com.youran.bookkeeping.common.BusinessException;
import com.youran.bookkeeping.common.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${avatar.storage-dir:${user.home}/.youran/avatars}")
    private String avatarStorageDir;

    @Override
    public Map<String, Object> register(RegisterDto dto) {
        SysUser existing = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getUsername, dto.getUsername()));
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        sysUserMapper.insert(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    @Override
    public Map<String, Object> login(LoginDto dto) {
        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    @Override
    public SysUser getCurrentUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    @Override
    public SysUser updateProfile(Long userId, UpdateProfileDto dto) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        sysUserMapper.updateById(user);
        return user;
    }

    @Override
    public String updateAvatar(Long userId, MultipartFile file) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验文件类型（先检查 Content-Type，再检查文件魔数）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(400, "仅支持图片文件");
        }

        // 校验文件内容魔数（防止 MIME 欺骗）
        try {
            byte[] headerBytes = file.getBytes();
            if (headerBytes.length < 4) {
                throw new BusinessException(400, "无效的图片文件");
            }
            if (!isValidImageMagicBytes(headerBytes)) {
                throw new BusinessException(400, "文件内容不是有效的图片格式");
            }
        } catch (IOException e) {
            throw new BusinessException(500, "头像上传失败");
        }

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String ext = ".jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 只允许图片扩展名
            String lowerExt = ext.toLowerCase();
            if (!lowerExt.matches("\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
                ext = ".jpg";
            }
        }

        // 生成唯一文件名
        String filename = "avatar_" + userId + "_" + System.currentTimeMillis() + ext;

        try {
            Path uploadDir = Paths.get(avatarStorageDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            Path filePath = uploadDir.resolve(filename);
            // 重新写入，因为 getBytes() 已消费输入流
            file.getInputStream().transferTo(java.nio.file.Files.newOutputStream(filePath));

            // 存储相对路径
            String avatarPath = "/api/auth/avatars/" + filename;
            user.setAvatar(avatarPath);
            sysUserMapper.updateById(user);

            return avatarPath;
        } catch (IOException e) {
            throw new BusinessException(500, "头像上传失败");
        }
    }

    /**
     * 校验图片文件魔数
     */
    private boolean isValidImageMagicBytes(byte[] header) {
        if (header.length < 4) return false;
        // PNG: 89 50 4E 47
        if ((header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) return true;
        // JPEG: FF D8 FF
        if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) return true;
        if (header.length < 6) return false;
        // GIF: 47 49 46 38 39/37 61
        if (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x38 &&
            (header[4] == 0x39 || header[4] == 0x37) && header[5] == 0x61) return true;
        if (header.length < 12) return false;
        // WEBP: 52 49 46 46 .... 57 45 42 50
        if (header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46 &&
            header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) return true;
        // BMP: 42 4D
        if (header[0] == 0x42 && header[1] == 0x4D) return true;
        return false;
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDto dto) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "旧密码错误");
        }

        // 新旧密码不能相同
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BusinessException(400, "新密码不能与旧密码相同");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserMapper.updateById(user);
    }
}
