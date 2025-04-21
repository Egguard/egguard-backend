package com.egguard.egguardbackend.entities;

import com.egguard.egguardbackend.enums.RobotStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "robots")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Robot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_date", updatable = false)
    @CreationTimestamp // Assuming purchaseDate is when the record is created
    private LocalDateTime purchaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RobotStatus status;
}
