package com.cinetime.payload.response.business;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class ApiMessageResponse {
    private String message;

    public ApiMessageResponse() {}
    public ApiMessageResponse(String message) { this.message = message; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}