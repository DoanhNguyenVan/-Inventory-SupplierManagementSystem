package com.Group5.ISMS.dto;

public class DashboardStatsDto {

    private long totalProducts;
    private long totalSuppliers;
    private long lowStockCount;

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(long totalProducts, long totalSuppliers, long lowStockCount) {
        this.totalProducts = totalProducts;
        this.totalSuppliers = totalSuppliers;
        this.lowStockCount = lowStockCount;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getTotalSuppliers() {
        return totalSuppliers;
    }

    public void setTotalSuppliers(long totalSuppliers) {
        this.totalSuppliers = totalSuppliers;
    }

    public long getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }
}
