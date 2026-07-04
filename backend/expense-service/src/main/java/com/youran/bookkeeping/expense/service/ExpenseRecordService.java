package com.youran.bookkeeping.expense.service;

import com.youran.bookkeeping.common.PageParam;
import com.youran.bookkeeping.common.PageResult;
import com.youran.bookkeeping.expense.dto.ExpenseRecordDto;
import com.youran.bookkeeping.expense.entity.ExpenseRecord;

import java.util.Map;

public interface ExpenseRecordService {
    PageResult<ExpenseRecord> getRecords(Long userId, Long categoryId, String startDate, String endDate, PageParam pageParam);
    ExpenseRecord create(ExpenseRecordDto dto, Long userId);
    ExpenseRecord update(Long id, ExpenseRecordDto dto, Long userId);
    void delete(Long id, Long userId);
    Map<String, Object> getDailySummary(Long userId, String date);
}
