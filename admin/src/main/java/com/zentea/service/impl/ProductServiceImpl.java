package com.zentea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zentea.common.exception.BusinessException;
import com.zentea.common.result.ResultCode;
import com.zentea.dto.request.ProductCreateRequest;
import com.zentea.dto.request.ProductUpdateRequest;
import com.zentea.dto.response.ProductResponse;
import com.zentea.entity.Category;
import com.zentea.entity.Product;
import com.zentea.mapper.CategoryMapper;
import com.zentea.mapper.ProductMapper;
import com.zentea.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public IPage<ProductResponse> page(int pageNum, int pageSize, Long categoryId, String keyword, Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, Product::getCategoryId, categoryId)
               .eq(status != null, Product::getStatus, status)
               .like(StringUtils.hasText(keyword), Product::getName, keyword)
               .orderByAsc(Product::getSort)
               .orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = productMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return productPage.convert(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> listByCategory(Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .eq(categoryId != null, Product::getCategoryId, categoryId)
               .orderByAsc(Product::getSort);
        return productMapper.selectList(wrapper).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        Category category = categoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        Product product = new Product();
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setDescription(request.getDescription());
        product.setStatus(1);
        product.setSort(request.getSort() != null ? request.getSort() : 0);
        productMapper.insert(product);

        log.info("Product created: {}", product.getName());
        return toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (request.getCategoryId() != null) {
            product.setCategoryId(request.getCategoryId());
        }
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        if (request.getSort() != null) {
            product.setSort(request.getSort());
        }
        productMapper.updateById(product);
        log.info("Product updated: id={}", id);
        return toResponse(product);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        product.setStatus(status);
        productMapper.updateById(product);
        log.info("Product status updated: id={}, status={}", id, status);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        productMapper.deleteById(id);
        log.info("Product deleted: id={}", id);
    }

    private ProductResponse toResponse(Product product) {
        Category category = categoryMapper.selectById(product.getCategoryId());
        return ProductResponse.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .categoryName(category != null ? category.getName() : null)
                .name(product.getName())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .description(product.getDescription())
                .status(product.getStatus())
                .sort(product.getSort())
                .createTime(product.getCreateTime())
                .updateTime(product.getUpdateTime())
                .build();
    }
}
