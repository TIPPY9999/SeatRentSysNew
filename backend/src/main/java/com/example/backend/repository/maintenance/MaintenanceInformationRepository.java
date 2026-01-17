package com.example.backend.repository.maintenance;

import com.example.backend.model.maintenance.MaintenanceInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceInformationRepository extends JpaRepository<MaintenanceInformation, Integer> {

    List<MaintenanceInformation> findBySpotIdOrderByReportedAtDescTicketIdAsc(Integer spotId);

    // 用於狀態查詢
    List<MaintenanceInformation> findByIssueStatusOrderByReportedAtDescTicketIdAsc(String issueStatus);

    // 用於 In List 查詢 (Active / History)
    List<MaintenanceInformation> findByIssueStatusInOrderByReportedAtDescTicketIdAsc(List<String> issueStatusList);

    // 查詢全部並排序 (解決 getAllTickets 沒排序的問題)
    List<MaintenanceInformation> findAllByOrderByReportedAtDescTicketIdAsc();

    //讓我們可以查某張椅子的所有工單
    List<MaintenanceInformation> findBySeatsIdOrderByReportedAtDescTicketIdAsc(Integer seatsId);

    //用於轉移工單
    // 在 interface 裡面新增這行
List<MaintenanceInformation> findByAssignedStaffIdAndIssueStatusIn(Integer assignedStaffId, List<String> issueStatuses);

boolean existsByAssignedStaffIdAndIssueStatusIn(Integer assignedStaffId, List<String> statuses);
}
