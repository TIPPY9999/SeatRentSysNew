package com.example.backend.model.member;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "admin")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admId")
    private Integer admId;

    @Column(name = "admUsername", nullable = false, unique = true)
    private String admUsername;

    @Column(name = "admPassword", nullable = false)
    private String admPassword;

    @Column(name = "admName", nullable = false)
    private String admName;

    @Column(name = "admEmail", nullable = false)
    private String admEmail;

    @Column(name = "admRole", nullable = false)
    private Integer admRole;

    @Column(name = "createdAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}