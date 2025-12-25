package com.example.backend.controller.spot;

import java.io.IOException;

import com.example.backend.dao.spot.RentalSpotDao;
import com.example.backend.model.spot.RentalSpotBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/spot/update")
public class SpotUpdateServ extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 先查出資料丟到表單
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String spotIdStr = request.getParameter("spotId");
        Integer spotId = parseInteger(spotIdStr);

        if (spotId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少 spotId");
            return;
        }

        RentalSpotDao dao = new RentalSpotDao();
        RentalSpotBean spot = dao.findById(spotId);

        if (spot == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "查無此租借點");
            return;
        }

        request.setAttribute("spot", spot);
        request.getRequestDispatcher("/WEB-INF/view/spot/spotUpdate.jsp")
                .forward(request, response);
    }

    // 接收表單做更新
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        Integer spotId = parseInteger(request.getParameter("spotId"));
        String spotCode = request.getParameter("spotCode");
        String spotName = request.getParameter("spotName");
        String spotAddress = request.getParameter("spotAddress");
        String spotStatus = request.getParameter("spotStatus");
        Integer merchantId = parseInteger(request.getParameter("merchantId"));
        Double latitude = parseDouble(request.getParameter("latitude"));
        Double longitude = parseDouble(request.getParameter("longitude"));

        RentalSpotBean spot = new RentalSpotBean();
        spot.setSpotId(spotId);
        spot.setSpotCode(spotCode);
        spot.setSpotName(spotName);
        spot.setSpotAddress(spotAddress);
        spot.setSpotStatus(spotStatus);
        spot.setMerchantId(merchantId);
        spot.setLatitude(latitude);
        spot.setLongitude(longitude);

        RentalSpotDao dao = new RentalSpotDao();
        dao.update(spot);

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
