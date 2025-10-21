package com.group4.evRentalBE.service.impl;

import com.group4.evRentalBE.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.model.entity.TopupBill;
import com.group4.evRentalBE.model.entity.User;
import com.group4.evRentalBE.model.entity.Wallet;
import com.group4.evRentalBE.repository.TopupBillRepository;
import com.group4.evRentalBE.repository.UserRepository;
import com.group4.evRentalBE.repository.WalletRepository;
import com.group4.evRentalBE.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepo;
    private final TopupBillRepository billRepo;
    private final UserRepository userRepo;

    @Value("${payment.vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${payment.vnpay.secret-key}")
    private String vnpSecretKey;

    @Value("${payment.vnpay.url}")
    private String vnpUrl;

    @Value("${payment.vnpay.wallet-return-url}")
    private String vnpReturnUrl;;

    // ===== helpers: giống cách bạn đã ký HMAC cho Booking/VNPay =====
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ===== helpers =====
    private static String buildSignData(Map<String,String> params) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> e : new TreeMap<>(params).entrySet()) {
            sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .append('&');
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
    private String hmac512(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        StringBuilder out = new StringBuilder();
        for (byte b : mac.doFinal(data.getBytes(StandardCharsets.UTF_8))) out.append(String.format("%02x", b));
        return out.toString();
    }
    private String buildUrl(String base, Map<String,String> params) {
        StringBuilder u = new StringBuilder(base).append('?');
        params.forEach((k,v) -> u.append(URLEncoder.encode(k, StandardCharsets.UTF_8))
                .append('=')
                .append(URLEncoder.encode(v, StandardCharsets.UTF_8))
                .append('&'));
        u.deleteCharAt(u.length()-1);
        return u.toString();
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User u = (User) auth.getPrincipal();
        return userRepo.findById(u.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Wallet getOrCreateWallet(User u) {
        return walletRepo.findByUserUserId(u.getUserId())
                .orElseGet(() -> walletRepo.save(Wallet.builder().user(u).balance(0L).build()));
    }

    // ===== API A: xem ví của chính mình =====
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMyWallet() {
        User u = currentUser();
        Wallet w = getOrCreateWallet(u);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", u.getUserId());
        data.put("balance", w.getBalance());
        data.put("updatedAt", w.getUpdatedAt());
        return data;
    }

    // ===== API B: admin/staff nạp hộ =====
    @Override
    @Transactional
    public Map<String, Object> adminTopup(Long userId, Long amount, String note) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (amount == null || amount <= 0) throw new IllegalArgumentException("Amount must be > 0");

        // Người đang thực hiện (admin/staff)
        User actor = currentUser();

        // Tìm user được nạp
        User target = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        // Lấy ví và cộng tiền
        Wallet wallet = getOrCreateWallet(target);
        wallet.credit(amount);
        walletRepo.save(wallet);

        // Lưu 1 bản ghi TopupBill trạng thái SUCCESS cho mục đích audit
        TopupBill bill = TopupBill.builder()
                .id(UUID.randomUUID().toString())
                .user(target)
                .amount(amount)
                .build();
        bill.setStatus(TopupBill.Status.SUCCESS);
        bill.setTransactionId("MANUAL");
        bill.setGatewayResponse("Manual adjust by userId=" + actor.getUserId()
                + (note != null && !note.isBlank() ? (", note=" + note) : ""));
        billRepo.save(bill);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", target.getUserId());
        data.put("newBalance", wallet.getBalance());
        data.put("topupAmount", amount);
        data.put("billId", bill.getId());
        data.put("note", note);
        return data;
    }

    // ========== API #1 ==========
    @Override
    @Transactional
    public String createTopupBill(long amountVnd) {
        if (amountVnd <= 0) throw new IllegalArgumentException("Amount must be > 0");
        User u = currentUser();
        TopupBill bill = TopupBill.builder()
                .id(UUID.randomUUID().toString())
                .user(u)
                .amount(amountVnd)
                .build();
        billRepo.save(bill);
        return bill.getId();
    }

    // ========== API #2 ==========
    @Override
    @Transactional
    public Map<String, Object> buildVnPayUrl(String billId, String clientIp) {
        TopupBill bill = billRepo.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        if (bill.isExpired()) {
            bill.setStatus(TopupBill.Status.EXPIRED);
            billRepo.save(bill);
            throw new IllegalStateException("Bill expired");
        }

        try {
            Map<String, String> p = new TreeMap<>();
            p.put("vnp_Version", "2.1.0");
            p.put("vnp_Command", "pay");
            p.put("vnp_TmnCode", vnpTmnCode);
            p.put("vnp_Locale", "vn");
            p.put("vnp_CurrCode", "VND");
            p.put("vnp_TxnRef", bill.getId());
            p.put("vnp_OrderInfo", "Topup wallet bill: " + bill.getId());
            p.put("vnp_OrderType", "other");
            p.put("vnp_Amount", String.valueOf(bill.getAmount() * 100));
            p.put("vnp_ReturnUrl", vnpReturnUrl);
            p.put("vnp_CreateDate", LocalDateTime.now().format(FMT));
            p.put("vnp_IpAddr", clientIp != null ? clientIp : "0.0.0.0");
            p.put("vnp_ExpireDate", bill.getExpiresAt().format(FMT));

            String sign = hmac512(vnpSecretKey, buildSignData(p));
            p.put("vnp_SecureHash", sign);

            String url = buildUrl(vnpUrl, p);

            long remaining = Duration.between(LocalDateTime.now(), bill.getExpiresAt()).toMinutes();
            Map<String, Object> data = new HashMap<>();
            data.put("vnpayUrl", url);
            data.put("expiryTime", bill.getExpiresAt());
            data.put("remainingMinutes", Math.max(remaining, 0));

            return data;
        } catch (Exception e) {
            throw new RuntimeException("Error creating VNPay URL", e);
        }
    }

    // ========== Callback ==========
    @Override
    @Transactional
    public Map<String,String> handleVnPayReturn(Map<String,String> params) {
        Map<String,String> rsp = new HashMap<>();
        try {
            String code = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef"); // chính là billId
            TopupBill bill = billRepo.findById(txnRef)
                    .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

            if (bill.isExpired()) {
                bill.setStatus(TopupBill.Status.EXPIRED);
                billRepo.save(bill);
                rsp.put("RspCode","98"); rsp.put("Message","Payment time expired");
                return rsp;
            }

            if ("00".equals(code)) {
                bill.setStatus(TopupBill.Status.SUCCESS);
                bill.setTransactionId(params.get("vnp_TransactionNo"));
                bill.setGatewayResponse(params.toString());
                billRepo.save(bill);

                // credit vào ví
                Wallet w = getOrCreateWallet(bill.getUser());
                w.credit(bill.getAmount());
                walletRepo.save(w);

                rsp.put("RspCode","00"); rsp.put("Message","Topup successful");
            } else {
                bill.setStatus(TopupBill.Status.FAILED);
                bill.setGatewayResponse(params.toString());
                billRepo.save(bill);
                rsp.put("RspCode","99"); rsp.put("Message","Payment failed: " + code);
            }
        } catch (Exception e) {
            rsp.put("RspCode","99"); rsp.put("Message","Error: " + e.getMessage());
        }
        return rsp;
    }
}
