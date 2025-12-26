package com.example.backend.controller;

import java.io.IOException;
import java.util.List;

import com.example.backend.service.RentalSpotService;
import com.example.backend.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import com.example.backend.model.RentalSpot;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/spot/list")
public class SpotListServ extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.getCurrentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            RentalSpotService rentalSpotService = new RentalSpotService(session);
            List<RentalSpot> list = rentalSpotService.selectAll();
            req.setAttribute("spotList", list);
            tx.commit();
            req.getRequestDispatcher("/WEB-INF/view/spotList.jsp").forward(req, res);
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw new ServletException(e);
        }
    }
}
