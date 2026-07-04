package com.youran.bookkeeping.budget.controller;

import com.youran.bookkeeping.budget.entity.Budget;
import com.youran.bookkeeping.budget.service.BudgetService;
import com.youran.bookkeeping.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "预算管理")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    @Operation(summary = "获取预算列表")
    public Result<List<Budget>> getBudgets(Authentication auth, @RequestParam String month) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(budgetService.getBudgets(userId, month));
    }

    @PostMapping
    @Operation(summary = "设置预算")
    public Result<Budget> setBudget(@RequestBody Budget budget, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        // 清除客户端可能传入的 id 字段
        budget.setId(null);
        return Result.success(budgetService.setBudget(budget, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除预算")
    public Result<Void> deleteBudget(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        budgetService.deleteBudget(id, userId);
        return Result.success();
    }
}
