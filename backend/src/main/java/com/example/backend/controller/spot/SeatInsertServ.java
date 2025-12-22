package com.example.backend.controller.spot;

import java.io.IOException;

import com.example.backend.dao.spot.SeatDao;
import com.example.backend.model.spot.SeatBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/seat/insert")
public class SeatInsertServ extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/view/spot/seatInsert.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        SeatBean bean = new SeatBean();
        bean.setSeatsName(request.getParameter("seatsName"));
        bean.setSeatsType(request.getParameter("seatsType"));
        bean.setSeatsStatus(request.getParameter("seatsStatus"));

        // spotId 防呆
        String spotIdStr = request.getParameter("spotId");
        Integer spotId = null;
        try {
            spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr.trim());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "spotId 格式錯誤");
            return;
        }
        bean.setSpotId(spotId);

        String serialNumber = request.getParameter("serialNumber");
        bean.setSerialNumber((serialNumber == null || serialNumber.isBlank()) ? null : serialNumber.trim());

        // ✅ createdAt / updatedAt 交給 DAO 的 GETDATE()，Servlet 不用塞
        new SeatDao().insert(bean);

        response.sendRedirect(request.getContextPath() + "/seat/list");
    }
}
