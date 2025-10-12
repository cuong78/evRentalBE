package com.group4.evRentalBE.model.entity;


import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "Customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ COMPOSITION: Customer thuộc về User
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String cccd;
    private String gplx;
    private LocalDate cccdExpiry;
    private LocalDate gplxExpiry;
    private String cccdPhoto;
    private String gplxPhoto;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ ASSOCIATION: Customer tạo nhiều Booking (không cascade ALL)
    @OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Booking> bookings = new ArrayList<>();

    // ✅ BUSINESS METHODS
    public boolean isDocumentValid() {
        LocalDate today = LocalDate.now();
        return cccd != null && !cccd.isEmpty()
                && gplx != null && !gplx.isEmpty()
                && cccdExpiry != null && cccdExpiry.isAfter(today)
                && gplxExpiry != null && gplxExpiry.isAfter(today);
    }

    public List<Booking> getActiveBookings() {
        return bookings.stream()
                .filter(Booking::isActive)
                .collect(Collectors.toList());
    }

    public boolean hasActiveBooking() {
        return bookings.stream().anyMatch(Booking::isActive);
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