package com.vanikathunaivan.pandakasalai.Controller;

import com.vanikathunaivan.pandakasalai.dto.ReservationRequest;
import com.vanikathunaivan.pandakasalai.model.BinInventory;
import com.vanikathunaivan.pandakasalai.repository.BinInventoryRepo;
import com.vanikathunaivan.pandakasalai.service.InventoryCache;
import com.vanikathunaivan.pandakasalai.service.InventoryService;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class CacheSynchronizationTest {
    @Autowired
    private InventoryService inventoryService;

    // Inject and Spy the Cache component
    @SpyBean
    private InventoryCache inventoryCache;

    @MockBean
    private BinInventoryRepo binInventoryRepo;

    // Helper method to create a valid reservation request and inventory object
    private ReservationRequest getValidRequest() { return new ReservationRequest(
            1,            // warehouseId (must be >= 1)
            5,            // quantity (must be >= 1)
            "SKU-PROD-A", // skuId (must be @NotBlank)
            "BIN-001"     // binCode (must be @NotBlank)
    );}
    private BinInventory getInitialInventory() { return new BinInventory(
            "SKU-PROD-A",   // skuId
            1L,             // id
            1,              // warehouseId
            "BIN-001",      // binCode
            0L,             // version (CRITICAL: Starting version 0 for Optimistic Locking)
            50,             // availableQty (sufficient stock)
            0               // reservedQty
    ); }


    /**
     * Test 1: Successful Reservation - Cache MUST Update
     */
    @Test
    void cache_ShouldUpdate_OnSuccessfulTransaction() {
        // Setup: Mock the repository to return a valid object on find, and return the updated object on save
        BinInventory inventory = getInitialInventory();
        when(binInventoryRepo.findBySkuIdAndWarehouseIdAndBinCode(any(), anyInt(), any()))
                .thenReturn(Optional.of(inventory));
        when(binInventoryRepo.save(any(BinInventory.class))).thenReturn(inventory); // Successful save simulation

        // Execute: Process the reservation
        inventoryService.processReservation(getValidRequest());

        // Verify: The cache update method must be called exactly once
        verify(inventoryCache, times(1)).updateCache(any(BinInventory.class));
    }


    /**
     * Test 2: Failed Reservation - Cache MUST NOT Update
     */
    @Test
    void cache_ShouldNotUpdate_OnTransactionRollback_OptimisticLockFailure() {
        // Setup: Mock the repository to simulate an Optimistic Lock failure during save
        BinInventory inventory = getInitialInventory();
        when(binInventoryRepo.findBySkuIdAndWarehouseIdAndBinCode(any(), anyInt(), any()))
                .thenReturn(Optional.of(inventory));

        // Mock the 'save' call to throw the exception that causes the rollback
        when(binInventoryRepo.save(any(BinInventory.class)))
                .thenThrow(new OptimisticLockException("Simulated Lock Failure"));

        // Execute: Call the service, expecting the exception to propagate
        assertThrows(OptimisticLockException.class, () -> {
            inventoryService.processReservation(getValidRequest());
        });

        // Verify: The cache update method must NOT be called (called zero times)
        verify(inventoryCache, times(0)).updateCache(any(BinInventory.class));
    }
}