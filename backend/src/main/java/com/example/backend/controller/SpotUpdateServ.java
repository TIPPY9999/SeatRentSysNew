package com.example.backend.controller;

import java.io.IOException;
import java.math.BigDecimal;

import com.example.backend.model.RentalSpot;
import com.example.backend.service.RentalSpotService;
import com.example.backend.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/spot/update")
public class SpotUpdateServ extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String spotIdStr = req.getParameter("spotId");
        Integer spotId = null;
        try {
            // 修改說明：將資料解析移至交易前，若格式錯誤直接拋出例外，避免開啟無謂的資料庫交易浪費資源
            spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr);
        } catch (NumberFormatException e) {
            throw new ServletException("spotId 格式錯誤", e);
        }

        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.getCurrentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            if (spotId != null) {
                RentalSpotService rentalSpotService = new RentalSpotService(session);
                RentalSpot spot = rentalSpotService.selectById(spotId);
                req.setAttribute("spot", spot);
            }
            tx.commit();
            req.getRequestDispatcher("/WEB-INF/view/spotUpdate.jsp").forward(req, res);
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String spotIdStr = req.getParameter("spotId");
        String spotCode = req.getParameter("spotCode");
        String spotName = req.getParameter("spotName");
        String spotAddress = req.getParameter("spotAddress");
        String spotStatus = req.getParameter("spotStatus");

        String merchantIdStr = req.getParameter("merchantId");
        String latStr = req.getParameter("latitude");
        String lonStr = req.getParameter("longitude");

        Integer spotId = null;
        Integer merchantId = null;
        BigDecimal latitude = null;
        BigDecimal longitude = null;

        try {
            // 修改說明：統一在交易前處理資料轉型，確保進入交易時資料已準備就緒，減少交易內的運算成本
            spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr);
            merchantId = (merchantIdStr == null || merchantIdStr.isBlank()) ? null : Integer.valueOf(merchantIdStr);
            latitude = (latStr == null || latStr.isBlank()) ? null : new BigDecimal(latStr);
            longitude = (lonStr == null || lonStr.isBlank()) ? null : new BigDecimal(lonStr);
        } catch (Exception e) {
            throw new ServletException("輸入資料格式錯誤", e);
        }

        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.getCurrentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            RentalSpotService rentalSpotService = new RentalSpotService(session);

            if (spotId != null) {
                // 修改說明：採用「先查詢，後更新」模式，確保只更新表單提供的欄位，避免遺失原資料庫中未修改的欄位資料(如建立時間等)
                // 1. 先從資料庫取出舊資料
                RentalSpot spot = rentalSpotService.selectById(spotId);

                if (spot != null) {
                    // 2. 更新欄位
                    spot.setSpotCode(spotCode);
                    spot.setSpotName(spotName);
                    spot.setSpotAddress(spotAddress);
                    spot.setSpotStatus(spotStatus);

                    // 處理數值型別
                    spot.setMerchantId(merchantId);
                    spot.setLatitude(latitude);
                    spot.setLongitude(longitude);

                    // 3. 執行更新 (Hibernate 會自動偵測變更，明確呼叫 update 也可以)
                    rentalSpotService.update(spot);
                }
            }
            tx.commit();
            res.sendRedirect(req.getContextPath() + "/spot/list");
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw new ServletException(e);
        }
    }
}
