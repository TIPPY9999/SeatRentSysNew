package com.example.backend.controller.spot;

import java.io.IOException;

import com.example.backend.dao.spot.SeatDao;
import com.example.backend.model.spot.SeatBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

@WebServlet("/seat/one")
public class SeatOneServ extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("seatsId");
        Integer seatsId;

        try {
            seatsId = (idStr == null || idStr.isBlank()) ? null : Integer.valueOf(idStr.trim());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "seatsId 格式錯誤");
            return;
        }

        if (seatsId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "seatsId 不可為空");
            return;
        }

        SeatBean seat = new SeatDao().findById(seatsId);
        request.setAttribute("seat", seat);
        request.getRequestDispatcher("/WEB-INF/view/spot/seatOne.jsp").forward(request, response);
    }
}
