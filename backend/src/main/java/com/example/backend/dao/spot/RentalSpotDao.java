package com.example.backend.dao.spot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.backend.model.spot.RentalSpotBean;
import com.example.backend.utils.GetConn;

public class RentalSpotDao {

	private RentalSpotBean mapBean(ResultSet resultSet) throws SQLException {
		RentalSpotBean bean = new RentalSpotBean();

		System.out.println("request.getParameter(spotId): ");

		bean.setSpotId(resultSet.getInt("spotId"));
		bean.setSpotCode(resultSet.getString("spotCode"));
		bean.setSpotName(resultSet.getString("spotName"));
		bean.setSpotAddress(resultSet.getString("spotAddress"));
		bean.setSpotStatus(resultSet.getString("spotStatus"));
		bean.setMerchantId(resultSet.getInt("merchantId"));
		bean.setCreatedAt(resultSet.getTimestamp("createdAt"));
		bean.setUpdatedAt(resultSet.getTimestamp("updatedAt"));
		bean.setLatitude(resultSet.getDouble("latitude"));
		bean.setLongitude(resultSet.getDouble("longitude"));

		return bean;
	}

	// findAll
	public List<RentalSpotBean> findAll() {
		List<RentalSpotBean> list = new ArrayList<>();

		String sql = "SELECT spotId, spotCode, spotName, spotAddress, spotStatus, merchantId, "
				+ "createdAt, updatedAt, latitude, longitude "
				+ "FROM renting_Spot";

		try (Connection conn = GetConn.byJDBC();
				PreparedStatement preparedStatement = conn.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				list.add(mapBean(resultSet));
			}

		} catch (SQLException e) {
			System.err.println("查詢全部：SQL 錯誤 → " + e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	// singleCatching
	public RentalSpotBean findById(Integer spotId) {
		RentalSpotBean bean = null;
		String sql = "SELECT spotId, spotCode, spotName, spotAddress, spotStatus, merchantId, "
				+ "createdAt, updatedAt, latitude, longitude " + "FROM renting_Spot WHERE spotId = ?";

		try (Connection conn = GetConn.byPool();
				PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

			preparedStatement.setInt(1, spotId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next())
					bean = mapBean(resultSet);
			}

		} catch (SQLException e) {
			System.err.println("查詢全部：SQL 錯誤 → " + e.getMessage());
			e.printStackTrace();
		}
		return bean;
	}

	/*
	 * 你的SpotByConditionServ裡面的List<RentalSpotBean> list =
	 * dao.findByCondition(spotCode, spotName, spotStatus, merchantId);
	 * 報錯我只好在這邊加這一段
	 */
	public List<RentalSpotBean> findByCondition(
			String spotCode,
			String spotName,
			String spotStatus,
			Integer merchantId) {
		// 直接轉呼叫原本那個 findByConditions(...)，
		// 其它條件都先塞 null（代表不過濾）
		return findByConditions(
				null, // spotId
				spotCode, // spotCode
				spotName, // spotName
				null, // spotAddress
				spotStatus, // spotStatus
				merchantId, // merchantId
				null, null, // createdStart, createdEnd
				null, null, // updatedStart, updatedEnd
				null, null // latitude, longitude
		);
	}

	// insert
	public int insert(RentalSpotBean bean) {
		String sql = "INSERT INTO renting_Spot "
				+ "(spotCode, spotName, spotAddress, spotStatus, merchantId, latitude, longitude) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = GetConn.byPool();
				PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
			preparedStatement.setString(1, bean.getSpotCode());
			preparedStatement.setString(2, bean.getSpotName());
			preparedStatement.setString(3, bean.getSpotAddress());
			preparedStatement.setString(4, bean.getSpotStatus());

			if (bean.getMerchantId() != null)
				preparedStatement.setInt(5, bean.getMerchantId());
			else
				preparedStatement.setNull(5, Types.INTEGER);

			// latitude
			if (bean.getLatitude() != null)
				preparedStatement.setDouble(6, bean.getLatitude());
			else
				preparedStatement.setNull(6, Types.DECIMAL);

			// longitude
			if (bean.getLongitude() != null)
				preparedStatement.setDouble(7, bean.getLongitude());
			else
				preparedStatement.setNull(7, Types.DECIMAL);

			return preparedStatement.executeUpdate();

		} catch (SQLException e) {
			System.err.println("新增失敗：SQL 錯誤 → " + e.getMessage());
			e.printStackTrace();
		}

		return 0;
	}
	// update

	public int update(RentalSpotBean bean) {
		String sql = "UPDATE renting_Spot SET "
				+ "spotCode = ?, " + "spotName = ?, " + "spotAddress = ?, "
				+ "spotStatus = ?, " + "merchantId = ?, "
				+ "latitude = ?, " + "longitude = ?, "
				+ "updatedAt = GETDATE() "
				+ "WHERE spotId = ?";

		try (Connection conn = GetConn.byPool();
				PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
			preparedStatement.setString(1, bean.getSpotCode());
			preparedStatement.setString(2, bean.getSpotName());
			preparedStatement.setString(3, bean.getSpotAddress());
			preparedStatement.setString(4, bean.getSpotStatus());
			if (bean.getMerchantId() != null)
				preparedStatement.setInt(5, bean.getMerchantId());
			else
				preparedStatement.setNull(5, Types.INTEGER);

			if (bean.getLatitude() != null)
				preparedStatement.setDouble(6, bean.getLatitude());
			else
				preparedStatement.setNull(6, Types.DECIMAL);

			if (bean.getLongitude() != null)
				preparedStatement.setDouble(7, bean.getLongitude());
			else
				preparedStatement.setNull(7, Types.DECIMAL);

			preparedStatement.setInt(8, bean.getSpotId());

			return preparedStatement.executeUpdate();

		} catch (SQLException e) {
			System.err.println("更新：SQL 錯誤 → " + e.getMessage());
			e.printStackTrace();
		}

		return 0;

	}

	// delete
	public int delete(Integer spotId) {

		if (spotId == null)
			return 0;
		String sql = "DELETE FROM renting_Spot WHERE spotId = ?";

		try (Connection conn = GetConn.byPool();
				PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

			preparedStatement.setInt(1, spotId);

			return preparedStatement.executeUpdate();

		} catch (SQLException e) {
			System.err.println("刪除：SQL 錯誤 → " + e.getMessage());
			e.printStackTrace();
		}
		return 0;
	}

	public List<RentalSpotBean> findByConditions(
			Integer spotId,
			String spotCode,
			String spotName,
			String spotAddress,
			String spotStatus,
			Integer merchantId,
			Date createdStart,
			Date createdEnd,
			Date updatedStart,
			Date updatedEnd,
			Double latitude,
			Double longitude) {

		List<RentalSpotBean> list = new ArrayList<>();
		List<Object> params = new ArrayList<>();

		StringBuilder sql = new StringBuilder(
				"SELECT spotId, spotCode, spotName, spotAddress, "
						+ "spotStatus, merchantId, "
						+ "createdAt, updatedAt, latitude, longitude "
						+ "FROM renting_Spot WHERE 1=1 ");

		// 查詢單筆
		if (spotId != null) {
			sql.append(" AND spotId = ? ");
			params.add(spotId);
		}
		if (merchantId != null) {
			sql.append(" AND merchantId = ? ");
			params.add(merchantId);
		}
		if (spotStatus != null && !spotStatus.isBlank()) {
			sql.append(" AND spotStatus = ? ");
			params.add(spotStatus);
		}
		if (latitude != null) {
			sql.append(" AND latitude = ? ");
			params.add(latitude);
		}
		if (longitude != null) {
			sql.append(" AND longitude = ? ");
			params.add(longitude);
		}

		// 模糊查詢
		if (spotCode != null && !spotCode.isBlank()) {
			sql.append(" AND spotCode LIKE ? ");
			params.add("%" + spotCode.trim() + "%");
		}
		if (spotName != null && !spotName.isBlank()) {
			sql.append(" AND spotName LIKE ? ");
			params.add("%" + spotName.trim() + "%");
		}
		if (spotAddress != null && !spotAddress.isBlank()) {
			sql.append(" AND spotAddress LIKE ? ");
			params.add("%" + spotAddress.trim() + "%");
		}

		if (createdStart != null) {
			sql.append(" AND createdAt >= ? ");
			params.add(createdStart);
		}
		if (createdEnd != null) {
			sql.append(" AND createdAt <= ? ");
			params.add(createdEnd);
		}
		if (updatedStart != null) {
			sql.append(" AND updatedAt >= ? ");
			params.add(updatedStart);
		}
		if (updatedEnd != null) {
			sql.append(" AND updatedAt <= ? ");
			params.add(updatedEnd);
		}

		try (Connection conn = GetConn.byPool();
				PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < params.size(); i++) {
				stmt.setObject(i + 1, params.get(i));
			}

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				RentalSpotBean bean = new RentalSpotBean();

				bean.setSpotId(rs.getInt("spotId"));
				bean.setSpotCode(rs.getString("spotCode"));
				bean.setSpotName(rs.getString("spotName"));
				bean.setSpotAddress(rs.getString("spotAddress"));
				bean.setSpotStatus(rs.getString("spotStatus"));
				bean.setMerchantId(rs.getInt("merchantId"));
				bean.setCreatedAt(rs.getTimestamp("createdAt"));
				bean.setUpdatedAt(rs.getTimestamp("updatedAt"));
				bean.setLatitude(rs.getDouble("latitude"));
				bean.setLongitude(rs.getDouble("longitude"));

				list.add(bean);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

}
