package com.example.backend.controller;

import java.io.IOException;
import java.math.BigDecimal;

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

@WebServlet("/spot/insert")
public class SpotInsertServ extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/view/spotInsert.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String spotCode = req.getParameter("spotCode");
        String spotName = req.getParameter("spotName");
        String spotAddress = req.getParameter("spotAddress");
        String spotStatus = req.getParameter("spotStatus");

        String merchantIdStr = req.getParameter("merchantId");
        Integer merchantId = (merchantIdStr == null || merchantIdStr.isBlank()) ? null : Integer.valueOf(merchantIdStr);

        String latStr = req.getParameter("latitude");
        BigDecimal latitude = (latStr == null || latStr.isBlank()) ? null : new BigDecimal(latStr);

        String lonStr = req.getParameter("longitude");
        BigDecimal longitude = (lonStr == null || lonStr.isBlank()) ? null : new BigDecimal(lonStr);

        RentalSpot spot = new RentalSpot();
        spot.setSpotCode(spotCode);
        spot.setSpotName(spotName);
        spot.setSpotAddress(spotAddress);
        spot.setSpotStatus(spotStatus);
        spot.setMerchantId(merchantId);
        spot.setLatitude(latitude);
        spot.setLongitude(longitude);

        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.getCurrentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            RentalSpotService rentalSpotService = new RentalSpotService(session);
            rentalSpotService.insert(spot);
            tx.commit();
            res.sendRedirect(req.getContextPath() + "/spot/list");
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw new ServletException(e);
        }
    }
}
