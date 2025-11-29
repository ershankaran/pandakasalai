package com.vanikathunaivan.pandakasalai.repository;

import com.vanikathunaivan.pandakasalai.model.BinInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BinInventoryRepo extends JpaRepository<BinInventory, Long> {
    // Custom method to find the specific inventory row
    Optional<BinInventory> findBySkuIdAndWarehouseIdAndBinCode(String skuId, Integer warehouseId, String binCode);}
