package com.example.backend.dao;

import java.util.List;

import com.example.backend.model.Seat;

public interface ISeatDao {

    public Seat insert(Seat insertBean);

    public Seat update(Seat updateBean);

    public Seat selectById(Integer seatId);

    public boolean deleteById(Integer seatId);

    public List<Seat> selectAll();

    public List<Seat> findByCondition(String seatsName, String seatsType, String seatsStatus, Integer spotId,
            String serialNumber);

}
