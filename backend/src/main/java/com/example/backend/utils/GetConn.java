package com.example.backend.utils;

import java.sql.Connection;
import java.sql.SQLException;

public class GetConn {
	private GetConn() {
	}

	// 全部都用DBUtil來取得連線

	public static Connection byPool() throws SQLException {

		return DBUtil.getConnection();
	}

	// 其他的寫法

	public static Connection byJDBC() throws SQLException {
		return DBUtil.getConnection();

	}
}
