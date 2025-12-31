package com.example.backend.controller;

import java.io.IOException;

import com.example.backend.model.merchantAndCoupon.DiscountBean;
import com.example.backend.model.merchantAndCoupon.DiscountDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/GetDiscount")
public class GetDiscount extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("couponId");
        if (idStr != null) {
            DiscountDao dao = new DiscountDao();
            DiscountBean d = dao.findById(Integer.parseInt(idStr));
            
            if (d != null) {
                request.setAttribute("discount", d);
                request.getRequestDispatcher("/WEB-INF/view/merchantandcoupon/UpdateDiscount.jsp")
                       .forward(request, response);
                return;
            }
        }
        response.sendRedirect(request.getContextPath() + "/GetAllDiscount");
    }
}