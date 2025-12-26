package com.example.backend.dao;

// [停用原因] 架構重構：已改用 Spring Data JPA 的 Repository 介面 (RentalSpotRepository) 取代傳統
// DAO 模式。
// 此檔案不再使用，故將內容註解保留以供參考，或可直接刪除。

/*
 * import org.hibernate.query.Query;
 * import org.hibernate.Session;
 * 
 * import com.example.backend.model.RentalSpot;
 * 
 * import java.util.List;
 * 
 * public class RentalSpotDao implements IRentSpotDao {
 * 
 * private Session session;
 * 
 * public RentalSpotDao(Session session) {
 * this.session = session;
 * }
 * 
 * @Override
 * public RentalSpot insert(RentalSpot insertSpotBean) {
 * session.persist(insertSpotBean);
 * return insertSpotBean;
 * }
 * 
 * @Override
 * public RentalSpot selectById(Integer spotId) {
 * RentalSpot resultBean = session.find(RentalSpot.class, spotId);
 * return resultBean;
 * }
 * 
 * @Override
 * public List<RentalSpot> selectAll() {
 * Query<RentalSpot> query = session.createQuery("from RentalSpot",
 * RentalSpot.class);
 * return query.list();
 * }
 * 
 * @Override
 * public RentalSpot update(RentalSpot updateBean) {
 * RentalSpot resultBean = session.find(RentalSpot.class,
 * updateBean.getSpotId());
 * 
 * if (resultBean != null) {
 * session.merge(updateBean);
 * }
 * 
 * return resultBean;
 * }
 * 
 * @Override
 * public boolean deleteById(Integer spotId) {
 * 
 * RentalSpot rBean = session.find(RentalSpot.class, spotId);
 * if (rBean != null) {
 * session.remove(rBean);
 * return true;
 * }
 * return false;
 * }
 * 
 * @Override
 * public List<RentalSpot> findByCondition(String spotCode, String spotName,
 * String spotStatus, Integer merchantId) {
 * StringBuilder hql = new StringBuilder("FROM RentalSpot WHERE 1=1");
 * if (spotCode != null && !spotCode.isBlank()) {
 * hql.append(" AND spotCode LIKE :spotCode");
 * }
 * if (spotName != null && !spotName.isBlank()) {
 * hql.append(" AND spotName LIKE :spotName");
 * }
 * if (spotStatus != null && !spotStatus.isBlank()) {
 * hql.append(" AND spotStatus = :spotStatus");
 * }
 * if (merchantId != null) {
 * hql.append(" AND merchantId = :merchantId");
 * }
 * 
 * Query<RentalSpot> query = session.createQuery(hql.toString(),
 * RentalSpot.class);
 * if (spotCode != null && !spotCode.isBlank())
 * query.setParameter("spotCode", "%" + spotCode + "%");
 * if (spotName != null && !spotName.isBlank())
 * query.setParameter("spotName", "%" + spotName + "%");
 * if (spotStatus != null && !spotStatus.isBlank())
 * query.setParameter("spotStatus", spotStatus);
 * if (merchantId != null)
 * query.setParameter("merchantId", merchantId);
 * 
 * return query.list();
 * }
 * }
 */