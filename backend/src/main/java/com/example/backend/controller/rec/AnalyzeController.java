package com.example.backend.controller.rec;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.service.rec.AnalyzeService;

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
    public ResponseEntity<Map<String, Object>> getAnalyzeStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // 將接收到的日期參數傳遞給 Service
        Map<String, Object> stats = analyzeService.getAnalyzeStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    /**
     * 取得站點即時監控數據（專供調度中心中心使用）
     * API: GET /api/analyze/spot-monitor
     */
    @GetMapping("/spot-monitor")
    public ResponseEntity<Object> getSpotMonitor() {
        // 監控頁面通常不需要日期篩選，傳入 null
        Map<String, Object> stats = analyzeService.getAnalyzeStats(null, null);
        // DispatchMonitor.vue 期望直接收到 List<SpotMonitor>
        return ResponseEntity.ok(stats.get("spotMonitor"));
    }
}