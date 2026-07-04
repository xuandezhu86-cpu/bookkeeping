package com.youran.bookkeeping.expense.controller;

import com.youran.bookkeeping.common.PageParam;
import com.youran.bookkeeping.common.PageResult;
import com.youran.bookkeeping.common.Result;
import com.youran.bookkeeping.expense.dto.ExpenseRecordDto;
import com.youran.bookkeeping.expense.entity.ExpenseRecord;
import com.youran.bookkeeping.expense.service.ExpenseRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "消费记录")
public class ExpenseRecordController {

    private final ExpenseRecordService expenseRecordService;

    @GetMapping
    @Operation(summary = "获取消费记录列表")
    public Result<PageResult<ExpenseRecord>> getRecords(
            Authentication auth,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            PageParam pageParam) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(expenseRecordService.getRecords(userId, categoryId, startDate, endDate, pageParam));
    }

    @PostMapping
    @Operation(summary = "新增消费记录")
    public Result<ExpenseRecord> create(@Valid @RequestBody ExpenseRecordDto dto, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(expenseRecordService.create(dto, userId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新消费记录")
    public Result<ExpenseRecord> update(@PathVariable Long id, @Valid @RequestBody ExpenseRecordDto dto,
                                         Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(expenseRecordService.update(id, dto, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除消费记录")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        expenseRecordService.delete(id, userId);
        return Result.success();
    }

    @GetMapping("/daily-summary")
    @Operation(summary = "获取日汇总")
    public Result<Map<String, Object>> getDailySummary(Authentication auth, @RequestParam String date) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(expenseRecordService.getDailySummary(userId, date));
    }
}
