package com.example.backend.repository.maintenance;

import com.example.backend.model.maintenance.MaintenanceInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceInformationRepository extends JpaRepository<MaintenanceInformation, Integer> {

    List<MaintenanceInformation> findBySpotIdOrderByReportedAtDescTicketIdAsc(Integer spotId);

    List<MaintenanceInformation> findByIssueStatusOrderByReportedAtDescTicketIdAsc(String issueStatus);
}
