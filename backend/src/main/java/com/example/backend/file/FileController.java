package com.example.backend.file;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.Map;

@RestController
@RequestMapping("/files") // [建議] 加上統一前綴，避免汙染根路徑
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        // 1. 儲存檔案並取得新檔名
        String fileName = fileStorageService.store(file);

        // 2. 根據當前請求的 context path，自動產生完整的 URL
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                // [注意] 這裡的 "/images/" 必須與 WebConfig 中的 addResourceHandlers 設定一致
                // 如果您的 WebConfig 是 registry.addResourceHandler("/uploads/**")... 這裡就要改成
                // "/uploads/"
                .path("/images/") // 對應 WebConfig 中的 addResourceHandlers
                .path(fileName)
                .toUriString();

        // 3. 回傳一個 JSON 物件，包含檔名和完整的 URL
        return ResponseEntity.ok(Map.of(
                "fileName", fileName, // 供存入資料庫
                "url", fileDownloadUri // 供前端即時預覽
        ));
    }
}
