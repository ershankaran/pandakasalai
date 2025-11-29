package com.vanikathunaivan.pandakasalai.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class DataGenerator {
    // --- CONFIGURATION ---
    // Generate 1 million inventory records

    private static final int NUM_SKUS = 10000; // Assume 10000 unique SKUs
    private static final int NUM_WAREHOUSES = 20; // Assume 20 Warehouses
    private static final int BINS_PER_SKU_PER_WAREHOUSE = 5; // Allow 5 bins for each SKU/Warehouse pair

    // Target rows: 10,000 SKUs * 20 Warehouses * 5 Bins = 1,000,000 unique rows
    private static final int NUM_RECORDS = NUM_SKUS * NUM_WAREHOUSES * BINS_PER_SKU_PER_WAREHOUSE;
    private static final String FILE_NAME = "inventory_data_fixed.csv";

    public static void main(String[] args) {
        System.out.println("Starting CSV generation for " + NUM_RECORDS + " records...");

        long startTime = System.currentTimeMillis();
        Random random = new Random();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {

            // 1. Write the CSV Header (must match the SQL column order)
            writer.write("sku_id,warehouse_id,bin_code,available_qty\n");

            // 2. Loop to generate records
            for (int s = 1; s <= NUM_SKUS; s++) {
                for (int w = 1; w <= NUM_WAREHOUSES; w++) {
                    for (int b = 1; b <= BINS_PER_SKU_PER_WAREHOUSE; b++) {

                        String skuId = "SKU-" + s;
                        int warehouseId = w;

                        // Create a unique bin code tied to the index (e.g., A-13-5)
                        String binCode = String.format("W%d-B%d", w, b);

                        int availableQty = random.nextInt(1001);

                        String line = String.format("%s,%d,%s,%d\n",
                                skuId,
                                warehouseId,
                                binCode,
                                availableQty);
                        writer.write(line);
                    }
                }
            }

            System.out.println("CSV generation complete.");

        } catch (IOException e) {
            System.err.println("Error generating CSV: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("Total time taken: %.2f seconds\n", (endTime - startTime) / 1000.0);
    }
}
