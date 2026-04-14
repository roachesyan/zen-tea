package com.zentea.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryUpdateRequest {

    @Size(max = 50, message = "分类名称最长50个字符")
    private String name;

    private String icon;

    private Integer sort;

    private Integer status;
}
