package com.zentea.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {

    @NotBlank(message = "状态不能为空")
    private String status;
}
