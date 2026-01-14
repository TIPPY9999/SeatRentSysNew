package com.example.backend.repository.spot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.backend.model.spot.Seat;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer>, JpaSpecificationExecutor<Seat> {
    // JpaRepository 提供基本的 CRUD (save, findAll, findById, deleteById)
    // JpaSpecificationExecutor 提供動態查詢 (findAll(Specification))

    public List<Seat> findBySpotId(Integer spotId);
}