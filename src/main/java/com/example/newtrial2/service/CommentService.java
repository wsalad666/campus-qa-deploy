package com.example.newtrial2.service;

import com.example.newtrial2.dto.request.CommentRequest;
import com.example.newtrial2.dto.response.CommentVO;

import java.util.List;

public interface CommentService {

    void addComment(Long userId, CommentRequest request);

    List<CommentVO> getCommentsByAnswerId(Long answerId, Long currentUserId);

    void deleteComment(Long userId, Long commentId);
}