package com.group4.evRentalBE.infrastructure.constant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResponseObject {
    @JsonProperty("statusCode")
    private int statusCode; // Thêm trạng thái HTTP

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private Object data;
}