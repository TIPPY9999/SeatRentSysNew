package com.example.backend.controller;

import java.io.IOException;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.example.backend.model.merchantAndCoupon.MerchantBean;
import com.example.backend.model.merchantAndCoupon.MerchantDao;
import com.example.backend.utils.HibernateUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/UpdateMerchant")
public class UpdateMerchant extends HttpServlet {
    private MerchantDao dao = new MerchantDao();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            int id = Integer.parseInt(request.getParameter("merchantId"));
            MerchantBean m = dao.getById(id); // 先抓取持久化對象
            
            m.setMerchantName(request.getParameter("merchantName"));
            m.setMerchantPhone(request.getParameter("merchantPhone"));
            m.setMerchantStatus(Integer.parseInt(request.getParameter("merchantStatus")));
            // ...設定其他欄位

            dao.saveOrUpdate(m); // 更新商家
            
            // 使用 Hibernate HQL 處理連動更新 (更新該商家的所有優惠券)
            updateCoupons(id, m.getMerchantStatus());

            request.getSession().setAttribute("msg", "更新成功");
        } catch (Exception e) {
            request.getSession().setAttribute("msg", "更新失敗");
        }
        response.sendRedirect("GetAllMerchant");
    }

    private void updateCoupons(int mid, int mStatus) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            int newCStatus = (mStatus == 1) ? 1 : 3;
            session.createQuery("UPDATE DiscountBean SET couponStatus = :cs WHERE merchantId = :mid")
                   .setParameter("cs", newCStatus)
                   .setParameter("mid", mid)
                   .executeUpdate();
            session.getTransaction().commit();
        }
    }
}