package com.youran.bookkeeping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youran.bookkeeping.dto.StatisticsVo;
import com.youran.bookkeeping.entity.ExpenseRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ExpenseRecordMapper extends BaseMapper<ExpenseRecord> {

    @Select("SELECT c.name, SUM(e.amount) AS amount " +
            "FROM expense_record e " +
            "JOIN category c ON e.category_id = c.id " +
            "WHERE e.user_id = #{userId} " +
            "AND e.record_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY c.name " +
            "ORDER BY amount DESC")
    List<StatisticsVo> selectCategorySummary(@Param("userId") Long userId,
                                              @Param("startDate") String startDate,
                                              @Param("endDate") String endDate);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM expense_record " +
            "WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate}")
    BigDecimal selectTotalByDateRange(@Param("userId") Long userId,
                                      @Param("startDate") String startDate,
                                      @Param("endDate") String endDate);
}
