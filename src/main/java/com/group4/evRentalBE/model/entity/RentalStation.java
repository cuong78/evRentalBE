package com.group4.evRentalBE.model.entity;


import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    // ✅ AGGREGATION: Station được quản lý bởi User có role ADMIN (không cascade)
    @ManyToOne
    @JoinColumn(name = "admin_user_id")
    private User adminUser;

    // ✅ AGGREGATION: Station có Staff Users (không cascade ALL)
    // Staff có thể chuyển station khác
    @OneToMany(mappedBy = "managedStation", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<User> staffUsers = new ArrayList<>();

    // ✅ AGGREGATION: Station chứa Vehicle (không cascade ALL)
    @OneToMany(mappedBy = "station", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Vehicle> vehicles = new ArrayList<>();

    // ✅ ASSOCIATION: Station xử lý Booking
    @OneToMany(mappedBy = "station", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    // ✅ BUSINESS METHODS
    public List<Vehicle> getAvailableVehicles() {
        return vehicles.stream()
                .filter(Vehicle::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Vehicle> getVehiclesByType(VehicleType type) {
        return vehicles.stream()
                .filter(v -> v.getType().equals(type))
                .collect(Collectors.toList());
    }

    public List<User> getStaffUsers() {
        return new ArrayList<>(staffUsers);
    }

    public void assignStaff(User staffUser) {
        if (!staffUsers.contains(staffUser) && staffUser.hasRole("STAFF")) {
            staffUsers.add(staffUser);
            staffUser.setManagedStation(this);
        }
    }

    public void removeStaff(User staffUser) {
        staffUsers.remove(staffUser);
        staffUser.setManagedStation(null);
    }

    public void addVehicle(Vehicle vehicle) {
        if (!vehicles.contains(vehicle)) {
            vehicles.add(vehicle);
            vehicle.setStation(this);
        }
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