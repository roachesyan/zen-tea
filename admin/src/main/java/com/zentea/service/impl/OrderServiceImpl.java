package com.zentea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zentea.common.constant.Constants;
import com.zentea.common.exception.BusinessException;
import com.zentea.common.result.ResultCode;
import com.zentea.common.util.OrderNoGenerator;
import com.zentea.dto.request.OrderCreateRequest;
import com.zentea.dto.request.OrderItemCreateRequest;
import com.zentea.dto.response.DashboardResponse;
import com.zentea.dto.response.OrderItemResponse;
import com.zentea.dto.response.OrderResponse;
import com.zentea.entity.OrderItem;
import com.zentea.entity.Orders;
import com.zentea.entity.Product;
import com.zentea.entity.User;
import com.zentea.mapper.OrderItemMapper;
import com.zentea.mapper.OrderMapper;
import com.zentea.mapper.ProductMapper;
import com.zentea.mapper.UserMapper;
import com.zentea.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse create(Long userId, OrderCreateRequest request) {
        List<OrderItemCreateRequest> itemRequests = request.getItems();
        Set<Long> productIds = itemRequests.stream()
                .map(OrderItemCreateRequest::getProductId)
                .collect(Collectors.toSet());

        List<Product> products = productMapper.selectBatchIds(productIds);
        var productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (OrderItemCreateRequest itemReq : itemRequests) {
            Product product = productMap.get(itemReq.getProductId());
            if (product == null || product.getStatus() != Constants.PRODUCT_ON_SHELF) {
                throw new BusinessException(ResultCode.PRODUCT_OFF_SHELF);
            }
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemCreateRequest itemReq : itemRequests) {
            Product product = productMap.get(itemReq.getProductId());
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }

        Orders order = new Orders();
        order.setOrderNo(OrderNoGenerator.generate());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(Constants.ORDER_STATUS_PENDING);
        order.setRemark(request.getRemark());
        orderMapper.insert(order);

        for (OrderItemCreateRequest itemReq : itemRequests) {
            Product product = productMap.get(itemReq.getProductId());
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setSpecs(itemReq.getSpecs());
            orderItem.setSubtotal(subtotal);
            orderItemMapper.insert(orderItem);
        }

        log.info("Order created: orderNo={}, userId={}, total={}", order.getOrderNo(), userId, totalAmount);
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<OrderResponse> pageByUser(Long userId, int pageNum, int pageSize, String status) {
        LambdaQueryWrapper<Orders> wrapper = buildQueryWrapper(userId, status);
        IPage<Orders> orderPage = orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return orderPage.convert(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<OrderResponse> pageAll(int pageNum, int pageSize, String status) {
        LambdaQueryWrapper<Orders> wrapper = buildQueryWrapper(null, status);
        IPage<Orders> orderPage = orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return orderPage.convert(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        Orders order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return toResponse(order);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        Orders order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        Set<String> validStatuses = Set.of(
                Constants.ORDER_STATUS_PENDING,
                Constants.ORDER_STATUS_MAKING,
                Constants.ORDER_STATUS_DONE,
                Constants.ORDER_STATUS_CANCELLED
        );
        if (!validStatuses.contains(status)) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        order.setStatus(status);
        orderMapper.updateById(order);
        log.info("Order status updated: id={}, status={}", id, status);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        Long todayOrderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Orders>()
                        .ge(Orders::getCreateTime, todayStart)
                        .le(Orders::getCreateTime, todayEnd)
                        .ne(Orders::getStatus, Constants.ORDER_STATUS_CANCELLED));

        Long totalOrderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Orders>()
                        .ne(Orders::getStatus, Constants.ORDER_STATUS_CANCELLED));

        Long pendingOrderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getStatus, Constants.ORDER_STATUS_PENDING));

        List<Orders> todayOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .ge(Orders::getCreateTime, todayStart)
                        .le(Orders::getCreateTime, todayEnd)
                        .ne(Orders::getStatus, Constants.ORDER_STATUS_CANCELLED));
        BigDecimal todayRevenue = todayOrders.stream()
                .map(Orders::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Orders> allOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .ne(Orders::getStatus, Constants.ORDER_STATUS_CANCELLED));
        BigDecimal totalRevenue = allOrders.stream()
                .map(Orders::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardResponse.builder()
                .todayOrderCount(todayOrderCount)
                .todayRevenue(todayRevenue)
                .totalOrderCount(totalOrderCount)
                .totalRevenue(totalRevenue)
                .pendingOrderCount(pendingOrderCount)
                .build();
    }

    private LambdaQueryWrapper<Orders> buildQueryWrapper(Long userId, String status) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, Orders::getUserId, userId)
               .eq(StringUtils.hasText(status), Orders::getStatus, status)
               .orderByDesc(Orders::getCreateTime);
        return wrapper;
    }

    private OrderResponse toResponse(Orders order) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));

        String username = null;
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            username = user.getUsername();
        }

        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .specs(item.getSpecs())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .username(username)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .remark(order.getRemark())
                .items(itemResponses)
                .createTime(order.getCreateTime())
                .updateTime(order.getUpdateTime())
                .build();
    }
}
