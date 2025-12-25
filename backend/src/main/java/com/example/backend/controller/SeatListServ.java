package com.example.backend.controller.spot;

import java.io.IOException;
import java.util.List;

import com.example.backend.dao.spot.SeatDao;
import com.example.backend.model.spot.SeatBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/seat/list")
public class SeatListServ extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        SeatDao dao = new SeatDao();
        List<SeatBean> seatList = dao.findAll();

        request.setAttribute("seatList", seatList);
        request.getRequestDispatcher("/WEB-INF/view/spot/seatList.jsp").forward(request, response);
    }
}
