package com.ecommerce.order.dtos;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryPerformanceDTO {
    private List<CategoryData> categories;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryData {
        private String categoryId;
        private String categoryName;
        private BigDecimal revenue;
        private Long orders;
        private Long units;
        private BigDecimal percentageOfTotal;
    }
}
