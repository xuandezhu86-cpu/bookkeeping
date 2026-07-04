package com.youran.bookkeeping.budget.service;

import com.youran.bookkeeping.budget.entity.Budget;

import java.util.List;

public interface BudgetService {
    List<Budget> getBudgets(Long userId, String month);
    Budget setBudget(Budget budget, Long userId);
    void deleteBudget(Long id, Long userId);
}
