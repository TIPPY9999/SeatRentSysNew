package com.example.backend.model;
import com.example.backend.model.MerchantBean;
import com.example.backend.utils.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class MerchantDao {

    // 新增或修改 (Save or Update)
    public void saveOrUpdate(MerchantBean m) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(m); // merge 會根據 ID 自動判斷新增或修改
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    // 刪除
    public void deleteMerchant(int id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            MerchantBean m = session.get(MerchantBean.class, id);
            if (m != null) session.remove(m);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    // 查單一
    public MerchantBean getById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(MerchantBean.class, id);
        }
    }

    // 查全部 (使用 HQL)
    public List<MerchantBean> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from MerchantBean", MerchantBean.class).list();
        }
    }

    // 關鍵字搜尋 (HQL)
    public List<MerchantBean> getByKeyword(String kw) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "from MerchantBean where merchantName like :kw or merchantAddress like :kw";
            return session.createQuery(hql, MerchantBean.class)
                          .setParameter("kw", "%" + kw + "%")
                          .list();
        }
    }
}