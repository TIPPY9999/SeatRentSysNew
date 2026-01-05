package com.example.backend.service.rec;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.backend.model.rec.RentDetails;
import com.example.backend.repository.rec.RentDetailsRepository;

@Service
public class RecDetailMgnService {

    private RentDetailsRepository rRepos;

    public List<RentDetails> getAllRec() {
        return rRepos.findAll();

    }

    public RentDetails getRecById(String memId) {
        // 找不到ID十回傳ERR MSG
        return rRepos.findById(memId).orElseThrow(
                () -> new RuntimeException("RentDetails not found with ID: " + memId));// ??

    }
    // public RentDetails getRecByMemName(String memName){
    // return rRepos.findByMemName(memName).orElseThrow(
    // () -> new RuntimeException("RentDetails not found with Name: " + memName));
    // }
}
