package com.zentea.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zentea.dto.request.OrderCreateRequest;
import com.zentea.dto.response.DashboardResponse;
import com.zentea.dto.response.OrderResponse;

public interface OrderService {

    OrderResponse create(Long userId, OrderCreateRequest request);

    IPage<OrderResponse> pageByUser(Long userId, int pageNum, int pageSize, String status);

    IPage<OrderResponse> pageAll(int pageNum, int pageSize, String status);

    OrderResponse getById(Long id);

    void updateStatus(Long id, String status);

    DashboardResponse getDashboardStats();
}
