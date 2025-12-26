package com.example.backend.controller;

import java.io.IOException;
import com.example.backend.service.RentalSpotService;
import com.example.backend.model.RentalSpot;
import com.example.backend.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/spot/one")
public class SpotOneServ extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String spotIdStr = req.getParameter("spotId");
        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.getCurrentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            if (spotIdStr != null && !spotIdStr.isBlank()) {
                RentalSpotService rentalSpotService = new RentalSpotService(session);
                RentalSpot spot = rentalSpotService.selectById(Integer.valueOf(spotIdStr));
                req.setAttribute("spot", spot);
            }
            tx.commit();
            req.getRequestDispatcher("/WEB-INF/view/spotOne.jsp").forward(req, res);
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw new ServletException(e);
        }
    }
}
