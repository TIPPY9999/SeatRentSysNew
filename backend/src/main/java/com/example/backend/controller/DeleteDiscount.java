package com.example.backend.controller;

import java.io.IOException;
import java.sql.PreparedStatement;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.example.backend.model.merchantAndCoupon.DiscountBean;
import com.example.backend.model.merchantAndCoupon.DiscountDao;
import com.example.backend.utils.HibernateUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@WebServlet("/DeleteDiscount")
public class DeleteDiscount extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("couponId");
        
        if (idStr != null) {
            DiscountDao dao = new DiscountDao();
            dao.delete(Integer.parseInt(idStr));
            
            request.getSession().setAttribute("msgType", "success");
            request.getSession().setAttribute("msg", "刪除成功！");
        }

        // 使用 Redirect 避免重複刪除
        response.sendRedirect(request.getContextPath() + "/GetAllDiscount");
    }
}