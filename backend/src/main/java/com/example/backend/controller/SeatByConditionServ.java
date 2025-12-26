package com.example.backend.controller;

import java.io.IOException;
import java.util.List;

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

@WebServlet("/seat/condition")
public class SeatByConditionServ extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String seatsName = req.getParameter("seatsName");
        String seatsType = req.getParameter("seatsType");
        String seatsStatus = req.getParameter("seatsStatus");
        String serialNumber = req.getParameter("serialNumber");

        String spotIdStr = req.getParameter("spotId");
        Integer spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr);

        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.getCurrentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            SeatService seatService = new SeatService(session);
            List<Seat> seatList = seatService.findByCondition(seatsName, seatsType, seatsStatus, spotId, serialNumber);
            req.setAttribute("seatList", seatList);
            tx.commit();
            req.getRequestDispatcher("/WEB-INF/view/seatResult.jsp").forward(req, res);
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw new ServletException(e);
        }
    }
}
