package com.group4.evRentalBE.domain.entity;


import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "RentalStation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "admin_user_id")
    private User adminUser;

    @OneToMany(mappedBy = "managedStation", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<User> staffUsers = new ArrayList<>();

    @OneToMany(mappedBy = "station", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Vehicle> vehicles = new ArrayList<>();

    @OneToMany(mappedBy = "station", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();


    public List<User> getStaffUsers() {
        return new ArrayList<>(staffUsers);
    }



    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}