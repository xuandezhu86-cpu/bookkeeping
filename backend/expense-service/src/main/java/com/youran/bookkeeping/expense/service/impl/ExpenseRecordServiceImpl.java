package com.youran.bookkeeping.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youran.bookkeeping.common.BusinessException;
import com.youran.bookkeeping.common.PageParam;
import com.youran.bookkeeping.common.PageResult;
import com.youran.bookkeeping.expense.dto.ExpenseRecordDto;
import com.youran.bookkeeping.expense.entity.ExpenseRecord;
import com.youran.bookkeeping.expense.mapper.ExpenseRecordMapper;
import com.youran.bookkeeping.expense.service.ExpenseRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpenseRecordServiceImpl implements ExpenseRecordService {

    private final ExpenseRecordMapper expenseRecordMapper;

    @Override
    public PageResult<ExpenseRecord> getRecords(Long userId, Long categoryId, String startDate, String endDate, PageParam pageParam) {
        LambdaQueryWrapper<ExpenseRecord> query = new LambdaQueryWrapper<ExpenseRecord>()
                .eq(ExpenseRecord::getUserId, userId)
                .orderByDesc(ExpenseRecord::getRecordDate, ExpenseRecord::getCreatedAt);

        if (categoryId != null) {
            query.eq(ExpenseRecord::getCategoryId, categoryId);
        }
        if (startDate != null) {
            query.ge(ExpenseRecord::getRecordDate, startDate);
        }
        if (endDate != null) {
            query.le(ExpenseRecord::getRecordDate, endDate);
        }

        long total = expenseRecordMapper.selectCount(query);
        int offset = (pageParam.getPage() - 1) * pageParam.getSize();
        query.last("LIMIT " + pageParam.getSize() + " OFFSET " + offset);
        List<ExpenseRecord> records = expenseRecordMapper.selectList(query);
        long pages = (total + pageParam.getSize() - 1) / pageParam.getSize();
        return new PageResult<>(records, total, pages);
    }

    @Override
    public ExpenseRecord create(ExpenseRecordDto dto, Long userId) {
        ExpenseRecord record = new ExpenseRecord();
        record.setUserId(userId);
        record.setCategoryId(dto.getCategoryId());
        record.setAmount(dto.getAmount());
        record.setRecordDate(dto.getRecordDate());
        record.setNote(dto.getNote());
        expenseRecordMapper.insert(record);
        return record;
    }

    @Override
    public ExpenseRecord update(Long id, ExpenseRecordDto dto, Long userId) {
        ExpenseRecord record = expenseRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }
        record.setCategoryId(dto.getCategoryId());
        record.setAmount(dto.getAmount());
        record.setRecordDate(dto.getRecordDate());
        record.setNote(dto.getNote());
        expenseRecordMapper.updateById(record);
        return record;
    }

    @Override
    public void delete(Long id, Long userId) {
        ExpenseRecord record = expenseRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }
        expenseRecordMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getDailySummary(Long userId, String date) {
        BigDecimal total = expenseRecordMapper.selectTotalByDateRange(userId, date, date);
        long count = expenseRecordMapper.selectCount(
                new LambdaQueryWrapper<ExpenseRecord>()
                        .eq(ExpenseRecord::getUserId, userId)
                        .eq(ExpenseRecord::getRecordDate, date));

        Map<String, Object> result = new HashMap<>();
        result.put("total", total != null ? total : BigDecimal.ZERO);
        result.put("count", count);
        return result;
    }
}
