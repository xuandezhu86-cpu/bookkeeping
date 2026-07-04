package com.youran.bookkeeping.statistics.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StatisticsVo {
    private String name;
    private BigDecimal amount;
    private BigDecimal percentage;
}
