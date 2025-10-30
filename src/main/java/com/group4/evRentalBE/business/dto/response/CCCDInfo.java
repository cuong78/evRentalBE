package com.group4.evRentalBE.business.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CCCDInfo {
    @JsonProperty("id")
    private String id;              // Số CCCD
    
    @JsonProperty("fullName")
    private String fullName;        // Họ tên
    
    @JsonProperty("dateOfBirth")
    private String dateOfBirth;     // Ngày sinh
    
    @JsonProperty("gender")
    private String gender;          // Giới tính
    
    @JsonProperty("nationality")
    private String nationality;     // Quốc tịch
    
    @JsonProperty("placeOfOrigin")
    private String placeOfOrigin;   // Quê quán
    
    @JsonProperty("placeOfResidence")
    private String placeOfResidence; // Nơi thường trú
    
    @JsonProperty("issueDate")
    private String issueDate;      // Ngày phát hành (từ mặt sau)
    
    @JsonProperty("issuedBy")
    private String issuedBy;       // Nơi cấp (từ mặt sau)
    
    @JsonProperty("expiryDate")
    private String expiryDate;      // Ngày hết hạn


}
