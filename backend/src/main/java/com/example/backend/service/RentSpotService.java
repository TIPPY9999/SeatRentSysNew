package com.example.backend.service;

import java.util.List;
import org.hibernate.Session;
import com.example.backend.dao.RentalSpotDao;
import com.example.backend.model.RentalSpot;

public class RentSpotService implements IRentalSpotService {

    private RentalSpotDao rDao;

    public RentSpotService(Session session) {
        rDao = new RentalSpotDao(session);
    }

    @Override
    public RentalSpot insert(RentalSpot insertBean) {
        return rDao.insert(insertBean);
    }

    @Override
    public RentalSpot selectById(Integer spotId) {
        return rDao.selectById(spotId);
    }

    @Override
    public List<RentalSpot> selectAll() {
        return rDao.selectAll();
    }

    @Override
    public RentalSpot update(RentalSpot updateBean) {
        return rDao.update(updateBean);
    }

    @Override
    public boolean deleteById(Integer spotId) {
        return rDao.deleteById(spotId);
    }
}