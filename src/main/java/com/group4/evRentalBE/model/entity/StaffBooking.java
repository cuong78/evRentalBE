package com.group4.evRentalBE.model.entity;


import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_booking", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"staff_id", "booking_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private LocalDateTime handledAt;

    @PrePersist
    protected void onCreate() {
        handledAt = LocalDateTime.now();
    }
}

