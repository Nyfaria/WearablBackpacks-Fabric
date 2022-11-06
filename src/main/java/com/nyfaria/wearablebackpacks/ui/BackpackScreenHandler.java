package com.nyfaria.wearablebackpacks.ui;

import com.nyfaria.wearablebackpacks.WearableBackpacks;
import com.nyfaria.wearablebackpacks.api.Dimension;
import com.nyfaria.wearablebackpacks.api.Point;
import com.nyfaria.wearablebackpacks.config.WearableBackpacksConfig;
import com.nyfaria.wearablebackpacks.item.BackpackItem;
import com.nyfaria.wearablebackpacks.util.InventoryUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class BackpackScreenHandler extends ScreenHandler {

    ItemStack backpackStack;
    private final int padding = 8;
    private final int titleSpace = 10;

    //private ItemStack backpack;
    public BackpackScreenHandler(int synchronizationID, PlayerInventory playerInventory, PacketByteBuf packetByteBuf) {
        this(synchronizationID, playerInventory, packetByteBuf.readItemStack());
    }
    public BackpackScreenHandler(int synchronizationID, PlayerInventory playerInventory, ItemStack backpackStack) {
        super(WearableBackpacks.CONTAINER_TYPE, synchronizationID);
        this.backpackStack = backpackStack;

        if (backpackStack.getItem() instanceof BackpackItem) {
            setupContainer(playerInventory, backpackStack);
        } else {
            PlayerEntity player = playerInventory.player;
            this.close(player);
        }
    }
    private void setupContainer(PlayerInventory playerInventory, ItemStack backpackStack) {
        Dimension dimension = getDimension();
        int rowWidth = WearableBackpacks.getInstance().CONFIG.columns;
        int numberOfRows = WearableBackpacks.getInstance().CONFIG.rows;

        NbtCompound tag = backpackStack.getOrCreateNbt().getCompound("PlayerInventory");
        SimpleInventory inventory = new SimpleInventory(rowWidth * numberOfRows) {
            @Override
            public void markDirty() {
                backpackStack.getOrCreateNbt().put("PlayerInventory", InventoryUtils.toTag(this));
                super.markDirty();
            }
        };

        InventoryUtils.fromTag(tag, inventory);

        for (int y = 0; y < numberOfRows; y++) {
            for (int x = 0; x < rowWidth; x++) {
                Point backpackSlotPosition = getBackpackSlotPosition(dimension, x, y);
                addSlot(new Slot(inventory, y * rowWidth + x, backpackSlotPosition.x + 1, backpackSlotPosition.y + 1));
            }
        }

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                Point playerInvSlotPosition = getPlayerInvSlotPosition(dimension, x, y);
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, playerInvSlotPosition.x + 1, playerInvSlotPosition.y + 1));
            }
        }

        for (int x = 0; x < 9; ++x) {
            Point playerInvSlotPosition = getPlayerInvSlotPosition(dimension, x, 3);
            this.addSlot(new Slot(playerInventory, x, playerInvSlotPosition.x + 1, playerInvSlotPosition.y + 1));
        }
    }

    public ItemStack getItem() {
        return backpackStack;
    }

    public Dimension getDimension() {
        WearableBackpacksConfig config = WearableBackpacks.getInstance().CONFIG;
        return new Dimension(padding * 2 + Math.max(config.columns, 9) * 18, padding * 2 + titleSpace * 2 + 8 + (config.rows + 4) * 18);
    }

    public Point getBackpackSlotPosition(Dimension dimension, int x, int y) {
        WearableBackpacksConfig config = WearableBackpacks.getInstance().CONFIG;
        return new Point(dimension.getWidth() / 2 - config.columns * 9 + x * 18, padding + titleSpace + y * 18);
    }

    public Point getPlayerInvSlotPosition(Dimension dimension, int x, int y) {
        return new Point(dimension.getWidth() / 2 - 9 * 9 + x * 18, dimension.getHeight() - padding - 4 * 18 - 3 + y * 18 + (y == 3 ? 4 : 0));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return backpackStack.getItem() instanceof BackpackItem;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack toInsert = slot.getStack();
            itemStack = toInsert.copy();
            WearableBackpacksConfig confg = WearableBackpacks.getInstance().CONFIG;
            int size = confg.columns * confg.rows;
            if (index < size) {
                if (!this.insertItem(toInsert, size, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(toInsert, 0, size, false)) {
                return ItemStack.EMPTY;
            }

            if (toInsert.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return itemStack;
    }

   
}
