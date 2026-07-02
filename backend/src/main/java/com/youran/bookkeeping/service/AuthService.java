package com.youran.bookkeeping.service;

import com.youran.bookkeeping.dto.LoginDto;
import com.youran.bookkeeping.dto.RegisterDto;

import java.util.Map;

public interface AuthService {
    Map<String, Object> register(RegisterDto dto);
    Map<String, Object> login(LoginDto dto);
}
