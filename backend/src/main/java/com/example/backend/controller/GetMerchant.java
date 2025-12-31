package com.example.backend.controller;

import java.io.IOException;

import com.example.backend.model.merchantAndCoupon.MerchantBean;
import com.example.backend.model.merchantAndCoupon.MerchantDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/Getmerchant")
public class GetMerchant extends HttpServlet {
    private MerchantDao dao = new MerchantDao();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            int id = Integer.parseInt(request.getParameter("merchantId"));
            
            // Hibernate 直接用 ID 抓取物件
            MerchantBean m = dao.getById(id);
            
            if (m != null) {
                request.setAttribute("merchant", m);
                request.getRequestDispatcher("/WEB-INF/view/merchantandcoupon/UpdateMerchant.jsp")
                       .forward(request, response);
            } else {
                response.sendRedirect("GetAllMerchant");
            }
        } catch (Exception e) {
            response.sendRedirect("GetAllMerchant");
        }
    }
}
