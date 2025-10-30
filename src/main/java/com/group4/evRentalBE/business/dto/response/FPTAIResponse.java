package com.group4.evRentalBE.business.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FPTAIResponse {
    @JsonProperty("errorCode")
    private String errorCode;
    
    @JsonProperty("errorMessage")
    private String errorMessage;
    
    @JsonProperty("data")
    private List<CCCDData> data;
    
    @Data
    public static class CCCDData {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("id_prob")
        private String idProb;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("name_prob")
        private String nameProb;
        
        @JsonProperty("dob")
        private String dob;
        
        @JsonProperty("dob_prob")
        private String dobProb;
        
        @JsonProperty("sex")
        private String sex;
        
        @JsonProperty("sex_prob")
        private String sexProb;
        
        @JsonProperty("nationality")
        private String nationality;
        
        @JsonProperty("nationality_prob")
        private String nationalityProb;
        
        @JsonProperty("home")
        private String home;
        
        @JsonProperty("home_prob")
        private String homeProb;
        
        @JsonProperty("address")
        private String address;
        
        @JsonProperty("address_prob")
        private String addressProb;
        
        @JsonProperty("doe")
        private String doe;
        
        @JsonProperty("doe_prob")
        private String doeProb;
        
        @JsonProperty("issue_date")
        private String issueDate;
        
        @JsonProperty("issue_date_prob")
        private String issueDateProb;
        
        @JsonProperty("issue_loc")
        private String issueLoc;
        
        @JsonProperty("issue_loc_prob")
        private String issueLocProb;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("type_prob")
        private String typeProb;
    }
}
