package com.example.backend;
import java.util.List;

import com.example.backend.model.merchantAndCoupon.DiscountBean;
import com.example.backend.model.merchantAndCoupon.DiscountDao;

public class DiscountTest {
    public static void main(String[] args) {
        DiscountDao dao = new DiscountDao();

        System.out.println("=== 1. 測試自動更新狀態 ===");
        dao.autoUpdateStatus();
        System.out.println("狀態更新執行完成。");

        System.out.println("\n=== 2. 測試查詢全部 (含商家名稱) ===");
        List<DiscountBean> list = dao.findAllWithMerchant("");
        for (DiscountBean d : list) {
            System.out.printf("ID: %d | 名稱: %-15s | 商家: %-10s | 狀態: %d%n", 
                d.getCouponId(), d.getCouponName(), d.getMerchantName(), d.getCouponStatus());
        }

        System.out.println("\n=== 3. 測試關鍵字搜尋 (例如 '折') ===");
        List<DiscountBean> searchResult = dao.findAllWithMerchant("折");
        System.out.println("搜尋到數量: " + searchResult.size());

        System.out.println("\n=== 測試完成 ===");
    }
}