package com.example.newtrial2.service;

import com.example.newtrial2.dto.response.FavoriteVO;
import com.example.newtrial2.dto.response.PageResult;

public interface FavoriteService {

    boolean toggleFavorite(Long userId, Long targetId, Integer type);

    PageResult<FavoriteVO> getMyFavorites(Long userId, Integer pageNum, Integer pageSize);

    boolean isFavorited(Long userId, Long targetId, Integer type);
}