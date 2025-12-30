package com.example.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

// @ControllerAdvice 的意思就是：我是所有 Controller 的「全域顧問/經理」
@ControllerAdvice(basePackages = "com.example.backend.controller.maintenance")
public class GlobalExceptionHandler {

    // 1. 專門處理 Service 丟出的「參數錯誤」 (例如：找不到 ID、必填沒填)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", e.getMessage()); // 這裡會抓到你在 Service 寫的中文錯誤訊息
        
        // 回傳 HTTP 400 狀態碼
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // 2. 專門處理 Service 丟出的「狀態錯誤」 (例如：已結案不能再取消)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Conflict"); 
        errorResponse.put("message", e.getMessage()); // 抓取 Service 的錯誤訊息
        
        // 回傳 HTTP 409 (Conflict) 代表狀態衝突
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    // 3. 處理其他所有未預期的錯誤 (例如 NullPointerException)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        // 在後端 Console 印出錯誤，方便你自己除錯
        e.printStackTrace(); 
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "系統發生錯誤，請聯繫管理員");
        
        // 回傳 HTTP 500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}