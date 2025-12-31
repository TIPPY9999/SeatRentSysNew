package com.example.backend;

import java.util.List;

import com.example.backend.model.merchantAndCoupon.MerchantBean;
import com.example.backend.model.merchantAndCoupon.MerchantDao;

public class MerchantTest {

    public static void main(String[] args) {
        MerchantDao dao = new MerchantDao();

        System.out.println("=== 開始測試 Hibernate Merchant 功能 ===");

        try {
            // 1. 測試新增 (Create)
            System.out.println("\n[1] 正在測試新增廠商...");
            MerchantBean newMerchant = new MerchantBean();
            newMerchant.setMerchantName("測試咖啡廳-" + System.currentTimeMillis() % 1000);
            newMerchant.setMerchantPhone("02-12345678");
            newMerchant.setMerchantEmail("test@example.com");
            newMerchant.setMerchantAddress("台北市信義區測試路 101 號");
            newMerchant.setMerchantStatus(1); // 1: 合作中

            dao.saveOrUpdate(newMerchant);
            System.out.println(">> 新增成功！廠商 ID 為: " + newMerchant.getMerchantId());

            // 2. 測試查詢全部 (Read All)
            System.out.println("\n[2] 正在測試查詢所有廠商...");
            List<MerchantBean> list = dao.getAll();
            
            if (list != null && !list.isEmpty()) {
                System.out.println(">> 查詢成功，目前共有 " + list.size() + " 筆資料：");
                for (MerchantBean m : list) {
                    System.out.printf("ID: %d | 名稱: %-15s | 狀態: %d%n", 
                        m.getMerchantId(), m.getMerchantName(), m.getMerchantStatus());
                }
            } else {
                System.out.println(">> 查詢完成，但資料庫中沒有廠商資料。");
            }

        } catch (Exception e) {
            System.err.println("!! 測試過程中發生錯誤 !!");
            e.printStackTrace();
        } finally {
            System.out.println("\n=== 測試結束 ===");
            // 如果你的 HibernateUtil 沒有自動關閉，可以在這裡強制結束程式
            System.exit(0); 
        }
    }
}