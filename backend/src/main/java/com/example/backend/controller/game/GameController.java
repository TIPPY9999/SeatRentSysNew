package com.example.backend.controller.game;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game")
// 確保這裡的 origins 符合你的 Vue 網址
@CrossOrigin(origins = "http://localhost:5173")
public class GameController {

    @PostMapping("/score")
    public ResponseEntity<Map<String, String>> saveScore(@RequestBody Map<String, Integer> data) {
        Integer finalScore = data.get("score");

        // 在 Console 印出結果，確認有收到
        System.out.println("成功接收到分數: " + finalScore);

        // 這裡可以寫入資料庫邏輯，例如：
        // scoreService.save(new ScoreEntity("Player1", finalScore));

        return ResponseEntity.ok(Map.of("message", "Score saved successfully!"));
    }
}