package com.example.backend.controller;

import java.io.IOException;

import com.example.backend.model.merchantAndCoupon.MerchantBean;
import com.example.backend.model.merchantAndCoupon.MerchantDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/InsertMerchant")
public class InsertMerchant extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // 初始化 DAO
    private MerchantDao dao = new MerchantDao();

    // GET：只顯示表單 (維持不變)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/view/merchantandcoupon/AddMerchant.jsp")
               .forward(request, response);
    }

    // POST：使用 Hibernate 新增資料
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            // 1. 取得參數
            String merchantName    = request.getParameter("merchantName");
            String merchantPhone   = request.getParameter("merchantPhone");
            String merchantEmail   = request.getParameter("merchantEmail");
            String merchantAddress = request.getParameter("merchantAddress");
            String statusStr       = request.getParameter("merchantStatus");
            
            int merchantStatus = (statusStr == null || statusStr.isEmpty())
                                ? 1 : Integer.parseInt(statusStr);

            // 2. 封裝成 Bean 物件 (這就是 Hibernate 的 ORM 概念)
            MerchantBean merchant = new MerchantBean();
            merchant.setMerchantName(merchantName);
            merchant.setMerchantPhone(merchantPhone);
            merchant.setMerchantEmail(merchantEmail);
            merchant.setMerchantAddress(merchantAddress);
            merchant.setMerchantStatus(merchantStatus);

            // 3. 呼叫 DAO 進行儲存 (Hibernate 會自動生成 INSERT SQL)
            dao.saveOrUpdate(merchant);

            // 4. 成功處理
            request.getSession().setAttribute("msgType", "success");
            request.getSession().setAttribute("msg", "新增廠商成功！");
            response.sendRedirect(request.getContextPath() + "/GetAllMerchant");

        } catch (Exception e) {
            e.printStackTrace();
            // 5. 失敗處理
            request.getSession().setAttribute("msgType", "error");
            request.getSession().setAttribute("msg", "新增廠商失敗：" + e.getMessage());
            
            // 轉發回清單 Servlet
            request.getRequestDispatcher("/GetAllMerchant").forward(request, response);
        }
    }
}