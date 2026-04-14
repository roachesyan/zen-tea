package com.zentea.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zentea.common.result.Result;
import com.zentea.dto.request.OrderCreateRequest;
import com.zentea.dto.request.OrderStatusUpdateRequest;
import com.zentea.dto.response.DashboardResponse;
import com.zentea.dto.response.OrderResponse;
import com.zentea.security.SecurityUtils;
import com.zentea.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单管理")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.created(orderService.create(userId, request));
    }

    @Operation(summary = "查询我的订单")
    @GetMapping("/mine")
    public Result<IPage<OrderResponse>> pageByUser(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.pageByUser(userId, pageNum, pageSize, status));
    }

    @Operation(summary = "查询所有订单(管理员)")
    @GetMapping
    public Result<IPage<OrderResponse>> pageAll(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        return Result.success(orderService.pageAll(pageNum, pageSize, status));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<OrderResponse> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @Operation(summary = "更新订单状态")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Valid @RequestBody OrderStatusUpdateRequest request) {
        orderService.updateStatus(id, request.getStatus());
        return Result.success();
    }

    @Operation(summary = "获取看板统计数据")
    @GetMapping("/dashboard/stats")
    public Result<DashboardResponse> getDashboardStats() {
        return Result.success(orderService.getDashboardStats());
    }
}
