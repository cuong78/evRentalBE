package com.group4.evRentalBE.model.entity;


import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ COMPOSITION: Admin thuộc về User
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ AGGREGATION: Admin quản lý nhiều Station (không cascade ALL)
    // Station có thể tồn tại khi không có admin
    @OneToMany(mappedBy = "admin", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<RentalStation> managedStations = new ArrayList<>();

    // ✅ BUSINESS METHODS
    public void manageStation(RentalStation station) {
        if (!managedStations.contains(station)) {
            managedStations.add(station);
            station.setAdmin(this);
        }
    }

    public void removeStation(RentalStation station) {
        managedStations.remove(station);
        station.setAdmin(null);
    }

    public List<RentalStation> getManagedStations() {
        return new ArrayList<>(managedStations);
    }

    public void addStation(RentalStation station) {
        manageStation(station);
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
