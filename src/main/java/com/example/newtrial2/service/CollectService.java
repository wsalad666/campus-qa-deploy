package com.example.newtrial2.service;

import com.example.newtrial2.dto.response.CollectFolderVO;
import com.example.newtrial2.dto.response.CollectItemVO;
import java.util.List;

public interface CollectService {
    /** 获取用户所有收藏文件夹 */
    List<CollectFolderVO> getFolders(Long userId);
    /** 创建收藏文件夹 */
    CollectFolderVO createFolder(Long userId, String folderName);
    /** 重命名文件夹 */
    void renameFolder(Long userId, Long folderId, String newName);
    /** 删除文件夹 */
    void deleteFolder(Long userId, Long folderId);
    /** 收藏/取消收藏(切换) */
    boolean toggleCollect(Long userId, Long folderId, Integer targetType, Long targetId);
    /** 收藏到多个文件夹 */
    void collectToFolders(Long userId, List<Long> folderIds, Integer targetType, Long targetId);
    /** 获取文件夹内的收藏列表 */
    List<CollectItemVO> getFolderCollects(Long userId, Long folderId, Integer targetType);
    /** 检查是否已收藏 */
    boolean isCollected(Long userId, Long folderId, Integer targetType, Long targetId);
    /** 检查是否已收藏（任意文件夹） */
    boolean isCollectedAny(Long userId, Integer targetType, Long targetId);
    /** 移动收藏到其他文件夹 */
    void moveCollect(Long userId, Long relationId, Long targetFolderId);
    /** 取消收藏 */
    void removeCollect(Long userId, Long relationId);
    void removeCollectByTarget(Long userId, Integer targetType, Long targetId);
    /** 鍙栨秷鏀惰棌锛堟寜鐩爣锛?*/
}