package com.youran.bookkeeping.category.service.impl;

import com.youran.bookkeeping.category.dto.CategoryVo;
import com.youran.bookkeeping.category.entity.Category;
import com.youran.bookkeeping.category.mapper.CategoryMapper;
import com.youran.bookkeeping.category.service.CategoryService;
import com.youran.bookkeeping.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVo> getCategoryTree(Long userId) {
        List<Category> parents = categoryMapper.selectParentCategories(userId);
        return parents.stream().map(parent -> {
            CategoryVo vo = new CategoryVo();
            vo.setId(parent.getId());
            vo.setName(parent.getName());
            vo.setIcon(parent.getIcon());
            vo.setSortOrder(parent.getSortOrder());
            List<Category> children = categoryMapper.selectChildrenByParentId(parent.getId(), userId);
            vo.setChildren(children.stream().map(child -> {
                CategoryVo childVo = new CategoryVo();
                childVo.setId(child.getId());
                childVo.setName(child.getName());
                childVo.setIcon(child.getIcon());
                childVo.setSortOrder(child.getSortOrder());
                return childVo;
            }).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Category> getParentCategories(Long userId) {
        return categoryMapper.selectParentCategories(userId);
    }

    @Override
    public List<Category> getChildrenByParentId(Long parentId, Long userId) {
        return categoryMapper.selectChildrenByParentId(parentId, userId);
    }

    @Override
    public Category create(Category category, Long userId) {
        category.setUserId(userId);
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category update(Category category, Long userId) {
        Category existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new BusinessException(404, "分类不存在");
        }
        // 系统预设分类不可修改
        if (existing.getUserId() == null) {
            throw new BusinessException(403, "系统预设分类不可修改");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改此分类");
        }
        existing.setName(category.getName());
        existing.setIcon(category.getIcon());
        if (category.getSortOrder() != null) existing.setSortOrder(category.getSortOrder());
        if (category.getParentId() != null) existing.setParentId(category.getParentId());
        categoryMapper.updateById(existing);
        return existing;
    }

    @Override
    public void delete(Long id, Long userId) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (existing.getUserId() == null) {
            throw new BusinessException(403, "系统预设分类不可删除");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此分类");
        }
        categoryMapper.deleteById(id);
    }
}
