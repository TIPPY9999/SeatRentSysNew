package com.example.backend.controller.spot;

import java.io.IOException;
import java.util.List;

import com.example.backend.dao.spot.RentalSpotDao;
import com.example.backend.model.spot.RentalSpotBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/spot/condition")
public class SpotByConditionServ extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String spotCode = request.getParameter("spotCode");
        String spotName = request.getParameter("spotName");
        String spotStatus = request.getParameter("spotStatus");
        Integer merchantId = parseInteger(request.getParameter("merchantId"));

        RentalSpotDao dao = new RentalSpotDao();
        List<RentalSpotBean> list = dao.findByCondition(spotCode, spotName, spotStatus, merchantId);

        request.setAttribute("spotList", list);
        request.getRequestDispatcher("/WEB-INF/view/spot/spotResult.jsp")
                .forward(request, response);
    }

    private Integer parseInteger(String str) {
        try {
            return (str == null || str.isBlank()) ? null : Integer.valueOf(str);
        } catch (Exception e) {
            return null;
        }
    }
}
