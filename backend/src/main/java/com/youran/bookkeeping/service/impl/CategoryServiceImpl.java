package com.youran.bookkeeping.service.impl;

import com.youran.bookkeeping.dto.CategoryVo;
import com.youran.bookkeeping.entity.Category;
import com.youran.bookkeeping.mapper.CategoryMapper;
import com.youran.bookkeeping.service.CategoryService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVo> getCategoryTree(Long userId) {
        List<Category> parents = categoryMapper.selectParentCategories();
        return parents.stream().map(parent -> {
            CategoryVo vo = new CategoryVo();
            vo.setId(parent.getId());
            vo.setName(parent.getName());
            vo.setIcon(parent.getIcon());
            vo.setSortOrder(parent.getSortOrder());

            List<Category> children = categoryMapper.selectChildrenByParentId(parent.getId());
            if (userId != null) {
                List<Category> userChildren = categoryMapper.selectList(
                        Wrappers.<Category>lambdaQuery()
                                .eq(Category::getParentId, parent.getId())
                                .eq(Category::getUserId, userId));
                children.addAll(userChildren);
            }

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
        return categoryMapper.selectParentCategories();
    }

    @Override
    public List<Category> getChildrenByParentId(Long parentId, Long userId) {
        return categoryMapper.selectChildrenByParentId(parentId);
    }

    @Override
    public Category create(Category category, Long userId) {
        category.setUserId(userId);
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category update(Category category) {
        categoryMapper.updateById(category);
        return category;
    }

    @Override
    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
