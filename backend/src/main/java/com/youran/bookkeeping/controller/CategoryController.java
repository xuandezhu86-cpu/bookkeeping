package com.youran.bookkeeping.controller;

import com.youran.bookkeeping.common.Result;
import com.youran.bookkeeping.dto.CategoryVo;
import com.youran.bookkeeping.entity.Category;
import com.youran.bookkeeping.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "分类管理")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    @Operation(summary = "获取分类树")
    public Result<List<CategoryVo>> getCategoryTree(Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return Result.success(categoryService.getCategoryTree(userId));
    }

    @GetMapping("/parents")
    @Operation(summary = "获取一级分类")
    public Result<List<Category>> getParentCategories(Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return Result.success(categoryService.getParentCategories(userId));
    }

    @GetMapping("/{parentId}/children")
    @Operation(summary = "获取二级分类")
    public Result<List<Category>> getChildren(@PathVariable Long parentId, Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return Result.success(categoryService.getChildrenByParentId(parentId, userId));
    }

    @PostMapping
    @Operation(summary = "创建自定义分类")
    public Result<Category> create(@RequestBody Category category, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(categoryService.create(category, userId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    public Result<Category> update(@PathVariable Long id, @RequestBody Category category,
                                    Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        category.setId(id);
        return Result.success(categoryService.update(category, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        categoryService.delete(id, userId);
        return Result.success();
    }
}
