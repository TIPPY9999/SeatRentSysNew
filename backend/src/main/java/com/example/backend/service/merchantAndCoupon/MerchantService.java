package com.example.backend.service.merchantAndCoupon;

import com.example.backend.model.merchantAndCoupon.MerchantBean;
import com.example.backend.repository.merchantAndCoupon.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;

    public void saveOrUpdate(MerchantBean m) {
        merchantRepository.save(m); 
    }

    public void deleteMerchant(int id) {
        merchantRepository.deleteById(id);
    }

    public MerchantBean getById(int id) {
        return merchantRepository.findById(id).orElse(null);
    }

    public List<MerchantBean> getAll() {
        return merchantRepository.findAll();
    }

    public List<MerchantBean> getByKeyword(String kw) {
        // [修正] 更嚴謹的判斷，如果 kw 是 null 或空字串，直接回傳全部，防止報錯
        if (kw == null || kw.trim().isEmpty()) {
            return merchantRepository.findAll();
        }
        return merchantRepository.findByKeyword(kw.trim());
    }
}