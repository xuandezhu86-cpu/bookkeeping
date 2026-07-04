package com.youran.bookkeeping.category.dto;

import lombok.Data;
import java.util.List;

@Data
public class CategoryVo {
    private Long id;
    private String name;
    private String icon;
    private Integer sortOrder;
    private List<CategoryVo> children;
}
