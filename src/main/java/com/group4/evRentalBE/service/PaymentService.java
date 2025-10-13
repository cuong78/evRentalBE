package com.group4.evRentalBE.service;

import com.group4.evRentalBE.constant.ResponseObject;

import java.util.Map;

public interface PaymentService {

    ResponseObject createVnPayUrl(String email);

    Map<String, String> handleVNPayReturn(Map<String, String> params);

}
