package com.example.backend.model.merchantAndCoupon;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import com.example.backend.utils.HibernateUtil;

public class DiscountDao {
    private SessionFactory factory;

    public DiscountDao() {
        // 直接從工具類獲取 SessionFactory
        this.factory = HibernateUtil.getSessionFactory();
    }
    public void autoUpdateStatus() {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            // 使用 Native SQL 執行資料庫函數運算
            String sql = "UPDATE discount SET couponStatus = CASE " +
                         "WHEN endDate < CAST(GETDATE() AS DATE) THEN 2 " +
                         "WHEN startDate > CAST(GETDATE() AS DATE) THEN 0 " +
                         "ELSE 1 END WHERE couponStatus != 3";
            session.createNativeQuery(sql, Object.class).executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
    // 依 ID 更新特定狀態 (用於手動下架/上架)
public void updateStatus(int couponId, String action) {
    Transaction tx = null;
    try (Session session = factory.openSession()) {
        tx = session.beginTransaction();
        DiscountBean discount = session.get(DiscountBean.class, couponId);
        
        if (discount != null) {
            if ("disable".equals(action)) {
                discount.setCouponStatus(3); // 下架
            } else if ("relist".equals(action)) {
                // 重新上架邏輯：依日期重新判定狀態
                LocalDate start = discount.getStartDate();
                LocalDate end = discount.getEndDate();
                LocalDate today = LocalDate.now();

                if (today.isBefore(start)) discount.setCouponStatus(0);      // 尚未開放
                else if (today.isAfter(end)) discount.setCouponStatus(2);    // 已結束
                else discount.setCouponStatus(1);                           // 活動中
            }
            session.merge(discount);
        }
        tx.commit();
    } catch (Exception e) {
        if (tx != null) tx.rollback();
        e.printStackTrace();
    }
}

    // 新增優惠券
    public void insert(DiscountBean discount) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            session.persist(discount); // Hibernate 6 建議使用 persist 代替 save
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    // 修改優惠券
    public void update(DiscountBean discount) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            session.merge(discount); // 使用 merge 處理游離態物件
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    // 刪除優惠券
    public void delete(int couponId) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            DiscountBean discount = session.get(DiscountBean.class, couponId);
            if (discount != null) {
                session.remove(discount);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    // 依ID查詢單筆
    public DiscountBean findById(int couponId) {
        try (Session session = factory.openSession()) {
            return session.get(DiscountBean.class, couponId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 查詢全部 (含 HQL 語法)
    public List<DiscountBean> findAll() {
        try (Session session = factory.openSession()) {
            // HQL 使用的是類別名而不是表格名
            return session.createQuery("FROM discountBean", DiscountBean.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 模糊查詢（依優惠名稱或描述）
    public List<DiscountBean> findByKeyword(String keyword) {
        String hql = "FROM discountBean WHERE couponName LIKE :kw OR couponDescription LIKE :kw";
        try (Session session = factory.openSession()) {
            return session.createQuery(hql, DiscountBean.class)
                          .setParameter("kw", "%" + keyword + "%")
                          .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 依商家ID查詢（包含商家名稱）
public List<DiscountBean> findByMerchantId(int merchantId) {
    try (Session session = factory.openSession()) {
        String sql = "SELECT d.*, m.merchantName FROM discount d " +
                     "JOIN merchant m ON d.merchantId = m.merchantId " +
                     "WHERE d.merchantId = :mId";
        
        return session.createNativeQuery(sql, DiscountBean.class)
                      .setParameter("mId", merchantId)
                      .getResultList();
    }
}
    // 整合後的查詢：含 Merchant Name 的 Join 查詢
    public List<DiscountBean> findAllWithMerchant(String keyword) {
        try (Session session = factory.openSession()) {
            String sql = "SELECT d.*, m.merchantName FROM discount d " +
                         "LEFT JOIN merchant m ON d.merchantId = m.merchantId " +
                         "WHERE d.couponName LIKE :kw OR d.couponDescription LIKE :kw";
            
            return session.createNativeQuery(sql, DiscountBean.class)
                          .setParameter("kw", "%" + keyword + "%")
                          .getResultList();
        }
    }
}

