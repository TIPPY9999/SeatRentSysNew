package com.example.backend.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import org.hibernate.Session;

import com.example.backend.model.DiscountBean;
import com.example.backend.model.DiscountDao;
import com.example.backend.model.MerchantBean;
import com.example.backend.utils.HibernateUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/InsertDiscount")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 10)
public class InsertDiscount extends HttpServlet {
    private final String uploadPath = "D:/SeatRentSys/images";

    // GET: 載入商家下拉選單 (這裡假設你有一個 MerchantDao)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 這裡建議你也建立一個 MerchantDao.findAll()，或是暫用 HibernateUtil 查詢
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                List<MerchantBean> merchants = session.createQuery("FROM merchantBean", MerchantBean.class).list();
                request.setAttribute("merchants", merchants);
            }
            request.getRequestDispatcher("/WEB-INF/view/merchantandcoupon/AddDiscount.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("載入選單失敗", e);
        }
    }

    // POST: 執行新增
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            // 1. 處理圖片上傳
            Part filePart = request.getPart("couponImg");
            String fileName = null;
            if (filePart != null && filePart.getSize() > 0) {
                fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();
                filePart.write(uploadPath + File.separator + fileName);
            }

            // 2. 封裝 Bean
            DiscountBean d = new DiscountBean();
            d.setCouponName(request.getParameter("couponName"));
            d.setCouponDescription(request.getParameter("couponDescription"));
            d.setPointsRequired(Integer.parseInt(request.getParameter("pointsRequired")));
            d.setStartDate(LocalDate.parse(request.getParameter("startDate")));
            d.setEndDate(LocalDate.parse(request.getParameter("endDate")));
            d.setMerchantId(Integer.parseInt(request.getParameter("merchantId")));
            d.setCouponStatus(Integer.parseInt(request.getParameter("couponStatus")));
            d.setCouponImg(fileName);

            // 3. 調用 DAO 存檔
            DiscountDao dao = new DiscountDao();
            dao.insert(d);

            request.getSession().setAttribute("msg", "新增成功！");
            response.sendRedirect(request.getContextPath() + "/GetAllDiscount");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/InsertDiscount");
        }
    }
}