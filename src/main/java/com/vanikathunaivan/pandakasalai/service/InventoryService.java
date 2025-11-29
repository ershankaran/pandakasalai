package com.vanikathunaivan.pandakasalai.service;

import com.vanikathunaivan.pandakasalai.Exceptions.ConcurrentUpdateException;
import com.vanikathunaivan.pandakasalai.dto.ReservationRequest;
import com.vanikathunaivan.pandakasalai.events.InventoryUpdatedEvent;
import com.vanikathunaivan.pandakasalai.model.BinInventory;
import com.vanikathunaivan.pandakasalai.repository.BinInventoryRepo;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InventoryService {

    private final BinInventoryRepo binInventoryRepo;
    // Inject the new cache component
    private final InventoryCache inventoryCache;
    private final ApplicationEventPublisher applicationEventPublisher;

    public InventoryService(BinInventoryRepo binInventoryRepo, InventoryCache inventoryCache, ApplicationEventPublisher applicationEventPublisher) {
        this.binInventoryRepo = binInventoryRepo;
        this.inventoryCache = inventoryCache;
        this.applicationEventPublisher = applicationEventPublisher;
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

        BinInventory updatedInventory = binInventoryRepo.save(binInventory);


        // We use TransactionalEvent to ensure the event is fired ONLY on successful commit
 applicationEventPublisher.publishEvent(new InventoryUpdatedEvent(updatedInventory));

        // 5. CACHE SYNCHRONIZATION: Update the cache only AFTER a successful DB commit.
        // This is simple for now, but we'll introduce the event listener next for better decoupling.

        inventoryCache.updateCache(updatedInventory);



        return updatedInventory;


    }

    // --- New High-Performance Search Method ---
    public Optional<BinInventory> findInventoryByDetails(String skuId, int warehouseId, String binCode) {
        String key = String.format("%s-%d-%s", skuId, warehouseId, binCode);

        // Use the O(1) in-memory cache for the primary read operation
        Optional<BinInventory> cachedInventory = inventoryCache.getInventoryByKey(key);

        if (cachedInventory.isPresent()) {
            System.out.println("Inventory found in high-speed cache.");
            return cachedInventory;
        }

        // Fallback to database (e.g., if cache hasn't loaded a new item yet)
        System.out.println("Cache miss. Falling back to DB search.");
        return binInventoryRepo.findBySkuIdAndWarehouseIdAndBinCode(skuId, warehouseId, binCode);
    }
}
