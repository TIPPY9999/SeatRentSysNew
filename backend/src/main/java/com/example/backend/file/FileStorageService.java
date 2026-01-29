package com.example.backend.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // 改用 @Value 注入設定，格式為 "${設定鍵:預設值}"
    // [修正] 應與 WebConfig 中讀取圖片的設定鍵 (app.file.upload-path) 一致，
    // 才能確保圖片存儲和讀取在同一個目錄，避免圖片上傳成功卻無法顯示的問題。
    @Value("${app.file.upload-path:uploads}")
    private String uploadDir;

    private Path rootLocation;

    // @PostConstruct 表示當 Spring 初始化這個 Bean 完成後，會自動執行此方法
    @PostConstruct
    public void init() {
        // 在初始化階段，根據設定值建立 Path 物件
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("無法初始化儲存目錄", e);
        }
    }

    public String store(MultipartFile file) {
        // 1. 檢查檔案是否為空
        if (file.isEmpty()) {
            throw new RuntimeException("無法儲存空檔案");
        }

        try {
            // 2. 檔案類型檢核：僅允許圖片格式
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("不支援的檔案格式！僅允許上傳圖片檔案 (MIME type: " + contentType + ")");
            }

            // 3. 淨化檔名 (只取最後的檔案名稱，防止路徑遍歷攻擊 ../../)
            // 例如: "foo/../bar.jpg" 會變成 "bar.jpg"
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new RuntimeException("檔案名稱不得為空");
            }
            String cleanFileName = Paths.get(originalFilename).getFileName().toString();

            // 使用 UUID 隨機生成檔名，避免檔名重複覆蓋
            String filename = UUID.randomUUID().toString() + "_" + cleanFileName;
            Path destinationFile = this.rootLocation.resolve(Paths.get(filename)).normalize().toAbsolutePath();

            // 3. 安全性檢查：確保儲存路徑仍在 rootLocation 之下
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new RuntimeException("無法將檔案儲存至預期目錄之外");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return filename; // 回傳生成的檔名，之後可以存入資料庫
        } catch (IOException e) {
            throw new RuntimeException("檔案儲存失敗", e);
        }
    }

    public void delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("無法刪除檔案: " + filename, e);
        }
    }
}
