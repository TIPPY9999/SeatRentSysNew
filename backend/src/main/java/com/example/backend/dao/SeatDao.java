package com.example.backend.dao;

import org.hibernate.query.Query;
import org.hibernate.Session;

import com.example.backend.model.Seat;

import java.util.List;

public class SeatDao implements ISeatDao {

	private Session session;

	public SeatDao(Session session) {
		this.session = session;
	}

	@Override
	public Seat insert(Seat insertSeatBean) {
		session.persist(insertSeatBean);
		return insertSeatBean;
	}

	@Override
	public Seat selectById(Integer seatId) {
		Seat resultBean = session.find(Seat.class, seatId);
		return resultBean;
	}

	@Override
	public List<Seat> selectAll() {
		Query<Seat> query = session.createQuery("from Seat", Seat.class);
		return query.list();
	}

	@Override
	public Seat update(Seat updateBean) {
		Seat resultBean = session.find(Seat.class, updateBean.getSeatsId());

		if (resultBean != null) {
			session.merge(updateBean);
		}

		return resultBean;
	}

	@Override
	public boolean deleteById(Integer seatId) {

		Seat sBean = session.find(Seat.class, seatId);
		if (sBean != null) {
			session.remove(sBean);
			return true;
		}
		return false;
	}
}