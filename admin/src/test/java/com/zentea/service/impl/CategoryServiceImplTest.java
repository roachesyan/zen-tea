package com.zentea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zentea.common.exception.BusinessException;
import com.zentea.dto.request.CategoryCreateRequest;
import com.zentea.dto.request.CategoryUpdateRequest;
import com.zentea.dto.response.CategoryResponse;
import com.zentea.entity.Category;
import com.zentea.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryMapper categoryMapper;

    @Test
    void list_returnsCategories() {
        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("经典奶茶");
        cat1.setSort(1);
        cat1.setStatus(1);

        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cat1));

        List<CategoryResponse> result = categoryService.list();

        assertEquals(1, result.size());
        assertEquals("经典奶茶", result.get(0).getName());
    }

    @Test
    void create_success() {
        when(categoryMapper.insert(any(Category.class))).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            cat.setId(1L);
            return 1;
        });

        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("水果茶");
        request.setIcon("🍋");
        request.setSort(2);

        CategoryResponse response = categoryService.create(request);

        assertNotNull(response);
        assertEquals("水果茶", response.getName());
        assertEquals("🍋", response.getIcon());
        verify(categoryMapper).insert(any(Category.class));
    }

    @Test
    void update_success() {
        Category existing = new Category();
        existing.setId(1L);
        existing.setName("经典奶茶");

        when(categoryMapper.selectById(1L)).thenReturn(existing);
        when(categoryMapper.updateById(any(Category.class))).thenReturn(1);

        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("经典奶茶升级");

        CategoryResponse response = categoryService.update(1L, request);

        assertEquals("经典奶茶升级", response.getName());
    }

    @Test
    void update_notFound_throwsException() {
        when(categoryMapper.selectById(999L)).thenReturn(null);

        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("test");

        assertThrows(BusinessException.class, () -> categoryService.update(999L, request));
    }

    @Test
    void delete_success() {
        Category existing = new Category();
        existing.setId(1L);
        when(categoryMapper.selectById(1L)).thenReturn(existing);
        when(categoryMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> categoryService.delete(1L));
        verify(categoryMapper).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsException() {
        when(categoryMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> categoryService.delete(999L));
    }
}
