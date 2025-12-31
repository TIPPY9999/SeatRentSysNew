package com.example.backend.controller;

import java.io.IOException;

import com.example.backend.model.merchantAndCoupon.DiscountDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/UpdateDiscountStatus")
public class UpdateDiscountStatus extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            int couponId = Integer.parseInt(request.getParameter("couponId"));
            String action = request.getParameter("newStatus"); // "disable" 或 "relist"

            DiscountDao dao = new DiscountDao();
            dao.updateStatus(couponId, action);

            response.sendRedirect(request.getContextPath() + "/GetAllDiscount");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}