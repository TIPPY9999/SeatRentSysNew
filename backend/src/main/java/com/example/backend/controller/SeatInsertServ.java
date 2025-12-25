package com.example.backend.controller.spot;

import java.io.IOException;

import org.hibernate.Session;
import com.example.backend.model.Seat;
import com.example.backend.utils.HibernateUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/seat/insert")
public class SeatInsertServ extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/view/spot/seatInsert.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        Seat sBean = new Seat();
        sBean.setSeatsName(request.getParameter("seatsName"));
        sBean.setSeatsType(request.getParameter("seatsType"));
        sBean.setSeatsStatus(request.getParameter("seatsStatus"));

        // spotId 防呆
        String spotIdStr = request.getParameter("spotId");
        Integer spotId = null;
        try {
            spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr.trim());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "spotId 格式錯誤");
            return;
        }
        sBean.setSpotId(spotId);

        String serialNumber = request.getParameter("serialNumber");
        sBean.setSerialNumber((serialNumber == null || serialNumber.isBlank()) ? null : serialNumber.trim());

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            session.persist(sBean);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

        response.sendRedirect(request.getContextPath() + "/seat/list");
    }
}
