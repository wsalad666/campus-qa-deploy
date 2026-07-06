package com.example.newtrial2.service;

import com.example.newtrial2.dto.response.UserVO;

import java.util.List;

public interface FollowService {

    boolean toggleFollow(Long userId, Long followedId);

    List<UserVO> getFollowList(Long userId);

    List<UserVO> getFansList(Long userId);

    boolean isFollowed(Long userId, Long targetUserId);
}