package com.zentea.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zentea.dto.request.ProductCreateRequest;
import com.zentea.dto.request.ProductUpdateRequest;
import com.zentea.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    IPage<ProductResponse> page(int pageNum, int pageSize, Long categoryId, String keyword, Integer status);

    List<ProductResponse> listByCategory(Long categoryId);

    ProductResponse getById(Long id);

    ProductResponse create(ProductCreateRequest request);

    ProductResponse update(Long id, ProductUpdateRequest request);

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
