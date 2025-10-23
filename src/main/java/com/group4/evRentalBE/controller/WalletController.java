package com.group4.evRentalBE.controller;

import com.group4.evRentalBE.constant.ResponseObject;
import com.group4.evRentalBE.model.dto.request.AdminTopupRequest;
import com.group4.evRentalBE.service.WalletService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wallet/topups")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class WalletController {
    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<ResponseObject> myWallet() {
        Map<String, Object> data = walletService.getMyWallet();
        return ResponseEntity.ok(new ResponseObject(200, "Wallet retrieved", data));
    }

    @PostMapping("/admin/topup")
    public ResponseEntity<ResponseObject> adminTopup(@RequestBody AdminTopupRequest req) {
        Map<String, Object> data = walletService.adminTopup(req.getUserId(), req.getAmount(), req.getNote());
        return ResponseEntity.ok(new ResponseObject(200, "Wallet credited manually", data));
    }

    @PostMapping
    public ResponseEntity<Map<String,String>> createBill(@RequestParam long amountVnd) {
        String billId = walletService.createTopupBill(amountVnd);
        return ResponseEntity.ok(Map.of("billId", billId));
    }

    @PostMapping("/{billId}/vnpay-url")
    public ResponseEntity<ResponseObject> buildVnpUrl(
            @PathVariable String billId,
            HttpServletRequest request) {

        Map<String, Object> data = walletService.buildVnPayUrl(billId, request.getRemoteAddr());

        return ResponseEntity.ok(
                new ResponseObject(
                        200,
                        "VNPay URL created successfully",
                        data
                )
        );
    }

    @RequestMapping(value="/vnpay-return", method={RequestMethod.GET, RequestMethod.POST})
    @Hidden
    public ResponseEntity<Map<String,String>> vnpayReturn(@RequestParam Map<String,String> params) {
        return ResponseEntity.ok(walletService.handleVnPayReturn(params));
    }
}
