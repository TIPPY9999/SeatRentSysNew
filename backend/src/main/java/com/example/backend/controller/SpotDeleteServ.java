package com.example.backend.controller;

import java.io.IOException;
import com.example.backend.service.RentalSpotService;
import com.example.backend.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/spot/delete")
public class SpotDeleteServ extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String spotIdStr = req.getParameter("spotId");
        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.getCurrentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            RentalSpotService rentalSpotService = new RentalSpotService(session);
            if (spotIdStr != null && !spotIdStr.isBlank()) {
                rentalSpotService.deleteById(Integer.valueOf(spotIdStr));
            }
            tx.commit();
            res.sendRedirect(req.getContextPath() + "/spot/list");
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.sendRedirect(req.getContextPath() + "/spot/list");
    }
}
