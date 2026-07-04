package com.youran.bookkeeping.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youran.bookkeeping.expense.entity.ExpenseRecord;
import com.youran.bookkeeping.common.PageResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ExpenseRecordMapper extends BaseMapper<ExpenseRecord> {

    @Select("SELECT COALESCE(SUM(amount), 0) FROM expense_record " +
            "WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate}")
    BigDecimal selectTotalByDateRange(@Param("userId") Long userId,
                                      @Param("startDate") String startDate,
                                      @Param("endDate") String endDate);
}
