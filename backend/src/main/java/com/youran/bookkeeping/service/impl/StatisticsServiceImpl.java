package com.youran.bookkeeping.service.impl;

import com.youran.bookkeeping.dto.StatisticsVo;
import com.youran.bookkeeping.mapper.ExpenseRecordMapper;
import com.youran.bookkeeping.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final ExpenseRecordMapper expenseRecordMapper;

    @Override
    public BigDecimal getMonthlyTotal(Long userId, String year, String month) {
        String monthPadded = month.length() == 1 ? "0" + month : month;
        String startDate = year + "-" + monthPadded + "-01";
        String endDate = year + "-" + monthPadded + "-31";
        BigDecimal total = expenseRecordMapper.selectTotalByDateRange(userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getYearlyTotal(Long userId, String year) {
        String startDate = year + "-01-01";
        String endDate = year + "-12-31";
        BigDecimal total = expenseRecordMapper.selectTotalByDateRange(userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public List<StatisticsVo> getCategorySummary(Long userId, String startDate, String endDate) {
        BigDecimal grandTotal = expenseRecordMapper.selectTotalByDateRange(userId, startDate, endDate);
        List<StatisticsVo> list = expenseRecordMapper.selectCategorySummary(userId, startDate, endDate);
        if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
            for (StatisticsVo vo : list) {
                vo.setPercentage(vo.getAmount().multiply(BigDecimal.valueOf(100))
                        .divide(grandTotal, 2, RoundingMode.HALF_UP));
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getDailyTrend(Long userId, String year, String month) {
        String monthPadded = month.length() == 1 ? "0" + month : month;
        String startDate = year + "-" + monthPadded + "-01";
        String endDate = year + "-" + monthPadded + "-31";

        // Use a list-based approach to get daily totals
        List<Map<String, Object>> trend = new ArrayList<>();
        int daysInMonth = java.time.YearMonth.of(Integer.parseInt(year), Integer.parseInt(month)).lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            String date = year + "-" + monthPadded + "-" + String.format("%02d", day);
            BigDecimal dailyTotal = expenseRecordMapper.selectTotalByDateRange(userId, date, date);
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", date);
            entry.put("amount", dailyTotal != null ? dailyTotal : BigDecimal.ZERO);
            trend.add(entry);
        }
        return trend;
    }
}
