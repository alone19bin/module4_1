package com.example.individuals_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorRs {
    
    private String error;

    
    private String details;

   
    private Integer status;

   
    public ErrorRs(String error, Integer status) {
        this.error = error;
        this.status = status;
        this.details = null;
    }
}
