package com.example.newtrial2.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.newtrial2.entity.Admin;
import com.example.newtrial2.mapper.AdminMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthInterceptor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AdminJwtUtil adminJwtUtil;

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            writeUnauthorized(response, "未登录，请先登录");
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 先尝试学生Token验证
        if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId != null) {
                request.setAttribute("userId", userId);
                return true;
            }
        }

        // 再尝试管理员Token验证（管理员也可以访问学生端接口）
        if (adminJwtUtil.validateToken(token)) {
            Long userId = adminJwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                // 旧版token没有userId，从数据库查询
                Long adminId = adminJwtUtil.getAdminIdFromToken(token);
                if (adminId != null) {
                    Admin admin = adminMapper.selectById(adminId);
                    if (admin != null && admin.getUserId() != null) {
                        userId = admin.getUserId();
                    }
                }
            }
            if (userId != null) {
                request.setAttribute("userId", userId);
                return true;
            }
        }

        writeUnauthorized(response, "Token无效，请重新登录");
        return false;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> result = Result.unauthorized(message);
        response.getOutputStream().write(objectMapper.writeValueAsBytes(result));
    }
}