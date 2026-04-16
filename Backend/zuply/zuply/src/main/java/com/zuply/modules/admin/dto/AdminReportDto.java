package com.zuply.modules.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportDto {
    private BigDecimal totalSales;
    private long totalSellers;
    private long totalCustomers;
    private Map<String, Long> productsByCategory;
}