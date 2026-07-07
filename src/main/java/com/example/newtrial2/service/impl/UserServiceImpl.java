package com.example.newtrial2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.newtrial2.common.JwtUtil;
import com.example.newtrial2.dto.request.LoginRequest;
import com.example.newtrial2.dto.request.RegisterRequest;
import com.example.newtrial2.dto.request.UpdateProfileRequest;
import com.example.newtrial2.dto.response.LoginResponse;
import com.example.newtrial2.dto.response.UserProfileResponse;
import com.example.newtrial2.dto.response.UserVO;
import com.example.newtrial2.entity.*;
import com.example.newtrial2.exception.BusinessException;
import com.example.newtrial2.mapper.*;
import com.example.newtrial2.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final ResourceMapper resourceMapper;
    private final FollowMapper followMapper;
    private final AdminMapper adminMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper, QuestionMapper questionMapper,
                           ResourceMapper resourceMapper, FollowMapper followMapper,
                           AdminMapper adminMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.questionMapper = questionMapper;
        this.resourceMapper = resourceMapper;
        this.followMapper = followMapper;
        this.adminMapper = adminMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStudentNo, request.getStudentNo());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该学号已注册");
        }
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该用户名已被使用");
        }
        User user = new User();
        user.setStudentNo(request.getStudentNo());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getUsername());
        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStudentNo, request.getStudentNo());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("学号不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        String token = jwtUtil.generateToken(user.getId());
        LoginResponse response = new LoginResponse(token, user.getId(),
                user.getUsername(), user.getNickname(), user.getAvatar(), user.getSignature());
        return response;
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId);
        if (request.getNickname() != null) {
            wrapper.set(User::getNickname, request.getNickname());
        }
        if (request.getAvatar() != null) {
            wrapper.set(User::getAvatar, request.getAvatar());
        }
        if (request.getSignature() != null) {
            wrapper.set(User::getSignature, request.getSignature());
        }
        userMapper.update(null, wrapper);
    }

    @Override
    public void updateAvatar(Long userId, String avatarPath) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setAvatar(avatarPath);
        userMapper.updateById(user);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        fillRole(vo, userId);
        return vo;
    }

    @Override
    public UserProfileResponse getUserProfile(Long userId, Long currentUserId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        fillRole(userVO, userId);

        Long questionCount = questionMapper.selectCount(
                new LambdaQueryWrapper<Question>().eq(Question::getUserId, userId));
        Long resourceCount = resourceMapper.selectCount(
                new LambdaQueryWrapper<Resource>().eq(Resource::getUserId, userId));
        Long fansCount = followMapper.selectCount(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFollowedId, userId));
        Long followCount = followMapper.selectCount(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFollowerId, userId));

        // 判断当前登录用户是否已关注
        boolean isFollowed = false;
        if (currentUserId != null && !currentUserId.equals(userId)) {
            isFollowed = followMapper.selectCount(
                    new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getFollowerId, currentUserId)
                            .eq(Follow::getFollowedId, userId)) > 0;
        }

        UserProfileResponse response = new UserProfileResponse();
        response.setUser(userVO);
        response.setQuestionCount(questionCount);
        response.setResourceCount(resourceCount);
        response.setFansCount(fansCount);
        response.setFollowCount(followCount);
        response.setIsFollowed(isFollowed);
        return response;
    }

    @Override
    public List<UserVO> searchUsers(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getNickname, keyword)
                .or()
                .like(User::getUsername, keyword);
        List<User> users = userMapper.selectList(wrapper);
        return users.stream().map(u -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(u, vo);
            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }

    private void fillRole(UserVO vo, Long userId) {
        LambdaQueryWrapper<Admin> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(Admin::getUserId, userId);
        if (adminMapper.selectCount(adminWrapper) > 0) {
            vo.setRole(1); // 管理员
        } else {
            vo.setRole(0); // 学生
        }
    }
}
