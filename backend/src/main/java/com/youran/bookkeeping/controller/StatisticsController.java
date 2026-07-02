package com.youran.bookkeeping.controller;

import com.youran.bookkeeping.common.Result;
import com.youran.bookkeeping.dto.StatisticsVo;
import com.youran.bookkeeping.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "数据统计")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/monthly-total")
    @Operation(summary = "月度总支出")
    public Result<BigDecimal> getMonthlyTotal(Authentication auth,
                                               @RequestParam String year,
                                               @RequestParam String month) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(statisticsService.getMonthlyTotal(userId, year, month));
    }

    @GetMapping("/yearly-total")
    @Operation(summary = "年度总支出")
    public Result<BigDecimal> getYearlyTotal(Authentication auth, @RequestParam String year) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(statisticsService.getYearlyTotal(userId, year));
    }

    @GetMapping("/category-summary")
    @Operation(summary = "分类支出汇总")
    public Result<List<StatisticsVo>> getCategorySummary(Authentication auth,
                                                          @RequestParam String startDate,
                                                          @RequestParam String endDate) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(statisticsService.getCategorySummary(userId, startDate, endDate));
    }

    @GetMapping("/daily-trend")
    @Operation(summary = "每日支出趋势")
    public Result<List<Map<String, Object>>> getDailyTrend(Authentication auth,
                                                            @RequestParam String year,
                                                            @RequestParam String month) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(statisticsService.getDailyTrend(userId, year, month));
    }
}
