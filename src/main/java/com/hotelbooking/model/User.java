package com.hotelbooking.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Entity
@Table(name = "users", schema = "myapps")
@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"bookings", "reviews"})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "uuid", updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "exp_date")
    private Timestamp expDate;

    @CreationTimestamp
    @Column(name = "cre_date", updatable = false)
    private Timestamp createDate;

    @Transient
    private String token;

    @Column(name = "active", nullable = false)
    private boolean active = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Booking> bookings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews;

    @PrePersist
    public void beforeSave() {
        if (uuid == null) {
            this.uuid = UUID.randomUUID();
        }
        long thirtyDaysInMillis = TimeUnit.DAYS.toMillis(30);
        this.expDate = new Timestamp(System.currentTimeMillis() + thirtyDaysInMillis);
    }
}
