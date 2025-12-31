package com.example.backend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.model.merchantAndCoupon.DiscountBean;
import com.example.backend.model.merchantAndCoupon.DiscountDao;
import com.example.backend.model.merchantAndCoupon.DiscountRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class DiscountService {

    @Autowired
    private DiscountRepository discountRepository;

    public List<DiscountBean> getAll(String keyword) {
        // 每次查詢前跑一次狀態檢查
        discountRepository.autoUpdateStatus();
        
        if (keyword != null && !keyword.isEmpty()) {
            return discountRepository.findByKeyword(keyword);
        }
        return discountRepository.findAll();
    }

    public DiscountBean getById(Integer id) {
        return discountRepository.findById(id).orElse(null);
    }

    public void save(DiscountBean discount) {
        discountRepository.save(discount);
    }

    public void delete(Integer id) {
        discountRepository.deleteById(id);
    }

    public void updateSingleStatus(Integer id, String action) {
        DiscountBean d = getById(id);
        if (d != null) {
            // 1:正常, 3:手動下架
            d.setCouponStatus("disable".equals(action) ? 3 : 1);
            discountRepository.save(d);
        }
    }
}