package com.youran.bookkeeping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youran.bookkeeping.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT * FROM category WHERE parent_id IS NULL AND is_deleted = 0 ORDER BY sort_order")
    List<Category> selectParentCategories();

    @Select("SELECT * FROM category WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY sort_order")
    List<Category> selectChildrenByParentId(@Param("parentId") Long parentId);
}
