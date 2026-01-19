package com.example.backend.controller.game;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

// 根據你的截圖修正 import 路徑
import com.example.backend.model.member.Member;
import com.example.backend.repository.member.MemberRepository;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "http://localhost:5173")
public class GameController {

    @Autowired
    private MemberRepository memberRepository; // 修正為你的 MemberRepository

@PostMapping("/add-points")
@Transactional // 確保點數更新過程中發生錯誤會自動回滾
public ResponseEntity<?> addPoints(@RequestBody Map<String, Object> payload) {
    try {
        // 1. 解析參數 ( memberId 轉為 Integer )
        Integer memberId = Integer.valueOf(payload.get("memberId").toString());
        Integer newPoints = Integer.valueOf(payload.get("points").toString());

        // 2. 找到該會員 ( 使用你截圖中的 MemberRepository )
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("找不到編號為 " + memberId + " 的會員"));

        // 3. 執行累加邏輯
        // 修正點：你的 Member.java 裡面的方法名稱是 getMemPoints()
        int currentPoints = member.getMemPoints();
        int updatedTotal = currentPoints + newPoints;
        
        // 修正點：你的 Member.java 裡面的方法名稱是 setMemPoints()
        member.setMemPoints(updatedTotal);

        // 4. 更新回資料庫
        memberRepository.save(member);

        // 5. 回傳最新結果給前端
        return ResponseEntity.ok(Map.of(
            "message", "點數兌換成功！",
            "addPoints", newPoints,
            "currentPoints", updatedTotal
        ));

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body("累加失敗: " + e.getMessage());
    }
}

}