package com.vanikathunaivan.pandakasalai.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bin_inventory")
public class BinInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String skuId;

    private Integer warehouseId;

    private String binCode;

    // CRITICAL: The @Version field for Optimistic Locking
    @Version
    private Long version;

    private Integer availableQty;

    private Integer reservedQty;

    public BinInventory() {
    }

    public BinInventory(String skuId, Long id, Integer warehouseId, String binCode, Long version, Integer availableQty, Integer reservedQty) {
        this.skuId = skuId;
        this.id = id;
        this.warehouseId = warehouseId;
        this.binCode = binCode;
        this.version = version;
        this.availableQty = availableQty;
        this.reservedQty = reservedQty;
    }

    public Integer getReservedQty() {
        return reservedQty;
    }

    public void setReservedQty(Integer reservedQty) {
        this.reservedQty = reservedQty;
    }

    public Integer getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(Integer availableQty) {
        this.availableQty = availableQty;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getBinCode() {
        return binCode;
    }

    public void setBinCode(String binCode) {
        this.binCode = binCode;
    }

    public Integer getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Integer warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
