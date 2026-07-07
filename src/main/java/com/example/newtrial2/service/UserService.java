package com.example.newtrial2.service;

import com.example.newtrial2.dto.request.LoginRequest;
import com.example.newtrial2.dto.request.RegisterRequest;
import com.example.newtrial2.dto.request.UpdateProfileRequest;
import com.example.newtrial2.dto.response.LoginResponse;
import com.example.newtrial2.dto.response.UserProfileResponse;
import com.example.newtrial2.dto.response.UserVO;

import java.util.List;

public interface UserService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void updateProfile(Long userId, UpdateProfileRequest request);

    void updateAvatar(Long userId, String avatarPath);

    UserVO getUserInfo(Long userId);

    UserProfileResponse getUserProfile(Long userId, Long currentUserId);

    List<UserVO> searchUsers(String keyword);
}