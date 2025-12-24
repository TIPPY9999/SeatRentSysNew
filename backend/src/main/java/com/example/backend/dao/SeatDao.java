package com.example.backend.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.example.backend.model.Seat;
import com.example.backend.utils.GetConn;

public class SeatDao {

	private Seat mapBean(ResultSet rs) throws SQLException {
		Seat bean = new Seat();

		bean.setSeatsId(rs.getInt("seatsId"));
		bean.setSeatsName(rs.getString("seatsName"));
		bean.setSeatsType(rs.getString("seatsType"));
		bean.setSeatsStatus(rs.getString("seatsStatus"));

		int spotId = rs.getInt("spotId");
		bean.setSpotId(rs.wasNull() ? null : spotId);

		bean.setSerialNumber(rs.getString("serialNumber"));
		bean.setCreatedAt(rs.getTimestamp("createdAt"));
		bean.setUpdatedAt(rs.getTimestamp("updatedAt"));

		return bean;
	}

	public List<Seat> findAll() {
		List<Seat> list = new ArrayList<>();
		String sql = "SELECT seatsId, seatsName, seatsType, seatsStatus, spotId, updatedAt, serialNumber, createdAt "
				+ "FROM seats";

		try (Connection conn = GetConn.byPool();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next())
				list.add(mapBean(rs));

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public Seat findById(Integer seatsId) {
		Seat bean = null;
		String sql = "SELECT seatsId, seatsName, seatsType, seatsStatus, spotId, updatedAt, serialNumber, createdAt "
				+ "FROM seats WHERE seatsId = ?";

		try (Connection conn = GetConn.byPool(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, seatsId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					bean = mapBean(rs);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return bean;
	}

	public List<Seat> findByCondition(String seatsName, String seatsType, String seatsStatus, Integer spotId,
			String serialNumber) {
		List<Seat> list = new ArrayList<>();
		List<Object> params = new ArrayList<>();

		StringBuilder sql = new StringBuilder(
				"SELECT seatsId, seatsName, seatsType, seatsStatus, spotId, updatedAt, serialNumber, createdAt "
						+ "FROM seats WHERE 1=1 ");

		if (spotId != null) {
			sql.append(" AND spotId = ? ");
			params.add(spotId);
		}
		if (seatsStatus != null && !seatsStatus.isBlank()) {
			sql.append(" AND seatsStatus = ? ");
			params.add(seatsStatus.trim());
		}
		if (seatsName != null && !seatsName.isBlank()) {
			sql.append(" AND seatsName LIKE ? ");
			params.add("%" + seatsName.trim() + "%");
		}
		if (seatsType != null && !seatsType.isBlank()) {
			sql.append(" AND seatsType LIKE ? ");
			params.add("%" + seatsType.trim() + "%");
		}
		if (serialNumber != null && !serialNumber.isBlank()) {
			sql.append(" AND serialNumber LIKE ? ");
			params.add("%" + serialNumber.trim() + "%");
		}

		try (Connection conn = GetConn.byPool(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < params.size(); i++)
				ps.setObject(i + 1, params.get(i));

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					list.add(mapBean(rs));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	// 新增：時間交給 DB
	public int insert(Seat bean) {
		String sql = "INSERT INTO seats "
				+ "(seatsName, seatsType, seatsStatus, spotId, serialNumber, createdAt, updatedAt) "
				+ "VALUES (?, ?, ?, ?, ?, GETDATE(), GETDATE())";

		try (Connection conn = GetConn.byPool(); PreparedStatement ps = conn.prepareStatement(sql)) {

			int i = 1;
			ps.setString(i++, bean.getSeatsName());
			ps.setString(i++, bean.getSeatsType());
			ps.setString(i++, bean.getSeatsStatus());

			if (bean.getSpotId() != null)
				ps.setInt(i++, bean.getSpotId());
			else
				ps.setNull(i++, Types.INTEGER);

			String sn = bean.getSerialNumber();
			if (sn != null && !sn.isBlank())
				ps.setString(i++, sn.trim());
			else
				ps.setNull(i++, Types.VARCHAR);

			return ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	// 更新：updatedAt 交給 DB
	public int update(Seat bean) {
		String sql = "UPDATE seats SET " + "seatsName = ?, seatsType = ?, seatsStatus = ?, spotId = ?, "
				+ "serialNumber = ?, updatedAt = GETDATE() " + "WHERE seatsId = ?";

		try (Connection conn = GetConn.byPool(); PreparedStatement ps = conn.prepareStatement(sql)) {

			int i = 1;
			ps.setString(i++, bean.getSeatsName());
			ps.setString(i++, bean.getSeatsType());
			ps.setString(i++, bean.getSeatsStatus());

			if (bean.getSpotId() != null)
				ps.setInt(i++, bean.getSpotId());
			else
				ps.setNull(i++, Types.INTEGER);

			String sn = bean.getSerialNumber();
			if (sn != null && !sn.isBlank())
				ps.setString(i++, sn.trim());
			else
				ps.setNull(i++, Types.VARCHAR);

			ps.setInt(i++, bean.getSeatsId());

			return ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int delete(Integer seatsId) {
		if (seatsId == null)
			return 0;

		String sql = "DELETE FROM seats WHERE seatsId = ?";

		try (Connection conn = GetConn.byPool(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, seatsId);
			return ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
