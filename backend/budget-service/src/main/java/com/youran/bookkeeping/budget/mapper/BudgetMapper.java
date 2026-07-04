package com.youran.bookkeeping.budget.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youran.bookkeeping.budget.entity.Budget;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BudgetMapper extends BaseMapper<Budget> {
}
