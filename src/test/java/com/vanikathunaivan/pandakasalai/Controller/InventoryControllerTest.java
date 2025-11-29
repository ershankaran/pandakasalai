package com.vanikathunaivan.pandakasalai.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanikathunaivan.pandakasalai.dto.ReservationRequest;
import com.vanikathunaivan.pandakasalai.model.BinInventory;
import com.vanikathunaivan.pandakasalai.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Use WebMvcTest to focus only on Spring MVC components (Controller, Exception Handler)
@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock the service layer, as we only want to test the controller's behavior
    @MockBean
    private InventoryService inventoryService;

    // --- Setup Helper ---
    private ReservationRequest createRequest() {
        ReservationRequest request = new ReservationRequest();
        request.setSkuId("SKU-123");
        request.setWarehouseId(1);
        request.setQuantity(5);
        request.setBinCode("W1-A1");
        return request;
    }

    // --- Test Cases ---

    @Test
    void shouldReturn200OkOnSuccessfulReservation() throws Exception {
        // Arrange
        ReservationRequest request = createRequest();
        BinInventory mockResult = new BinInventory();
        mockResult.setSkuId("SKU-123");

        // Mock successful call
        when(inventoryService.processReservation(any(ReservationRequest.class)))
                .thenReturn(mockResult);

        // Act & Assert
        mockMvc.perform(post("/api/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Expect HTTP 200
    }

    @Test
    void shouldReturn400BadRequestOnInsufficientStock() throws Exception {
        // Arrange
        ReservationRequest request = createRequest();

        // Mock business logic failure (Insufficent Stock)
        when(inventoryService.processReservation(any(ReservationRequest.class)))
                .thenThrow(new RuntimeException("Insufficient inventory"));

        // Act & Assert
        mockMvc.perform(post("/api/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Expect HTTP 400
    }

    @Test
    void shouldReturn409ConflictOnOptimisticLockFailure() throws Exception {
        // Arrange
        ReservationRequest request = createRequest();

        // Mock JPA/Spring throwing the lock failure exception
        // The GlobalExceptionHandler should catch this and map it to 409
        when(inventoryService.processReservation(any(ReservationRequest.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(BinInventory.class, "SKU-123"));

        // Act & Assert
        mockMvc.perform(post("/api/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()); // Expect HTTP 409
    }
}