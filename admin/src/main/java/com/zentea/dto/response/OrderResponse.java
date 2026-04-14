package com.zentea.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private BigDecimal totalAmount;
    private String status;
    private String remark;
    private List<OrderItemResponse> items;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
