package com.zentea.controller;

import com.zentea.common.result.Result;
import com.zentea.dto.request.CategoryCreateRequest;
import com.zentea.dto.request.CategoryUpdateRequest;
import com.zentea.dto.response.CategoryResponse;
import com.zentea.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取分类列表")
    @GetMapping
    public Result<List<CategoryResponse>> list() {
        return Result.success(categoryService.list());
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest request) {
        return Result.created(categoryService.create(request));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<CategoryResponse> update(@PathVariable Long id,
                                           @Valid @RequestBody CategoryUpdateRequest request) {
        return Result.success(categoryService.update(id, request));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
