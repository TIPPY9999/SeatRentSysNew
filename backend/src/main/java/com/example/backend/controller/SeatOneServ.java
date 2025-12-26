package com.example.backend.controller;

import java.io.IOException;
import com.example.backend.model.Seat;
import com.example.backend.service.SeatService;
import com.example.backend.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/seat/one")
public class SeatOneServ extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String seatsIdStr = req.getParameter("seatsId");
        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.getCurrentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            if (seatsIdStr != null && !seatsIdStr.isBlank()) {
                SeatService seatService = new SeatService(session);
                Seat seat = seatService.selectById(Integer.valueOf(seatsIdStr));
                req.setAttribute("seat", seat);
            }
            tx.commit();
            req.getRequestDispatcher("/WEB-INF/view/seatOne.jsp").forward(req, res);
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw new ServletException(e);
        }
    }
}
