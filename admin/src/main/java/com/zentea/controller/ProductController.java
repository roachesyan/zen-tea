package com.zentea.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zentea.common.result.Result;
import com.zentea.dto.request.ProductCreateRequest;
import com.zentea.dto.request.ProductUpdateRequest;
import com.zentea.dto.response.ProductResponse;
import com.zentea.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品管理")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "分页查询商品")
    @GetMapping
    public Result<IPage<ProductResponse>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(productService.page(pageNum, pageSize, categoryId, keyword, status));
    }

    @Operation(summary = "按分类获取商品列表")
    @GetMapping("/list")
    public Result<List<ProductResponse>> listByCategory(
            @RequestParam(required = false) Long categoryId) {
        return Result.success(productService.listByCategory(categoryId));
    }

    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    public Result<ProductResponse> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @Operation(summary = "创建商品")
    @PostMapping
    public Result<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        return Result.created(productService.create(request));
    }

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    public Result<ProductResponse> update(@PathVariable Long id,
                                          @Valid @RequestBody ProductUpdateRequest request) {
        return Result.success(productService.update(id, request));
    }

    @Operation(summary = "更新商品状态(上下架)")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }
}
