package com.example.newtrial2.config;

import com.example.newtrial2.common.AdminAuthInterceptor;
import com.example.newtrial2.common.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadPath).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absolutePath);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/uploads/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "HEAD", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 学生JWT拦截器
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/register",
                        "/api/user/login",
                        "/api/user/profile/*",
                        "/api/qa/question/list",
                        "/api/resource/list",
                        "/api/resource/detail/*",
                        "/api/resource/download/*",
                        "/api/upload/**",
                        "/api/admin/**",
                        "/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                );

        // 管理员JWT拦截器
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns(
                        "/api/admin/login",
                        "/api/admin/course/list"
                );
    }
}