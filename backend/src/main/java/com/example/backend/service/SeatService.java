package com.example.backend.service;

import com.example.backend.model.Seat;
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
public class SeatService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Seat insert(Seat seat) {
        entityManager.persist(seat);
        return seat;
    }

    @Transactional
    public Seat update(Seat seat) {
        // 使用 merge 來處理更新，如果實體存在則更新，不存在則新增
        return entityManager.merge(seat);
    }

    @Transactional
    public void deleteById(Integer seatsId) {
        Seat seat = selectById(seatsId);
        if (seat != null) {
            entityManager.remove(seat);
        }
    }

    @Transactional(readOnly = true)
    public Seat selectById(Integer seatsId) {
        return entityManager.find(Seat.class, seatsId);
    }

    @Transactional(readOnly = true)
    public List<Seat> selectAll() {
        return entityManager.createQuery("from Seat", Seat.class).getResultList();
    }

    @Transactional(readOnly = true)
    public List<Seat> findByCondition(String seatsName, String seatsType, String seatsStatus, Integer spotId,
            String serialNumber) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
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
        return entityManager.createQuery(cq).getResultList();
    }
}