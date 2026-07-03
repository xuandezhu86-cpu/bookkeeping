package com.youran.bookkeeping.service;

import com.youran.bookkeeping.dto.CategoryVo;
import com.youran.bookkeeping.entity.Category;

import java.util.List;

public interface CategoryService {
    List<CategoryVo> getCategoryTree(Long userId);
    List<Category> getParentCategories(Long userId);
    List<Category> getChildrenByParentId(Long parentId, Long userId);
    Category create(Category category, Long userId);
    Category update(Category category, Long userId);
    void delete(Long id, Long userId);
}
