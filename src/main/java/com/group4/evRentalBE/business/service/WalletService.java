package com.group4.evRentalBE.business.service;

import java.util.Map;

public interface WalletService {
    String createTopupBill(long amountVnd);
    Map<String, Object> buildVnPayUrl(String billId, String clientIp);

    Map<String,String> handleVnPayReturn(Map<String,String> params);

    Map<String, Object> getMyWallet();            // API A
    Map<String, Object> adminTopup(Long userId, Long amount, String note); // API B
}
