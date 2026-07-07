package com.example.newtrial2.service;

import com.example.newtrial2.dto.request.AnswerRequest;
import com.example.newtrial2.dto.request.QuestionRequest;
import com.example.newtrial2.dto.response.QuestionDetailResponse;
import com.example.newtrial2.dto.response.QuestionVO;
import com.example.newtrial2.dto.response.PageResult;
import com.example.newtrial2.dto.response.SimilarQuestionVO;

import java.util.List;

public interface QuestionService {

    void publishQuestion(Long userId, QuestionRequest request);

    PageResult<QuestionVO> pageQuestions(List<Long> courseIds, String keyword, String sort, Integer pageNum, Integer pageSize, Long currentUserId, Long userId);

    QuestionDetailResponse getQuestionDetail(Long questionId, Long currentUserId);

    void answerQuestion(Long userId, AnswerRequest request);

    boolean likeAnswer(Long userId, Long answerId);

    boolean likeQuestion(Long userId, Long questionId);

    void acceptAnswer(Long userId, Long answerId);

    void deleteQuestion(Long userId, Long questionId);

    void deleteAnswer(Long userId, Long answerId);

    void closeQuestion(Long userId, Long questionId);

    void hideQuestion(Long userId, Long questionId);

    void unhideQuestion(Long userId, Long questionId);

    void reopenQuestion(Long userId, Long questionId);

    /** 获取相似问题推荐 */
    List<SimilarQuestionVO> getSimilarQuestions(Long courseId, Long excludeId, String title, String content, int limit);
}