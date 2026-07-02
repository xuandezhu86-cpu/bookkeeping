package com.youran.bookkeeping.service;

import com.youran.bookkeeping.dto.StatisticsVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StatisticsService {
    BigDecimal getMonthlyTotal(Long userId, String year, String month);
    BigDecimal getYearlyTotal(Long userId, String year);
    List<StatisticsVo> getCategorySummary(Long userId, String startDate, String endDate);
    List<Map<String, Object>> getDailyTrend(Long userId, String year, String month);
}
