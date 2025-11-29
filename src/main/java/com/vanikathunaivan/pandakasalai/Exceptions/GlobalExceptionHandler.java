package com.vanikathunaivan.pandakasalai.Exceptions;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handler for Optimistic Locking Failure (The 409 Conflict)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<String> handleOptimisticLockingFailureException(ObjectOptimisticLockingFailureException ex) {
        System.err.println("Optimistic Locking Conflict: " + ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // Returns 409
                .body("Reservation failed due to concurrent update. Please retry the order.");
    }

    // 2. Handler for Business Logic Exceptions (The 400 Bad Request)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        // Catch the custom 'Insufficient inventory' or 'Inventory not found' messages
        if (ex.getMessage().contains("inventory") || ex.getMessage().contains("not found")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()); // Returns 400
        }

        // 3. Fallback for all other unknown errors
        System.err.println("Uncaught Internal Server Error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unknown error occurred."); // Returns 500
    }
}
