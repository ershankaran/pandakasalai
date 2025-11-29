package com.vanikathunaivan.pandakasalai.service;

import com.vanikathunaivan.pandakasalai.dto.ReservationRequest;
import com.vanikathunaivan.pandakasalai.model.BinInventory;
import com.vanikathunaivan.pandakasalai.repository.BinInventoryRepo;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private BinInventoryRepo binInventoryRepo;

    @InjectMocks
    private InventoryService inventoryService;

    // --- Setup Helper ---
    private BinInventory setupInventory(int availableQty, int reservedQty) {
        BinInventory inventory = new BinInventory();
        inventory.setSkuId("SKU-TEST");
        inventory.setWarehouseId(1);
        inventory.setBinCode("W1-B1");
        inventory.setAvailableQty(availableQty);
        inventory.setReservedQty(reservedQty);
        // Version is not critical for unit test logic, only for JPA runtime
        return inventory;
    }

    // --- Test Cases ---

    @Test
    void shouldSuccessfullyProcessReservation() {
        // Arrange
        int initialAvailable = 20;
        int requestedQuantity = 5;
        BinInventory inventory = setupInventory(initialAvailable, 0);
        ReservationRequest request = new ReservationRequest();
        request.setSkuId("SKU-TEST");
        request.setQuantity(requestedQuantity);

        // Mocking the successful find
        when(binInventoryRepo.findBySkuIdAndWarehouseIdAndBinCode(any(), any(), any()))
                .thenReturn(Optional.of(inventory));

        // Mocking the save call to return the updated object
        when(binInventoryRepo.save(any(BinInventory.class)))
                .thenReturn(inventory);

        // Act
        BinInventory result = inventoryService.processReservation(request);

        // Assert
        assertEquals(initialAvailable - requestedQuantity, result.getAvailableQty(), "Available quantity should be reduced.");
        assertEquals(requestedQuantity, result.getReservedQty(), "Reserved quantity should be increased.");
        verify(binInventoryRepo, times(1)).save(inventory);
    }

    @Test
    void shouldThrowExceptionWhenInventoryNotFound() {
        // Arrange
        ReservationRequest request = new ReservationRequest();
        request.setSkuId("SKU-MISSING");
        request.setQuantity(1);

        // Mocking the not found scenario
        when(binInventoryRepo.findBySkuIdAndWarehouseIdAndBinCode(any(), any(), any()))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            inventoryService.processReservation(request);
        });

        assertTrue(thrown.getMessage().contains("Inventory not found"), "Should throw 'Inventory not found' exception.");
        verify(binInventoryRepo, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientStock() {
        // Arrange
        int initialAvailable = 5;
        int requestedQuantity = 10; // Request more than available
        BinInventory inventory = setupInventory(initialAvailable, 0);
        ReservationRequest request = new ReservationRequest();
        request.setQuantity(requestedQuantity);

        when(binInventoryRepo.findBySkuIdAndWarehouseIdAndBinCode(any(), any(), any()))
                .thenReturn(Optional.of(inventory));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            inventoryService.processReservation(request);
        });

        assertTrue(thrown.getMessage().contains("Insufficient inventory"), "Should throw 'Insufficient inventory' exception.");
        verify(binInventoryRepo, never()).save(any());
    }
}