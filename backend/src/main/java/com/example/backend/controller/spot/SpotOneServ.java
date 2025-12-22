package com.example.backend.controller.spot;

import java.io.IOException;

import com.example.backend.dao.spot.RentalSpotDao;
import com.example.backend.model.spot.RentalSpotBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/spot/one")
public class SpotOneServ extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String spotIdStr = request.getParameter("spotId");
        Integer spotId = null;

        try {
            spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr);

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "spotId 格式錯誤");
            return;
        }

        if (spotId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少 spotId");
            return;
        }

        RentalSpotDao dao = new RentalSpotDao();

        System.out.print("dao.findById(spotId)");// DEBUG
        RentalSpotBean spot = dao.findById(spotId);

        if (spot == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "找不到資料");
            return;
        }

        request.setAttribute("spot", spot);

        request.getRequestDispatcher("/WEB-INF/view/spot/spotOne.jsp")
                .forward(request, response);
    }
}
