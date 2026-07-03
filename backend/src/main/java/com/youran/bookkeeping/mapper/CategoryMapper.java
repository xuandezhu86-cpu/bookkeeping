package com.youran.bookkeeping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youran.bookkeeping.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT * FROM category WHERE parent_id IS NULL AND is_deleted = 0 AND (user_id IS NULL OR user_id = #{userId}) ORDER BY sort_order")
    List<Category> selectParentCategories(@Param("userId") Long userId);

    @Select("SELECT * FROM category WHERE parent_id = #{parentId} AND is_deleted = 0 AND (user_id IS NULL OR user_id = #{userId}) ORDER BY sort_order")
    List<Category> selectChildrenByParentId(@Param("parentId") Long parentId, @Param("userId") Long userId);
}
