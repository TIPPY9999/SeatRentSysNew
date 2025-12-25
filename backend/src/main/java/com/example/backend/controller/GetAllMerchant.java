package com.example.backend.controller;

import java.io.IOException;
import java.util.List;

import com.example.backend.model.MerchantBean;
import com.example.backend.model.MerchantDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/GetAllMerchant")
public class GetAllMerchant extends HttpServlet {
    private MerchantDao dao = new MerchantDao();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. 使用 Hibernate DAO 取得所有資料
        List<MerchantBean> merchants = dao.getAll();

        // 2. 傳遞給 JSP
        request.setAttribute("merchants", merchants);
        
        // 處理 Session 中的訊息 (如果有新增/修改/刪除的提示)
        String msg = (String) request.getSession().getAttribute("msg");
        if (msg != null) {
            request.setAttribute("msg", msg);
            request.getSession().removeAttribute("msg"); // 顯示完後移除
        }

        request.getRequestDispatcher("/WEB-INF/view/merchantandcoupon/GetAllMerchant.jsp")
               .forward(request, response);
    }
}
 