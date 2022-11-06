package com.nyfaria.wearablebackpacks.util;


import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;


public class InventoryUtils {

    public static NbtCompound toTag(SimpleInventory inventory) {
        NbtCompound nbt = new NbtCompound();
        NbtList nbtList = new NbtList();
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = inventory.getStack(i);
            if (itemStack.isEmpty()) continue;
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putByte("Slot", (byte)i);
            itemStack.writeNbt(nbtCompound);
            nbtList.add(nbtCompound);
        }
        if (!nbtList.isEmpty() ) {
            nbt.put("Items", nbtList);
        }

        return nbt;
    }

    public static void fromTag(NbtCompound nbt, SimpleInventory inventory) {
        inventory.clear();
        NbtList nbtList = nbt.getList("Items", 10);
        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            int j = nbtCompound.getByte("Slot") & 0xFF;
            if (j < 0 || j >= inventory.size()) continue;
            inventory.setStack(j, ItemStack.fromNbt(nbtCompound));
        }
    }
}