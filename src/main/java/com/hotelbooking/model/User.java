package com.hotelbooking.model;

import com.hotelbooking.Enum.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Entity
@Table(name = "users", schema = "myapps",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_User_Username", columnNames = "username"),
                @UniqueConstraint(name = "UQ_User_Email", columnNames = "email")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(exclude = {"passwordHash", "bookings", "reviews"}) // Exclude sensitive data and relationships
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Changed from primitive long to Long for better null handling

    @NaturalId
    @Column(name = "uuid", updatable = false, nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private UUID uuid;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "exp_date")
    private Timestamp expDate;

    @CreationTimestamp
    @Column(name = "cre_date", updatable = false)
    private Timestamp createDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    @Transient
    private String token;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean active = false;  // Changed from primitive to Boolean

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Booking> bookings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews;

    @PrePersist
    public void prePersist() {
        if (uuid == null) {
            this.uuid = UUID.randomUUID();
        }
        if (this.expDate == null) {
            long thirtyDaysInMillis = TimeUnit.DAYS.toMillis(30);
            this.expDate = new Timestamp(System.currentTimeMillis() + thirtyDaysInMillis);
        }
    }
}