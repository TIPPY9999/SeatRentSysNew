package com.example.backend.controller.spot;

import java.io.IOException;

import com.example.backend.dao.spot.RentalSpotDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/spot/delete")
public class SpotDeleteServ extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String spotIdStr = request.getParameter("spotId");
        Integer spotId = null;
        try {
            spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr);
        } catch (Exception e) {
            // 忽略，當作沒傳
        }

        if (spotId != null) {
            RentalSpotDao dao = new RentalSpotDao();
            dao.delete(spotId);
        }

        System.out.println("request.getParameter(spotId): ");

        response.sendRedirect(request.getContextPath() + "/spot/list");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 如果有人用 GET 刪除，就導回列表
        response.sendRedirect(request.getContextPath() + "/spot/list");
    }
}
