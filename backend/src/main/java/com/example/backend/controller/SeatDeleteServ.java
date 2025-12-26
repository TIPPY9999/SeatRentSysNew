package com.example.backend.controller;

import java.io.IOException;

import com.example.backend.service.SeatService;
import com.example.backend.utils.HibernateUtil;
import org.hibernate.Session;

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

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            new SeatService(session).deleteById(seatsId);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        response.sendRedirect(request.getContextPath() + "/seat/list");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/seat/list");
    }
}
