package com.example.backend.controller;

import java.io.IOException;

import com.example.backend.model.Seat;
import com.example.backend.service.SeatService;
import com.example.backend.utils.HibernateUtil;
import org.hibernate.Session;

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

        Seat seat = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            seat = new SeatService(session).selectById(seatsId);
        } finally {
            session.close();
        }
        request.setAttribute("seat", seat);
        request.getRequestDispatcher("/WEB-INF/view/seatOne.jsp").forward(request, response);
    }
}
