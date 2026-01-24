package com.example.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.service.AnalyzeService;

@RestController
@RequestMapping("/api/analyze")
public class AnalyzeController {

    @Autowired
    private AnalyzeService analyzeService;

    /**
     * 取得儀表板統計數據
     * API: GET /api/analyze/stats
     * 回傳: JSON 物件，包含 cityDistribution, spotMonitor, hourlyHeatMap, durationStats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAnalyzeStats() {
        Map<String, Object> stats = analyzeService.getAnalyzeStats();
        return ResponseEntity.ok(stats);
    }
}