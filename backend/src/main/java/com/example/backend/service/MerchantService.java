package com.example.backend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.model.DiscountDao;
import com.example.backend.model.MerchantBean;
import com.example.backend.model.MerchantDao;
import com.example.backend.model.MerchantRepository;

import java.util.List;

@Service
@Transactional // 代替手動 transaction.begin/commit
public class MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;

    public void saveOrUpdate(MerchantBean m) {
        // save() 方法會自動判斷 ID：
        // 如果 ID 是 null/0 則 INSERT；如果 ID 已存在則 UPDATE (類似 merge)
        merchantRepository.save(m); 
    }

    public void deleteMerchant(int id) {
        merchantRepository.deleteById(id);
    }

    public MerchantBean getById(int id) {
        // findById 回傳的是 Optional，可以避免空指標
        return merchantRepository.findById(id).orElse(null);
    }

    public List<MerchantBean> getAll() {
        return merchantRepository.findAll();
    }

    public List<MerchantBean> getByKeyword(String kw) {
        return merchantRepository.findByKeyword(kw);
    }
}