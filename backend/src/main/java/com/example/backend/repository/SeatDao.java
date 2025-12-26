package com.example.backend.dao;

// [停用原因] 架構重構：已改用 Spring Data JPA 的 Repository 介面 (SeatRepository) 取代傳統 DAO 模式。
// 此檔案不再使用，故將內容註解保留以供參考，或可直接刪除。

/*
 * import org.hibernate.query.Query;
 * import org.hibernate.Session;
 * 
 * import com.example.backend.model.Seat;
 * 
 * import java.util.List;
 * 
 * public class SeatDao implements ISeatDao {
 * 
 * private Session session;
 * 
 * public SeatDao(Session session) {
 * this.session = session;
 * }
 * 
 * @Override
 * public Seat insert(Seat insertSeatBean) {
 * session.persist(insertSeatBean);
 * return insertSeatBean;
 * }
 * 
 * @Override
 * public Seat selectById(Integer seatId) {
 * Seat resultBean = session.find(Seat.class, seatId);
 * return resultBean;
 * }
 * 
 * @Override
 * public List<Seat> selectAll() {
 * Query<Seat> query = session.createQuery("from Seat", Seat.class);
 * return query.list();
 * }
 * 
 * @Override
 * public Seat update(Seat updateBean) {
 * Seat resultBean = session.find(Seat.class, updateBean.getSeatsId());
 * 
 * if (resultBean != null) {
 * session.merge(updateBean);
 * }
 * 
 * return resultBean;
 * }
 * 
 * @Override
 * public boolean deleteById(Integer seatId) {
 * 
 * Seat sBean = session.find(Seat.class, seatId);
 * if (sBean != null) {
 * session.remove(sBean);
 * return true;
 * }
 * return false;
 * }
 * 
 * @Override
 * public List<Seat> findByCondition(String seatsName, String seatsType, String
 * seatsStatus, Integer spotId,
 * String serialNumber) {
 * StringBuilder hql = new StringBuilder("FROM Seat WHERE 1=1");
 * if (seatsName != null && !seatsName.isBlank())
 * hql.append(" AND seatsName LIKE :seatsName");
 * if (seatsType != null && !seatsType.isBlank())
 * hql.append(" AND seatsType = :seatsType");
 * if (seatsStatus != null && !seatsStatus.isBlank())
 * hql.append(" AND seatsStatus = :seatsStatus");
 * if (spotId != null)
 * hql.append(" AND spotId = :spotId");
 * if (serialNumber != null && !serialNumber.isBlank())
 * hql.append(" AND serialNumber LIKE :serialNumber");
 * 
 * Query<Seat> query = session.createQuery(hql.toString(), Seat.class);
 * 
 * if (seatsName != null && !seatsName.isBlank())
 * query.setParameter("seatsName", "%" + seatsName + "%");
 * if (seatsType != null && !seatsType.isBlank())
 * query.setParameter("seatsType", seatsType);
 * if (seatsStatus != null && !seatsStatus.isBlank())
 * query.setParameter("seatsStatus", seatsStatus);
 * if (spotId != null)
 * query.setParameter("spotId", spotId);
 * if (serialNumber != null && !serialNumber.isBlank())
 * query.setParameter("serialNumber", "%" + serialNumber + "%");
 * 
 * return query.list();
 * }
 * }
 */