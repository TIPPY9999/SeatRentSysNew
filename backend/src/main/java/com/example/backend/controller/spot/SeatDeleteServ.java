package com.example.backend.controller.spot;

import java.io.IOException;

import com.example.backend.dao.spot.SeatDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/seat/delete")
public class SeatDeleteServ extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer seatsId;
        try {
            seatsId = Integer.valueOf(request.getParameter("seatsId"));
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "seatsId 格式錯誤");
            return;
        }

        new SeatDao().delete(seatsId);
        response.sendRedirect(request.getContextPath() + "/seat/list");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/seat/list");
    }
}
