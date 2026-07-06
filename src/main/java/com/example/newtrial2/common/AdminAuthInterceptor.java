package com.example.newtrial2.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    public static final String ADMIN_ID_KEY = "adminId";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AdminJwtUtil adminJwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            writeUnauthorized(response, "管理员未登录");
            return false;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!adminJwtUtil.validateToken(token)) {
            writeUnauthorized(response, "管理员Token无效或已过期");
            return false;
        }
        Long adminId = adminJwtUtil.getAdminIdFromToken(token);
        if (adminId == null) {
            writeUnauthorized(response, "管理员Token解析失败");
            return false;
        }
        request.setAttribute("adminId", adminId);
        
        // 自动续期：如果token剩余有效期不足50%，自动签发新token
        if (adminJwtUtil.shouldRenewToken(token)) {
            Long userId = adminJwtUtil.getUserIdFromToken(token);
            String newToken = adminJwtUtil.generateToken(adminId, userId);
            response.setHeader("X-New-Token", newToken);
        }
        
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> result = Result.unauthorized(message);
        response.getOutputStream().write(objectMapper.writeValueAsBytes(result));
    }
}