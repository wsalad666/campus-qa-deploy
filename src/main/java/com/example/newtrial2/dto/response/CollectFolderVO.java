package com.example.newtrial2.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CollectFolderVO {
    private Long id;
    private String folderName;
    private Integer count;
    private LocalDateTime createTime;
}