package com.example.backend.dao;

import java.util.List;

import com.example.backend.model.RentalSpot;

public interface IRentSpotDao {

    public RentalSpot insert(RentalSpot insertBean);

    public RentalSpot update(RentalSpot updateBean);

    public RentalSpot selectById(Integer spotId);

    public boolean deleteById(Integer spotId);

    public List<RentalSpot> selectAll();

}
