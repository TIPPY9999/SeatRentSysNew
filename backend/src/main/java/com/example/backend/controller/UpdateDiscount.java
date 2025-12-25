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

@WebServlet("/UpdateDiscount")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 10)
public class UpdateDiscount extends HttpServlet {
    private final String uploadPath = "D:/SeatRentSys/images";

    // GET: 讀取資料並轉向編輯頁面
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            int couponId = Integer.parseInt(request.getParameter("couponId"));
            
            // 1. 抓取優惠券
            DiscountBean discount = session.get(DiscountBean.class, couponId);
            // 2. 抓取商家清單
            List<MerchantBean> merchants = session.createQuery("FROM merchantBean", MerchantBean.class).list();

            request.setAttribute("discount", discount);
            request.setAttribute("merchants", merchants);
            request.getRequestDispatcher("/WEB-INF/view/merchantandcoupon/UpdateDiscount.jsp")
                   .forward(request, response);
        } catch (Exception e) {
            response.sendRedirect("GetAllDiscount");
        }
    }

    // POST: 執行更新動作
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            // 1. 處理圖片 (若沒傳新圖，沿用舊圖名稱)
            Part filePart = request.getPart("couponImg");
            String dbPath = request.getParameter("oldCouponImg"); 
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                filePart.write(uploadPath + File.separator + fileName);
                dbPath = fileName;
            }

            // 2. 封裝 Bean
            DiscountBean d = new DiscountBean();
            d.setCouponId(Integer.parseInt(request.getParameter("couponId")));
            d.setCouponName(request.getParameter("couponName"));
            d.setCouponDescription(request.getParameter("couponDescription"));
            d.setPointsRequired(Integer.parseInt(request.getParameter("pointsRequired")));
            d.setStartDate(LocalDate.parse(request.getParameter("startDate")));
            d.setEndDate(LocalDate.parse(request.getParameter("endDate")));
            d.setMerchantId(Integer.parseInt(request.getParameter("merchantId")));
            d.setCouponStatus(Integer.parseInt(request.getParameter("couponStatus")));
            d.setCouponImg(dbPath);

            // 3. 執行更新
            new DiscountDao().update(d);

            request.getSession().setAttribute("msg", "修改成功！");
            response.sendRedirect(request.getContextPath() + "/GetAllDiscount");
        } catch (Exception e) {
            request.getSession().setAttribute("msg", "修改失敗");
            response.sendRedirect("GetAllDiscount");
        }
    }
}
