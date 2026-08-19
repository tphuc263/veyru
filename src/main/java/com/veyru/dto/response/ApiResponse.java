package com.veyru.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponse<T> {
    private T data;
    private String message;

    public ApiResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public ApiResponse(String message) {
        this.message = message;
        this.data = null;
    }

    /*
     * Factory method
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(message);
    }
}
