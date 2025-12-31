package com.example.backend.controller;

import java.io.IOException;
import java.util.List;

import com.example.backend.model.merchantAndCoupon.MerchantBean;
import com.example.backend.model.merchantAndCoupon.MerchantDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/GetMerchantByKeyword")
public class GetMerchantByKeyword extends HttpServlet {
    private MerchantDao dao = new MerchantDao();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String keyword = request.getParameter("keyword");
        
        // 呼叫 DAO 的 search 方法 (內部使用 HQL: FROM MerchantBean WHERE ...)
        List<MerchantBean> results = dao.getByKeyword(keyword);
        
        request.setAttribute("merchants", results);
        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/WEB-INF/view/merchantandcoupon/SearchMerchantByKeyword.jsp")
               .forward(request, response);
    }
}