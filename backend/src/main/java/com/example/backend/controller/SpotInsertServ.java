package com.example.backend.controller.spot;

import java.io.IOException;

import com.example.backend.dao.spot.RentalSpotDao;
import com.example.backend.model.spot.RentalSpotBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/spot/insert")
public class SpotInsertServ extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 顯示新增表單
        request.getRequestDispatcher("/WEB-INF/view/spot/spotInsert.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String spotCode = request.getParameter("spotCode");
        String spotName = request.getParameter("spotName");
        String spotAddress = request.getParameter("spotAddress");
        String spotStatus = request.getParameter("spotStatus");
        Integer merchantId = parseInteger(request.getParameter("merchantId"));
        Double latitude = parseDouble(request.getParameter("latitude"));
        Double longitude = parseDouble(request.getParameter("longitude"));

        RentalSpot spot = new RentalSpot();
        spot.setSpotCode(spotCode);
        spot.setSpotName(spotName);
        spot.setSpotAddress(spotAddress);
        spot.setSpotStatus(spotStatus);
        spot.setMerchantId(merchantId);
        spot.setLatitude(latitude);
        spot.setLongitude(longitude);

        RentalSpotDao dao = new RentalSpotDao();
        dao.insert(spot);

        response.sendRedirect(request.getContextPath() + "/spot/list");
    }

    private Integer parseInteger(String str) {
        try {
            return (str == null || str.isBlank()) ? null : Integer.valueOf(str);
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDouble(String str) {
        try {
            return (str == null || str.isBlank()) ? null : Double.valueOf(str);
        } catch (Exception e) {
            return null;
        }
    }
}
