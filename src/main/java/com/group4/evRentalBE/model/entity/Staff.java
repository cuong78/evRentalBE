package com.group4.evRentalBE.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String role;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private RentalStation station;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffBooking> staffBookings = new ArrayList<>();

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffReturn> staffReturns = new ArrayList<>();

    // ✅ BUSINESS METHODS - ĐÃ CẬP NHẬT
    public void handleBooking(Booking booking) {
        StaffBooking staffBooking = new StaffBooking();
        staffBooking.setStaff(this);
        staffBooking.setBooking(booking);
        // Không cần set ID vì đã dùng @GeneratedValue
        this.staffBookings.add(staffBooking);
    }

    public void handleReturn(ReturnTransaction returnTransaction) {
        StaffReturn staffReturn = new StaffReturn();
        staffReturn.setStaff(this);
        staffReturn.setReturnTransaction(returnTransaction);
        // Không cần set ID vì đã dùng @GeneratedValue
        this.staffReturns.add(staffReturn);
    }

    public int getTotalBookingsHandled() {
        return this.staffBookings.size();
    }

    public int getTotalReturnsHandled() {
        return this.staffReturns.size();
    }

    public List<Booking> getHandledBookings() {
        return this.staffBookings.stream()
                .map(StaffBooking::getBooking)
                .toList();
    }

    public List<ReturnTransaction> getHandledReturns() {
        return this.staffReturns.stream()
                .map(StaffReturn::getReturnTransaction)
                .toList();
    }

    public boolean isAvailable() {
        // Staff được coi là available nếu đang làm việc tại station
        return this.station != null;
    }

    public void transferToStation(RentalStation newStation) {
        if (this.station != null) {
            this.station.getStaffMembers().remove(this);
        }
        this.station = newStation;
        if (newStation != null) {
            newStation.getStaffMembers().add(this);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public RentalStation getWorkStation() {
        return this.station;
    }

    public boolean canHandleBooking() {
        return isAvailable() && "ACTIVE".equals(this.role);
    }

    public boolean canHandleReturn() {
        return isAvailable() && ("ACTIVE".equals(this.role) || "MANAGER".equals(this.role));
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