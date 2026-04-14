package com.zentea.service;

import com.zentea.dto.request.CategoryCreateRequest;
import com.zentea.dto.request.CategoryUpdateRequest;
import com.zentea.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> list();

    CategoryResponse create(CategoryCreateRequest request);

    CategoryResponse update(Long id, CategoryUpdateRequest request);

    void delete(Long id);
}
