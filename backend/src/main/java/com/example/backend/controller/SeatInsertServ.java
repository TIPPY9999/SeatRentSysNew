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

@WebServlet("/seat/insert")
public class SeatInsertServ extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/view/seatInsert.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String seatsName = req.getParameter("seatsName");
        String seatsType = req.getParameter("seatsType");
        String seatsStatus = req.getParameter("seatsStatus");
        String spotIdStr = req.getParameter("spotId");
        String serialNumber = req.getParameter("serialNumber");

        Seat sBean = new Seat();
        sBean.setSeatsName(seatsName);
        sBean.setSeatsType(seatsType);
        sBean.setSeatsStatus(seatsStatus);

        Integer spotId = null;
        try {
            spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr.trim());
        } catch (Exception e) {
            throw new ServletException("spotId 格式錯誤", e);
        }
        sBean.setSpotId(spotId);

        sBean.setSerialNumber((serialNumber == null || serialNumber.isBlank()) ? null : serialNumber.trim());

        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.getCurrentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            SeatService seatService = new SeatService(session);
            seatService.insert(sBean);
            tx.commit();
            res.sendRedirect(req.getContextPath() + "/seat/list");
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw new ServletException(e);
        }
    }
}
