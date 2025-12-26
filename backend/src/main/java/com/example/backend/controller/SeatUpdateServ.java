package com.example.backend.controller;

import com.example.backend.model.Seat;
import com.example.backend.service.SeatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeatUpdateServ {

	// [修正：改用建構子注入]
	private final SeatService seatService;

	public SeatUpdateServ(SeatService seatService) {
		this.seatService = seatService;
	}

	// [處理進入編輯頁時的資料讀取]
	// 對應前端 axios.get('/seat/update', { params: { seatsId: ... } })
	@GetMapping("/seat/update")
	public Seat getOne(HttpServletRequest req) {
		String seatsIdStr = req.getParameter("seatsId");
		if (seatsIdStr != null && !seatsIdStr.isBlank()) {
			return seatService.selectById(Integer.valueOf(seatsIdStr));
		}
		return null;
	}

	// [處理表單送出的更新]
	// 對應前端 axios.post('/seat/update', params)
	@PostMapping("/seat/update")
	public Seat update(HttpServletRequest req) {
		String seatsIdStr = req.getParameter("seatsId");
		String seatsName = req.getParameter("seatsName");
		String seatsType = req.getParameter("seatsType");
		String seatsStatus = req.getParameter("seatsStatus");
		String spotIdStr = req.getParameter("spotId");
		String serialNumber = req.getParameter("serialNumber");

		Integer spotId = null;
		if (spotIdStr != null && !spotIdStr.isBlank()) {
			spotId = Integer.valueOf(spotIdStr.trim());
		}

		Seat seat = null;
		if (seatsIdStr != null && !seatsIdStr.isBlank()) {
			// 先查出舊資料
			seat = seatService.selectById(Integer.valueOf(seatsIdStr));
			if (seat != null) {
				// 更新欄位
				seat.setSeatsName(seatsName);
				seat.setSeatsType(seatsType);
				seat.setSeatsStatus(seatsStatus);
				seat.setSpotId(spotId);
				seat.setSerialNumber((serialNumber == null || serialNumber.isBlank()) ? null : serialNumber.trim());

				// 執行更新並回傳結果 (JSON)
				return seatService.update(seat);
			}
		}
		return null;
	}
}
