package com.vanikathunaivan.pandakasalai.service;

import com.vanikathunaivan.pandakasalai.Exceptions.ConcurrentUpdateException;
import com.vanikathunaivan.pandakasalai.dto.ReservationRequest;
import com.vanikathunaivan.pandakasalai.model.BinInventory;
import com.vanikathunaivan.pandakasalai.repository.BinInventoryRepo;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final BinInventoryRepo binInventoryRepo;

    public InventoryService(BinInventoryRepo binInventoryRepo) {
        this.binInventoryRepo = binInventoryRepo;
    }

    @Transactional
    public BinInventory processReservation(ReservationRequest request){
        // 1. Find the specific bin inventory record (READ
        BinInventory binInventory = binInventoryRepo
                .findBySkuIdAndWarehouseIdAndBinCode (request.getSkuId(), request.getWarehouseId(), request.getBinCode())
                .orElseThrow(() -> new RuntimeException(" Inventory not found"));

        //2. Check availability
        if(binInventory.getAvailableQty() < request.getQuantity()) {
            throw new RuntimeException("Insufficient inventory");
        }

        // 3. update inventory values
        binInventory.setAvailableQty(binInventory.getAvailableQty() - request.getQuantity());
        binInventory.setReservedQty(binInventory.getReservedQty() + request.getQuantity());


            // 4. SAVE (COMMIT) - JPA checks the old 'version' against the database 'version'.
            // If they match, it updates the row AND increments the 'version' column.
            // If they don't match, an OptimisticLockException occurs!

            return binInventoryRepo.save(binInventory);

    }
}
