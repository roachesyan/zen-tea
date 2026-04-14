package com.zentea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zentea.common.exception.BusinessException;
import com.zentea.dto.request.ProductCreateRequest;
import com.zentea.dto.request.ProductUpdateRequest;
import com.zentea.dto.response.ProductResponse;
import com.zentea.entity.Category;
import com.zentea.entity.Product;
import com.zentea.mapper.CategoryMapper;
import com.zentea.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryMapper categoryMapper;

    private Product createProduct(Long id, String name, BigDecimal price) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(price);
        p.setCategoryId(1L);
        p.setStatus(1);
        p.setSort(1);
        return p;
    }

    @Test
    void page_returnsPagedProducts() {
        Product product = createProduct(1L, "珍珠奶茶", new BigDecimal("12.00"));
        Page<Product> page = new Page<>(1, 10);
        page.setRecords(List.of(product));
        page.setTotal(1);

        when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(categoryMapper.selectById(1L)).thenReturn(new Category());

        var result = productService.page(1, 10, null, null, null);

        assertEquals(1, result.getTotal());
        assertEquals("珍珠奶茶", result.getRecords().get(0).getName());
    }

    @Test
    void getById_success() {
        Product product = createProduct(1L, "珍珠奶茶", new BigDecimal("12.00"));
        when(productMapper.selectById(1L)).thenReturn(product);
        when(categoryMapper.selectById(1L)).thenReturn(new Category());

        ProductResponse response = productService.getById(1L);

        assertEquals("珍珠奶茶", response.getName());
    }

    @Test
    void getById_notFound_throwsException() {
        when(productMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> productService.getById(999L));
    }

    @Test
    void create_success() {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("经典奶茶");
        when(categoryMapper.selectById(1L)).thenReturn(cat);
        when(productMapper.insert(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(1L);
            return 1;
        });

        ProductCreateRequest request = new ProductCreateRequest();
        request.setCategoryId(1L);
        request.setName("珍珠奶茶");
        request.setPrice(new BigDecimal("12.00"));

        ProductResponse response = productService.create(request);

        assertNotNull(response);
        assertEquals("珍珠奶茶", response.getName());
    }

    @Test
    void create_categoryNotFound_throwsException() {
        when(categoryMapper.selectById(999L)).thenReturn(null);

        ProductCreateRequest request = new ProductCreateRequest();
        request.setCategoryId(999L);
        request.setName("test");
        request.setPrice(new BigDecimal("10.00"));

        assertThrows(BusinessException.class, () -> productService.create(request));
    }

    @Test
    void update_success() {
        Product existing = createProduct(1L, "珍珠奶茶", new BigDecimal("12.00"));
        when(productMapper.selectById(1L)).thenReturn(existing);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setPrice(new BigDecimal("14.00"));

        ProductResponse response = productService.update(1L, request);
        assertEquals(new BigDecimal("14.00"), response.getPrice());
    }

    @Test
    void updateStatus_success() {
        Product product = createProduct(1L, "test", BigDecimal.TEN);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        assertDoesNotThrow(() -> productService.updateStatus(1L, 0));
    }

    @Test
    void delete_success() {
        Product product = createProduct(1L, "test", BigDecimal.TEN);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(productMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> productService.delete(1L));
        verify(productMapper).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsException() {
        when(productMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> productService.delete(999L));
    }
}
