package com.example.backend.repository.rec;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.backend.model.rec.RentDetails;

@Repository
public interface RentDetailsRepository
        extends JpaRepository<RentDetails, String>, JpaSpecificationExecutor<RentDetails> {

}
