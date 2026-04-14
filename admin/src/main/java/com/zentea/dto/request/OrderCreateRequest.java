package com.zentea.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {

    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemCreateRequest> items;

    @Size(max = 500, message = "备注最长500个字符")
    private String remark;
}
