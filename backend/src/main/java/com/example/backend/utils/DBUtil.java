package com.example.backend.utils;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DBUtil {
	
	private DBUtil() {}
	private static DataSource dataSource;
	
	
	static{
		try {
			InitialContext init = new InitialContext();
			dataSource = (DataSource) init.lookup("java:comp/env/jdbc/SeatRentSysDS");
			System.out.println("測試datasource是否連線成功" + dataSource);
		} catch (NamingException nameEx) {
			nameEx.printStackTrace();
			throw new ExceptionInInitializerError("資料庫連線錯誤" + nameEx.getMessage());
					
		}
	}
	
	
	
	
	public static Connection getConnection() throws SQLException {
		
		if(dataSource == null) {
			throw new IllegalStateException("連線資源錯誤");
		}
		return dataSource.getConnection();
	}
	
}
