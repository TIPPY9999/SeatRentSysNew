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

@WebServlet("/seat/update")
public class SeatUpdateServ extends HttpServlet {

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
			req.getRequestDispatcher("/WEB-INF/view/seatUpdate.jsp").forward(req, res);
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			throw new ServletException(e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String seatsIdStr = req.getParameter("seatsId");
		String seatsName = req.getParameter("seatsName");
		String seatsType = req.getParameter("seatsType");
		String seatsStatus = req.getParameter("seatsStatus");
		String spotIdStr = req.getParameter("spotId");
		String serialNumber = req.getParameter("serialNumber");

		Integer spotId = null;
		try {
			spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr.trim());
		} catch (Exception e) {
			throw new ServletException("spotId 格式錯誤", e);
		}

		SessionFactory factory = HibernateUtil.getSessionFactory();
		Session session = factory.getCurrentSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			SeatService seatService = new SeatService(session);

			if (seatsIdStr != null && !seatsIdStr.isBlank()) {
				Seat seat = seatService.selectById(Integer.valueOf(seatsIdStr));
				if (seat != null) {
					seat.setSeatsName(seatsName);
					seat.setSeatsType(seatsType);
					seat.setSeatsStatus(seatsStatus);
					seat.setSpotId(spotId);
					seat.setSerialNumber((serialNumber == null || serialNumber.isBlank()) ? null : serialNumber.trim());

					seatService.update(seat);
				}
			}
			tx.commit();
			res.sendRedirect(req.getContextPath() + "/seat/list");
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			throw new ServletException(e);
		}
	}
}
