package com.zentea.service.impl;

import com.zentea.common.constant.Constants;
import com.zentea.common.exception.BusinessException;
import com.zentea.common.result.ResultCode;
import com.zentea.dto.request.OrderCreateRequest;
import com.zentea.dto.request.OrderItemCreateRequest;
import com.zentea.dto.response.DashboardResponse;
import com.zentea.dto.response.OrderResponse;
import com.zentea.entity.OrderItem;
import com.zentea.entity.Orders;
import com.zentea.entity.Product;
import com.zentea.entity.User;
import com.zentea.mapper.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserMapper userMapper;

    private Product createProduct(Long id, String name, BigDecimal price) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(price);
        p.setStatus(Constants.PRODUCT_ON_SHELF);
        return p;
    }

    @Test
    void create_success() {
        Product product = createProduct(1L, "珍珠奶茶", new BigDecimal("12.00"));
        when(productMapper.selectBatchIds(anyCollection())).thenReturn(List.of(product));
        when(orderMapper.insert(any(Orders.class))).thenAnswer(invocation -> {
            Orders o = invocation.getArgument(0);
            o.setId(1L);
            return 1;
        });
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);

        OrderCreateRequest request = new OrderCreateRequest();
        OrderItemCreateRequest item = new OrderItemCreateRequest();
        item.setProductId(1L);
        item.setQuantity(2);
        request.setItems(List.of(item));
        request.setRemark("少冰");

        OrderResponse response = orderService.create(1L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("24.00"), response.getTotalAmount());
        assertEquals(Constants.ORDER_STATUS_PENDING, response.getStatus());
        verify(orderMapper).insert(any(Orders.class));
        verify(orderItemMapper, times(1)).insert(any(OrderItem.class));
    }

    @Test
    void create_productOffShelf_throwsException() {
        Product product = createProduct(1L, "珍珠奶茶", new BigDecimal("12.00"));
        product.setStatus(Constants.PRODUCT_OFF_SHELF);
        when(productMapper.selectBatchIds(anyCollection())).thenReturn(List.of(product));

        OrderCreateRequest request = new OrderCreateRequest();
        OrderItemCreateRequest item = new OrderItemCreateRequest();
        item.setProductId(1L);
        item.setQuantity(1);
        request.setItems(List.of(item));

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.create(1L, request));
        assertEquals(ResultCode.PRODUCT_OFF_SHELF, ex.getResultCode());
    }

    @Test
    void getById_success() {
        Orders order = new Orders();
        order.setId(1L);
        order.setOrderNo("ZN202604141234560001");
        order.setUserId(1L);
        order.setTotalAmount(new BigDecimal("24.00"));
        order.setStatus(Constants.ORDER_STATUS_PENDING);

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderItemMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(userMapper.selectById(1L)).thenReturn(new User());

        OrderResponse response = orderService.getById(1L);

        assertNotNull(response);
        assertEquals("ZN202604141234560001", response.getOrderNo());
    }

    @Test
    void getById_notFound_throwsException() {
        when(orderMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> orderService.getById(999L));
    }

    @Test
    void updateStatus_success() {
        Orders order = new Orders();
        order.setId(1L);
        order.setStatus(Constants.ORDER_STATUS_PENDING);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.updateById(any(Orders.class))).thenReturn(1);

        assertDoesNotThrow(() -> orderService.updateStatus(1L, Constants.ORDER_STATUS_MAKING));
        verify(orderMapper).updateById(any(Orders.class));
    }

    @Test
    void updateStatus_invalidStatus_throwsException() {
        Orders order = new Orders();
        order.setId(1L);
        order.setStatus(Constants.ORDER_STATUS_PENDING);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> orderService.updateStatus(1L, "INVALID"));
    }

    @Test
    void getDashboardStats_returnsStats() {
        when(orderMapper.selectCount(any())).thenReturn(5L);
        when(orderMapper.selectList(any())).thenReturn(Collections.emptyList());

        DashboardResponse stats = orderService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(0, stats.getTodayRevenue().compareTo(BigDecimal.ZERO));
        assertEquals(5L, stats.getTotalOrderCount());
    }
}
