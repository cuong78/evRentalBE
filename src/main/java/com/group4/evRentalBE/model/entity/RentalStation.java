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

    // ✅ AGGREGATION: Station được quản lý bởi Admin (không cascade)
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    // ✅ AGGREGATION: Station thuê Staff (không cascade ALL)
    // Staff có thể chuyển station khác
    @OneToMany(mappedBy = "station", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Staff> staffMembers = new ArrayList<>();

    // ✅ AGGREGATION: Station chứa Vehicle (không cascade ALL)
    @OneToMany(mappedBy = "station", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Vehicle> vehicles = new ArrayList<>();

    // ✅ ASSOCIATION: Station xử lý Booking
    @OneToMany(mappedBy = "station", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
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

    public List<Staff> getStaffMembers() {
        return new ArrayList<>(staffMembers);
    }

    public void assignStaff(Staff staff) {
        if (!staffMembers.contains(staff)) {
            staffMembers.add(staff);
            staff.setStation(this);
        }
    }

    public void removeStaff(Staff staff) {
        staffMembers.remove(staff);
        staff.setStation(null);
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