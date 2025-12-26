package com.example.backend.controller;

import com.example.backend.model.Seat;
import com.example.backend.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SeatUpdateServ {

	@Autowired
	private SeatService seatService;

	@GetMapping("/seat/update")
	public String showForm(@RequestParam("seatsId") Integer seatsId, Model model) {
		Seat seat = seatService.selectById(seatsId);
		model.addAttribute("seat", seat);
		return "seatUpdate";
	}

	@PostMapping("/seat/update")
	public String update(
			@RequestParam("seatsId") Integer seatsId,
			@RequestParam("seatsName") String seatsName,
			@RequestParam("seatsType") String seatsType,
			@RequestParam("seatsStatus") String seatsStatus,
			@RequestParam(value = "spotId", required = false) String spotIdStr,
			@RequestParam(value = "serialNumber", required = false) String serialNumber) {

		Seat bean = new Seat();
		bean.setSeatsId(seatsId);
		bean.setSeatsName(seatsName);
		bean.setSeatsType(seatsType);
		bean.setSeatsStatus(seatsStatus);

		Integer spotId = null;
		try {
			spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr.trim());
		} catch (Exception e) {
			throw new IllegalArgumentException("spotId 格式錯誤");
		}
		bean.setSpotId(spotId);

		bean.setSerialNumber((serialNumber == null || serialNumber.isBlank()) ? null : serialNumber.trim());

		seatService.update(bean);

		return "redirect:/seat/list";
	}
}
