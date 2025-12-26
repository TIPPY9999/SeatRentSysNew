package com.example.backend.service;

import com.example.backend.model.RentalSpot;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class RentalSpotService implements IRentalSpotService {

    private Session session;

    public RentalSpotService(Session session) {
        this.session = session;
    }

    @Override
    public List<RentalSpot> selectAll() {
        Query<RentalSpot> query = session.createQuery("from RentalSpot", RentalSpot.class);
        return query.list();
    }

    @Override
    public RentalSpot selectById(Integer spotId) {
        return session.get(RentalSpot.class, spotId);
    }

    @Override
    public RentalSpot insert(RentalSpot spot) {
        session.persist(spot);
        return spot;
    }

    @Override
    public RentalSpot update(RentalSpot spot) {
        return session.merge(spot);
    }

    @Override
    public boolean deleteById(Integer spotId) {
        RentalSpot spot = selectById(spotId);
        if (spot != null) {
            session.remove(spot);
            return true;
        }
        return false;
    }

    @Override
    public List<RentalSpot> findByCondition(String spotCode, String spotName, String spotStatus, Integer merchantId) {
        CriteriaBuilder cb = session.getCriteriaBuilder();
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
        return session.createQuery(cq).getResultList();
    }
}