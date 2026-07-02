package com.youran.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youran.bookkeeping.common.BusinessException;
import com.youran.bookkeeping.entity.Budget;
import com.youran.bookkeeping.mapper.BudgetMapper;
import com.youran.bookkeeping.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetMapper budgetMapper;

    @Override
    public List<Budget> getBudgets(Long userId, String month) {
        return budgetMapper.selectList(
                new LambdaQueryWrapper<Budget>()
                        .eq(Budget::getUserId, userId)
                        .eq(Budget::getMonth, month));
    }

    @Override
    public Budget setBudget(Budget budget, Long userId) {
        budget.setUserId(userId);
        Budget existing = budgetMapper.selectOne(
                new LambdaQueryWrapper<Budget>()
                        .eq(Budget::getUserId, userId)
                        .eq(Budget::getMonth, budget.getMonth())
                        .eq(budget.getCategoryId() != null, Budget::getCategoryId, budget.getCategoryId()));
        if (existing != null) {
            existing.setAmount(budget.getAmount());
            budgetMapper.updateById(existing);
            return existing;
        }
        budgetMapper.insert(budget);
        return budget;
    }

    @Override
    public void deleteBudget(Long id, Long userId) {
        Budget budget = budgetMapper.selectById(id);
        if (budget == null || !budget.getUserId().equals(userId)) {
            throw new BusinessException("预算不存在");
        }
        budgetMapper.deleteById(id);
    }
}
