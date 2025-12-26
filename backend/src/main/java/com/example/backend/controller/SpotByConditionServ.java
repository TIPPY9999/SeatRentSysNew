package com.example.backend.controller;

import java.io.IOException;
import java.util.List;

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

@WebServlet("/spot/condition")
public class SpotByConditionServ extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String spotCode = req.getParameter("spotCode");
        String spotName = req.getParameter("spotName");
        String spotStatus = req.getParameter("spotStatus");

        String merchantIdStr = req.getParameter("merchantId");
        Integer merchantId = (merchantIdStr == null || merchantIdStr.isBlank()) ? null : Integer.valueOf(merchantIdStr);

        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.getCurrentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            RentalSpotService rentalSpotService = new RentalSpotService(session);
            List<RentalSpot> list = rentalSpotService.findByCondition(spotCode, spotName, spotStatus, merchantId);
            req.setAttribute("spotList", list);
            tx.commit();
            req.getRequestDispatcher("/WEB-INF/view/spotResult.jsp").forward(req, res);
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw new ServletException(e);
        }
    }
}
