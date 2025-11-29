package com.vanikathunaivan.pandakasalai.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;


public class ReservationRequest {
    @NotBlank
    private String skuId;
    @Min(1)
    private Integer quantity;
    @Min(1)
    private Integer warehouseId;
    @NotBlank
    private String binCode;

    public ReservationRequest() {
    }

    public ReservationRequest(Integer warehouseId, Integer quantity, String skuId, String binCode) {
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.skuId = skuId;
        this.binCode = binCode;
    }

    public String getSkuId() {
        return skuId;
    }
    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    public Integer getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Integer warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getBinCode() {
        return binCode;
    }

    public void setBinCode(String binCode) {
        this.binCode = binCode;
    }
}
