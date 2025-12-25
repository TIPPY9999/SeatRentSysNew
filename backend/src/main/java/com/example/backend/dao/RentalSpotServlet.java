package com.example.backend.dao;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.example.backend.model.RentalSpot;
import com.example.backend.service.RentalSpotService;
import com.example.backend.utils.HibernateUtil;

@WebServlet("/rental/insert")
public class RentalSpotServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // 設定編碼，避免中文亂碼
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        // 1. 【最外層】在這裡取得 Session
        // 這是整個請求的起點，Session 從這裡誕生
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            // 2. 【交易開始】
            session.beginTransaction();

            // 3. 【注入 Session】
            // 這裡就是「控制反轉」的關鍵：Service 被動接收 session，而不是自己建立
            RentalSpotService service = new RentalSpotService(session);

            // 4. 準備資料 (模擬從前端表單接收資料)
            String code = "SPOT-001";
            String name = "台北車站據點";
            String status = "Active";

            RentalSpot spot = new RentalSpot(code, name, status);
            // 假設有更多欄位...
            // spot.setLatitude(new BigDecimal("25.0478"));

            // 5. 呼叫 Service 執行邏輯 (Service 會再去呼叫 DAO)
            service.insert(spot);

            // 6. 【交易提交】
            // 只有當 Service 和 DAO 都沒報錯，才會執行到這裡
            session.getTransaction().commit();

            resp.getWriter().write("Insert Success!");

        } catch (Exception e) {
            // 7. 【交易回滾】發生任何錯誤，全部還原
            session.getTransaction().rollback();
            e.printStackTrace();
            resp.getWriter().write("Error: " + e.getMessage());
        } finally {
            // 8. 【關閉資源】確保 Session 一定被關閉
            session.close();
        }
    }
}