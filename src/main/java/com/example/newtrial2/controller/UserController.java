package com.example.newtrial2.controller;

import com.example.newtrial2.common.Result;
import com.example.newtrial2.dto.request.*;
import com.example.newtrial2.dto.response.*;
import com.example.newtrial2.exception.BusinessException;
import com.example.newtrial2.file.FileService;
import com.example.newtrial2.service.FavoriteService;
import com.example.newtrial2.service.FollowService;
import com.example.newtrial2.service.UserService;
import com.example.newtrial2.service.CollectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "用户模块", description = "注册、登录、个人资料、关注、收藏")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final FileService fileService;
    private final FavoriteService favoriteService;
    private final FollowService followService;
    private final CollectService collectService;

    public UserController(UserService userService, FileService fileService,
                          FavoriteService favoriteService, FollowService followService,
                          CollectService collectService) {
        this.userService = userService;
        this.fileService = fileService;
        this.favoriteService = favoriteService;
        this.followService = followService;
        this.collectService = collectService;
    }

    @Operation(summary = "学号注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @Operation(summary = "修改个人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(HttpServletRequest httpRequest,
                                       @RequestBody UpdateProfileRequest request) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userService.updateProfile(userId, request);
        return Result.success();
    }

    @Operation(summary = "上传用户头像")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadAvatar(HttpServletRequest httpRequest,
                                        @RequestParam("file") MultipartFile file) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        try {
            String[] allowedTypes = {"image/jpeg", "image/png"};
            long maxSize = 5 * 1024 * 1024;
            String filePath = fileService.uploadFile(file, allowedTypes, maxSize);
            userService.updateAvatar(userId, filePath);
            return Result.success("头像上传成功", filePath);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("头像上传失败，请重试");
        }
    }

    @Operation(summary = "切换关注/取消关注")
    @PostMapping("/follow/toggle")
    public Result<Boolean> toggleFollow(HttpServletRequest httpRequest,
                                         @Valid @RequestBody FollowRequest request) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(followService.toggleFollow(userId, request.getFollowedId()));
    }

    @Operation(summary = "我的关注列表")
    @GetMapping("/follow/list")
    public Result<List<UserVO>> getFollowList(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(followService.getFollowList(userId));
    }

    @Operation(summary = "我的粉丝列表")
    @GetMapping("/fans/list")
    public Result<List<UserVO>> getFansList(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(followService.getFansList(userId));
    }

    @Operation(summary = "切换收藏/取消收藏")
    @PostMapping("/favorite/toggle")
    public Result<Boolean> toggleFavorite(HttpServletRequest httpRequest,
                                           @Valid @RequestBody FavoriteRequest request) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(favoriteService.toggleFavorite(userId, request.getTargetId(), request.getType()));
    }

    @Operation(summary = "分页查询我的收藏")
    @GetMapping("/favorite/list")
    public Result<PageResult<FavoriteVO>> getMyFavorites(
            HttpServletRequest httpRequest,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(favoriteService.getMyFavorites(userId, pageNum, pageSize));
    }

    @Operation(summary = "个人主页")
    @GetMapping("/profile/{userId}")
    public Result<UserProfileResponse> getUserProfile(
            HttpServletRequest httpRequest,
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        Long currentUserId = (Long) httpRequest.getAttribute("userId");
        return Result.success(userService.getUserProfile(userId, currentUserId));
    }

    @Operation(summary = "搜索用户")
    @GetMapping("/search")
    public Result<List<UserVO>> searchUsers(
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        return Result.success(userService.searchUsers(keyword));
    }

    // ========== 收藏夹管理 ==========

    @Operation(summary = "获取收藏文件夹列表")
    @GetMapping("/collect/folders")
    public Result<List<CollectFolderVO>> getCollectFolders(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(collectService.getFolders(userId));
    }

    @Operation(summary = "创建收藏文件夹")
    @PostMapping("/collect/folder")
    public Result<CollectFolderVO> createCollectFolder(HttpServletRequest httpRequest,
                                                        @RequestBody Map<String, String> body) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(collectService.createFolder(userId, body.get("folderName")));
    }

    @Operation(summary = "重命名收藏文件夹")
    @PutMapping("/collect/folder/{folderId}")
    public Result<Void> renameCollectFolder(HttpServletRequest httpRequest,
                                             @PathVariable Long folderId,
                                             @RequestBody Map<String, String> body) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        collectService.renameFolder(userId, folderId, body.get("folderName"));
        return Result.success();
    }

    @Operation(summary = "删除收藏文件夹")
    @DeleteMapping("/collect/folder/{folderId}")
    public Result<Void> deleteCollectFolder(HttpServletRequest httpRequest,
                                             @PathVariable Long folderId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        collectService.deleteFolder(userId, folderId);
        return Result.success();
    }

    @Operation(summary = "收藏到文件夹")
    @PostMapping("/collect/add")
    public Result<Void> addCollect(HttpServletRequest httpRequest,
                                    @RequestBody Map<String, Object> body) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<Long> folderIds = body.get("folderIds") != null
                ? ((List<Integer>) body.get("folderIds")).stream().map(Long::valueOf).collect(java.util.stream.Collectors.toList())
                : null;
        Integer targetType = ((Number) body.get("targetType")).intValue();
        Long targetId = ((Number) body.get("targetId")).longValue();
        collectService.collectToFolders(userId, folderIds, targetType, targetId);
        return Result.success();
    }

    @Operation(summary = "获取文件夹收藏列表")
    @GetMapping("/collect/items")
    public Result<List<CollectItemVO>> getCollectItems(
            HttpServletRequest httpRequest,
            @Parameter(description = "文件夹ID") @RequestParam Long folderId,
            @Parameter(description = "类型") @RequestParam(required = false) Integer targetType) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(collectService.getFolderCollects(userId, folderId, targetType));
    }

    @Operation(summary = "检查是否已收藏")
        @Operation(summary = "检查是否已收藏（任意文件夹）")
    @GetMapping("/collect/check-any")
    public Result<Boolean> isCollectedAny(
            HttpServletRequest httpRequest,
            @Parameter(description = "类型") @RequestParam Integer targetType,
            @Parameter(description = "目标ID") @RequestParam Long targetId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(collectService.isCollectedAny(userId, targetType, targetId));
    }

@GetMapping("/collect/check")
    public Result<Boolean> checkCollected(
            HttpServletRequest httpRequest,
            @Parameter(description = "文件夹ID") @RequestParam Long folderId,
            @Parameter(description = "类型") @RequestParam Integer targetType,
            @Parameter(description = "目标ID") @RequestParam Long targetId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(collectService.isCollected(userId, folderId, targetType, targetId));
    }

    @Operation(summary = "移动收藏到其他文件夹")
    @PutMapping("/collect/move/{relationId}")
    public Result<Void> moveCollect(HttpServletRequest httpRequest,
                                     @PathVariable Long relationId,
                                     @RequestBody Map<String, Long> body) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        collectService.moveCollect(userId, relationId, body.get("folderId"));
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/collect/{relationId}")
    public Result<Void> removeCollect(HttpServletRequest httpRequest,
                                       @PathVariable Long relationId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        collectService.removeCollect(userId, relationId);
        return Result.success();
    }

    @Operation(summary = "鍙栨秷鏀惰棌锛堟寜鐩爣锛?)
    @DeleteMapping("/collect/target")
    public Result<Void> removeCollectByTarget(
            HttpServletRequest httpRequest,
            @Parameter(description = "绫诲瀷") @RequestParam Integer targetType,
            @Parameter(description = "鐩爣ID") @RequestParam Long targetId) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        collectService.removeCollectByTarget(userId, targetType, targetId);
        return Result.success();
    }

}