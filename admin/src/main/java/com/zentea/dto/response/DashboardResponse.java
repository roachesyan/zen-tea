package com.zentea.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardResponse {

    private Long todayOrderCount;
    private BigDecimal todayRevenue;
    private Long totalOrderCount;
    private BigDecimal totalRevenue;
    private Long pendingOrderCount;
}
