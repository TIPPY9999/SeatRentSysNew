package com.example.backend.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.example.backend.model.merchantAndCoupon.DiscountBean;
import com.example.backend.model.merchantAndCoupon.DiscountDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/GetAllDiscount")
public class GetAllDiscount extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String keyword = request.getParameter("keyword");
        if (keyword == null) keyword = "";

      DiscountDao dao = new DiscountDao();
        dao.autoUpdateStatus(); // 1. 先跑排程更新狀態
        
        // 2. 獲取清單（含商家名稱）
        List<DiscountBean> discounts = dao.findAllWithMerchant(keyword);
        
        request.setAttribute("discounts", discounts);
        request.setAttribute("keyword", keyword);
        request.setAttribute("now", LocalDate.now());
        
        request.getRequestDispatcher("/WEB-INF/view/merchantandcoupon/GetAllDiscount.jsp")
               .forward(request, response);
    }
}