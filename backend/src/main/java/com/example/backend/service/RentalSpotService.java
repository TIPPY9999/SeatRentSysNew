package com.example.backend.service;

import com.example.backend.model.RentalSpot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RentalSpotService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<RentalSpot> selectAll() {
        return entityManager.createQuery("from RentalSpot", RentalSpot.class).getResultList();
    }

    @Transactional(readOnly = true)
    public RentalSpot selectById(Integer spotId) {
        return entityManager.find(RentalSpot.class, spotId);
    }

    @Transactional
    public RentalSpot insert(RentalSpot spot) {
        entityManager.persist(spot);
        return spot;
    }

    @Transactional
    public RentalSpot update(RentalSpot spot) {
        return entityManager.merge(spot);
    }

    @Transactional
    public void deleteById(Integer spotId) {
        RentalSpot spot = selectById(spotId);
        if (spot != null) {
            entityManager.remove(spot);
        }
    }
    // findByCondition 方法可以後續再加入

    @Transactional(readOnly = true)
    public List<RentalSpot> findByCondition(String spotCode, String spotName, String spotStatus, Integer merchantId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RentalSpot> cq = cb.createQuery(RentalSpot.class);
        Root<RentalSpot> root = cq.from(RentalSpot.class);
        List<Predicate> predicates = new ArrayList<>();

        if (spotCode != null && !spotCode.isBlank()) {
            predicates.add(cb.like(root.get("spotCode"), "%" + spotCode + "%"));
        }
        if (spotName != null && !spotName.isBlank()) {
            predicates.add(cb.like(root.get("spotName"), "%" + spotName + "%"));
        }
        if (spotStatus != null && !spotStatus.isBlank()) {
            predicates.add(cb.equal(root.get("spotStatus"), spotStatus));
        }
        if (merchantId != null) {
            predicates.add(cb.equal(root.get("merchantId"), merchantId));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(cq).getResultList();
    }
}