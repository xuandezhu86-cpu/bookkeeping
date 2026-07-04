package com.youran.bookkeeping.auth.service;

import com.youran.bookkeeping.auth.dto.LoginDto;
import com.youran.bookkeeping.auth.dto.RegisterDto;

import java.util.Map;

public interface AuthService {
    Map<String, Object> register(RegisterDto dto);
    Map<String, Object> login(LoginDto dto);
}
