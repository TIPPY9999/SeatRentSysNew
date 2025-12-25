package com.example.backend.controller.spot;

import java.io.IOException;
import java.util.List;

import com.example.backend.dao.SeatDao;
import com.example.backend.model.spot.SeatBean;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/seat/condition")
public class SeatByConditionServ extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String seatsName = request.getParameter("seatsName");
        String seatsType = request.getParameter("seatsType");
        String seatsStatus = request.getParameter("seatsStatus");
        String serialNumber = request.getParameter("serialNumber");

        String spotIdStr = request.getParameter("spotId");
        Integer spotId = null;
        try {
            spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr.trim());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "spotId 格式錯誤");
            return;
        }

        List<SeatBean> seatList = new SeatDao().findByCondition(
                seatsName, seatsType, seatsStatus, spotId, serialNumber);

        request.setAttribute("seatList", seatList);
        request.getRequestDispatcher("/WEB-INF/view/spot/seatResult.jsp").forward(request, response);
    }
}
