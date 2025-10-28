package com.group4.evRentalBE.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="TopupBill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopupBill {
    @Id
    private String id; // dùng UUID string để map thẳng vào vnp_TxnRef

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(nullable=false)
    private Long amount; // VND (ví dụ 150000)

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    @Builder.Default
    private Status status = Status.PENDING;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    private String transactionId;     // từ VNPay (nếu có)
    @Column(columnDefinition="TEXT")
    private String gatewayResponse;   // log JSON trả về

    public enum Status { PENDING, SUCCESS, FAILED, EXPIRED }

    @PrePersist
    void pre() {
        createdAt = LocalDateTime.now();
        expiresAt = createdAt.plusMinutes(10);
    }

    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
}
