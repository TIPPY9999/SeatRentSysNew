package com.example.backend.controller.spot;

import java.io.IOException;

import com.example.backend.dao.spot.SeatDao;
import com.example.backend.model.spot.SeatBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/seat/update")
public class SeatUpdateServ extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String idStr = request.getParameter("seatsId");
		Integer seatsId = null;

		System.out.println("request.getParameter(spotId): +" + request.getParameter("seatsId"));

		try {
			seatsId = (idStr == null || idStr.isBlank()) ? null : Integer.valueOf(idStr.trim());
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "seatsId 格式錯誤");
			return;
		}

		if (seatsId == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "seatsId 不可為空");
			return;
		}

		SeatBean seat = new SeatDao().findById(seatsId);

		request.setAttribute("seat", seat); // seat 可能為 null，JSP 顯示找不到
		request.getRequestDispatcher("/WEB-INF/view/spot/seatUpdate.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");

		// seatsId 防呆
		Integer seatsId = null;
		try {
			seatsId = Integer.valueOf(request.getParameter("seatsId"));
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "seatsId 格式錯誤");
			return;
		}

		SeatBean bean = new SeatBean();
		bean.setSeatsId(seatsId);
		bean.setSeatsName(request.getParameter("seatsName"));
		bean.setSeatsType(request.getParameter("seatsType"));
		bean.setSeatsStatus(request.getParameter("seatsStatus"));

		// spotId 防呆
		String spotIdStr = request.getParameter("spotId");
		Integer spotId = null;
		try {
			spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr.trim());
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "spotId 格式錯誤");
			return;
		}
		bean.setSpotId(spotId);

		String serialNumber = request.getParameter("serialNumber");
		bean.setSerialNumber((serialNumber == null || serialNumber.isBlank()) ? null : serialNumber.trim());

		// ✅ updatedAt 交給 DAO 的 SQL：updatedAt = GETDATE()
		new SeatDao().update(bean);

		response.sendRedirect(request.getContextPath() + "/seat/list");
	}
}
