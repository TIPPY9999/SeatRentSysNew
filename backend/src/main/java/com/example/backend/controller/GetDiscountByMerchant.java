package com.example.backend.controller;

import java.io.IOException;
import java.util.List;

import com.example.backend.model.merchantAndCoupon.DiscountBean;
import com.example.backend.model.merchantAndCoupon.DiscountDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/GetDiscountByMerchant")
public class GetDiscountByMerchant extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        String mIdStr = request.getParameter("merchantId");
        if (mIdStr != null && !mIdStr.isEmpty()) {
            DiscountDao dao = new DiscountDao();
            List<DiscountBean> list = dao.findByMerchantId(Integer.parseInt(mIdStr));
            
            request.setAttribute("discounts", list);
            request.getRequestDispatcher("/WEB-INF/view/merchantandcoupon/GetAllDiscount.jsp")
                   .forward(request, response);
        } else {
            response.sendRedirect("GetAllMerchant");
        }
    }
}