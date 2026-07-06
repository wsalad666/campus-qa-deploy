package com.example.newtrial2.common;

import com.example.newtrial2.exception.BusinessException;
import com.example.newtrial2.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        logger.error("系统异常", e);
        return Result.error("系统内部错误，请稍后重试");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return Result.error(400, "参数校验失败");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("参数错误: {}", e.getMessage());
        return Result.badRequest(e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Result<Object> handleResourceNotFoundException(ResourceNotFoundException e) {
        return Result.notFound(e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Object> handleBusinessException(BusinessException e) {
        logger.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<Object> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        logger.warn("数据库唯一约束冲突: {}", e.getMessage());
        return Result.error("操作过于频繁，请稍后重试");
    }

    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public Result<Object> handleDuplicateKeyException(org.springframework.dao.DuplicateKeyException e) {
        logger.warn("重复键异常: {}", e.getMessage());
        return Result.error("操作过于频繁，请稍后重试");
    }
}