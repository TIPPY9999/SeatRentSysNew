package com.example.backend.service;

import com.example.backend.model.Seat;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class SeatService implements ISeatService {

    private Session session;

    public SeatService(Session session) {
        this.session = session;
    }

    @Override
    public Seat insert(Seat seat) {
        session.persist(seat);
        return seat;
    }

    @Override
    public Seat update(Seat seat) {
        return session.merge(seat);
    }

    @Override
    public boolean deleteById(Integer seatsId) {
        Seat seat = selectById(seatsId);
        if (seat != null) {
            session.remove(seat);
            return true;
        }
        return false;
    }

    @Override
    public Seat selectById(Integer seatsId) {
        return session.get(Seat.class, seatsId);
    }

    @Override
    public List<Seat> selectAll() {
        Query<Seat> query = session.createQuery("from Seat", Seat.class);
        return query.list();
    }

    @Override
    public List<Seat> findByCondition(String seatsName, String seatsType, String seatsStatus, Integer spotId,
            String serialNumber) {
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Seat> cq = cb.createQuery(Seat.class);
        Root<Seat> root = cq.from(Seat.class);
        List<Predicate> predicates = new ArrayList<>();

        if (seatsName != null && !seatsName.isBlank()) {
            predicates.add(cb.like(root.get("seatsName"), "%" + seatsName + "%"));
        }
        if (seatsType != null && !seatsType.isBlank()) {
            predicates.add(cb.equal(root.get("seatsType"), seatsType));
        }
        if (seatsStatus != null && !seatsStatus.isBlank()) {
            predicates.add(cb.equal(root.get("seatsStatus"), seatsStatus));
        }
        if (spotId != null) {
            predicates.add(cb.equal(root.get("spotId"), spotId));
        }
        if (serialNumber != null && !serialNumber.isBlank()) {
            predicates.add(cb.equal(root.get("serialNumber"), serialNumber));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        return session.createQuery(cq).getResultList();
    }
}