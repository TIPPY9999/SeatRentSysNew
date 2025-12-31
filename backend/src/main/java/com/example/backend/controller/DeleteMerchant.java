package com.example.backend.controller;

import java.io.IOException;

import com.example.backend.model.merchantAndCoupon.MerchantDao;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/DeleteMerchant")
public class DeleteMerchant extends HttpServlet {
    private MerchantDao dao = new MerchantDao();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = Integer.parseInt(request.getParameter("merchantId"));
        try {
            dao.deleteMerchant(id);
            request.getSession().setAttribute("msg", "刪除成功");
        } catch (Exception e) {
            request.getSession().setAttribute("msg", "刪除失敗");
        }
        response.sendRedirect("GetAllMerchant");
    }
}
