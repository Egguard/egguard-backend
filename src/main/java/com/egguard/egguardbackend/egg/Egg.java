package com.egguard.egguardbackend.egg;

import com.egguard.egguardbackend.farm.Farm;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "eggs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Egg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @Column(name = "coord_x", nullable = false)
    private Double coordX;

    @Column(name = "coord_y", nullable = false)
    private Double coordY;

    @Column(nullable = false)
    private Boolean picked = false; // Default value

    @Column(nullable = false)
    private Boolean broken = false; // Default value

    @Column(nullable = false, updatable = false)
    @CreationTimestamp // Timestamp when the egg was detected/created
    private LocalDateTime timestamp;
}
