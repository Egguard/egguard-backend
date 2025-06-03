package com.egguard.egguardbackend.farm;

import com.egguard.egguardbackend.egg.Egg;
import com.egguard.egguardbackend.notification.Notification;
import com.egguard.egguardbackend.shared.entity.Robot;
import com.egguard.egguardbackend.shared.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "farms")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Farm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY) // Lazy fetch is often preferred for ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Robot> robots;

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Notification> notifications;

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Egg> eggs;
}
