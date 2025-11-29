package com.vanikathunaivan.pandakasalai.events;

import com.vanikathunaivan.pandakasalai.model.BinInventory;

public class InventoryUpdatedEvent {
    private final BinInventory inventory;

    public InventoryUpdatedEvent(BinInventory inventory) {
        this.inventory = inventory;
    }

    public BinInventory getInventory() {
        return inventory;
    }
}
