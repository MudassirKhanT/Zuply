package com.zuply.modules.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class AdminReportDto {
    private BigDecimal totalRevenue;
    private long totalSellers;
    private long totalCustomers;
    private long totalOrders;
    private List<CategoryStat> categoryBreakdown;

    public AdminReportDto(BigDecimal totalRevenue, long totalSellers, long totalCustomers,
                          long totalOrders, List<CategoryStat> categoryBreakdown) {
        this.totalRevenue      = totalRevenue;
        this.totalSellers      = totalSellers;
        this.totalCustomers    = totalCustomers;
        this.totalOrders       = totalOrders;
        this.categoryBreakdown = categoryBreakdown;
    }

    @Data
    public static class CategoryStat {
        private String category;
        private long count;
        private double percentage;

        public CategoryStat(String category, long count, double percentage) {
            this.category   = category;
            this.count      = count;
            this.percentage = percentage;
        }
    }
}
