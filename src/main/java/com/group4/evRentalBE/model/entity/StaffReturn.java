package com.group4.evRentalBE.model.entity;


import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Staff_Return")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffReturn {
    @EmbeddedId
    private StaffReturnId id;

    @ManyToOne
    @MapsId("staffId")
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @ManyToOne
    @MapsId("returnId")
    @JoinColumn(name = "return_id")
    private ReturnTransaction returnTransaction;

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
class StaffReturnId implements java.io.Serializable {
    private Long staffId;
    private Long returnId;
}