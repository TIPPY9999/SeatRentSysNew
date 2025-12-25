package com.example.backend.dao;

import org.hibernate.query.Query;
import org.hibernate.Session;

import com.example.backend.model.RentalSpot;

import java.util.List;

public class RentalSpotDao implements IRentSpotDao {

	private Session session;

	public RentalSpotDao(Session session) {
		this.session = session;
	}

	@Override
	public RentalSpot insert(RentalSpot insertSpotBean) {
		session.persist(insertSpotBean);
		return insertSpotBean;
	}

	@Override
	public RentalSpot selectById(Integer spotId) {
		RentalSpot resultBean = session.find(RentalSpot.class, spotId);
		return resultBean;
	}

	@Override
	public List<RentalSpot> selectAll() {
		Query<RentalSpot> query = session.createQuery("from RentalSpot", RentalSpot.class);
		return query.list();
	}

	@Override
	public RentalSpot update(RentalSpot updateBean) {
		RentalSpot resultBean = session.find(RentalSpot.class,
				updateBean.getSpotId());

		if (resultBean != null) {
			session.merge(updateBean);
		}

		return resultBean;
	}

	@Override
	public boolean deleteById(Integer spotId) {

		RentalSpot rBean = session.find(RentalSpot.class, spotId);
		if (rBean != null) {
			session.remove(rBean);
			return true;
		}
		return false;
	}
}