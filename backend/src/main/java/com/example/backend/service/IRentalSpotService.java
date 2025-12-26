package com.example.backend.service;

import java.util.List;

import com.example.backend.model.RentalSpot;

public interface IRentalSpotService {

    public RentalSpot insert(RentalSpot insertBean);

    public RentalSpot update(RentalSpot updateBean);

    public RentalSpot selectById(Integer spotId);

    public boolean deleteById(Integer spotId);

    public List<RentalSpot> selectAll();

    public List<RentalSpot> findByCondition(String spotCode, String spotName, String spotStatus, Integer merchantId);

}
