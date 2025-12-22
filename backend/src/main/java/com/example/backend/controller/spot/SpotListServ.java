package com.example.backend.controller.spot;

import java.io.IOException;
import java.util.List;

import com.example.backend.dao.spot.RentalSpotDao;
import com.example.backend.model.spot.RentalSpotBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/spot/list")
public class SpotListServ extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        RentalSpotDao dao = new RentalSpotDao();
        List<RentalSpotBean> list = dao.findAll();

        request.setAttribute("spotList", list);
        request.getRequestDispatcher("/WEB-INF/view/spot/spotList.jsp")
                .forward(request, response);
    }
}
