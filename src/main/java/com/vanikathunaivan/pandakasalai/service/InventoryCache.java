package com.vanikathunaivan.pandakasalai.service;

import com.vanikathunaivan.pandakasalai.events.InventoryUpdatedEvent;
import com.vanikathunaivan.pandakasalai.model.BinInventory;
import com.vanikathunaivan.pandakasalai.repository.BinInventoryRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the in-memory cache for fast, read-heavy inventory lookups (DSA Challenge).
 */
@Component
public class InventoryCache {

    private final BinInventoryRepo binInventoryRepo;

    // Key: Combination of SKU-WarehouseId-BinCode (e.g., "SKU-123-W1-B1")
    // Value: The BinInventory object (contains availableQty, reservedQty, version, etc.)
    private final ConcurrentHashMap<String, BinInventory> inventoryMap = new ConcurrentHashMap<>();

    public InventoryCache(BinInventoryRepo binInventoryRepo) {
        this.binInventoryRepo = binInventoryRepo;
    }

    /**
     * Step 1: Cache Loading (Executed once on startup)
     * Loads all existing inventory data from the DB into the in-memory structure.
     */
    @PostConstruct
    public void loadCache() {
        System.out.println("--- Loading Inventory Cache from Database... ---");
        // Load all data from the database
        binInventoryRepo.findAll().forEach(inventory -> {
            String key = generateKey(inventory);
            inventoryMap.put(key, inventory);
        });
        System.out.println("--- Inventory Cache Loaded: " + inventoryMap.size() + " records. ---");
    }

    // --- Public Access Methods ---

    public Optional<BinInventory> getInventoryByKey(String key) {
        // O(1) time complexity for exact lookup
        return Optional.ofNullable(inventoryMap.get(key));
    }

    public void updateCache(BinInventory inventory) {
        // Used for synchronization after a successful transaction commit
        String key = generateKey(inventory);
        inventoryMap.put(key, inventory);
        System.out.println("Cache updated for key: " + key + ". New Available Qty: " + inventory.getAvailableQty());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInventoryUpdate(InventoryUpdatedEvent event) {
        // This method is called ONLY AFTER the DB transaction commits
        updateCache(event.getInventory());
    }

    // --- Helper Method ---

    public static String generateKey(BinInventory inventory) {
        return String.format("%s-%d-%s",
                inventory.getSkuId(),
                inventory.getWarehouseId(),
                inventory.getBinCode()
        );
    }
}
