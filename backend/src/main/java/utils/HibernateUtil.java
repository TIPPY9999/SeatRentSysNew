package utils;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateUtil {

	private static final SessionFactory factory = createSessionFactory();

//	private static SessionFactory createSessionFactory() {
//		StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().configure().build();
//		SessionFactory sessionFactory = new MetadataSources(serviceRegistry).buildMetadata().buildSessionFactory();
//		return sessionFactory;
//	}

	private static SessionFactory createSessionFactory() {

		StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().configure().build();

		try {
			return new MetadataSources(serviceRegistry).buildMetadata().buildSessionFactory();

		} catch (Exception e) {

			// 印出完整原因(含caused by)
			e.printStackTrace();
			// 重要：建置失敗要釋放 registry，避免資源殘留（尤其在 Tomcat）
			StandardServiceRegistryBuilder.destroy(serviceRegistry);

			// 讓錯誤在「類別初始化」就清楚爆出來
			throw new ExceptionInInitializerError(e);
		}
	}

	public static SessionFactory getSessionFactory() {
		return factory;
	}

	public static void closeSessionFactory() {
		if (factory != null) {
			factory.close();
		}
	}
}
