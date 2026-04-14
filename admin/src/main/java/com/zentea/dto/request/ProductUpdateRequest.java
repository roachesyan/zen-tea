package com.zentea.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {

    private Long categoryId;

    @Size(max = 100, message = "商品名称最长100个字符")
    private String name;

    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    private String imageUrl;

    private String description;

    private Integer status;

    private Integer sort;
}
