package com.group4.evRentalBE.model.entity;


import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Staff_Booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffBooking {
    @EmbeddedId
    private StaffBookingId id;

    @ManyToOne
    @MapsId("staffId")
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @ManyToOne
    @MapsId("bookingId")
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private LocalDateTime handledAt;

    @PrePersist
    protected void onCreate() {
        handledAt = LocalDateTime.now();
    }
}

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class StaffBookingId implements java.io.Serializable {
    private Long staffId;
    private Long bookingId;
}