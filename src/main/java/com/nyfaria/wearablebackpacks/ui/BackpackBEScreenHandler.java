package com.nyfaria.wearablebackpacks.ui;

import com.nyfaria.wearablebackpacks.WearableBackpacks;
import com.nyfaria.wearablebackpacks.api.Dimension;
import com.nyfaria.wearablebackpacks.api.Point;
import com.nyfaria.wearablebackpacks.item.BackpackItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

public class BackpackBEScreenHandler extends ScreenHandler {

    public final int rows;
    public final int columns;
    private final int padding = 8;
    private final int titleSpace = 10;
    private final BlockPos pos;
    private final Inventory container;

    public BackpackBEScreenHandler(int id, PlayerInventory player, PacketByteBuf buff) {
        this(id, player, new SimpleInventory(36),BlockPos.ORIGIN);
    }
    public BackpackBEScreenHandler(int id, PlayerInventory player, Inventory backpackInventory, BlockPos pos) {
        super(WearableBackpacks.BACKPACK_BE_CONTAINER, id);
        this.rows = WearableBackpacks.getInstance().CONFIG.rows;
        this.columns = WearableBackpacks.getInstance().CONFIG.columns;
        this.pos = pos;
        container = backpackInventory;
        this.addSlots(this.rows, this.columns, backpackInventory, player);
        container.onOpen(player.player);

    }



    public Dimension getDimension() {
        return new Dimension(padding * 2 + Math.max(this.columns, 9) * 18, padding * 2 + titleSpace * 2 + 8 + (this.rows + 4) * 18);
    }

    public Point getBackpackSlotPosition(Dimension dimension, int x, int y) {
        return new Point(dimension.getWidth() / 2 - columns * 9 + x * 18, padding + titleSpace + y * 18);
    }

    public Point getPlayerInvSlotPosition(Dimension dimension, int x, int y) {

        return new Point(dimension.getWidth() / 2 - 9 * 9 + x * 18, dimension.getHeight() - padding - 4 * 18 - 3 + y * 18 + (y == 3 ? 4 : 0));

    }

    @SuppressWarnings("unused")
    private void addSlots(int rows, int columns, Inventory inventory, Inventory player) {
        Dimension dimension = getDimension();
        int startX = 8;
        int startY = rows < 9 ? 17 : 8;

        rows = Math.min(rows, 9);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Point getBackpackSlotPosition = getBackpackSlotPosition(dimension, column, row);
                int index = row * columns + column;
                this.addSlot(new Slot(inventory, index, getBackpackSlotPosition.x, getBackpackSlotPosition.y));
            }
        }

        startX = 8 + (columns - 9) * 9;
        startY += rows * 18 + (rows == 9 ? 4 : 13);

        // player slots
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                Point playerInvSlotPosition = getPlayerInvSlotPosition(dimension, column, row);
                int index = column + row * 9 + 9;
                this.addSlot(new Slot(player, index, playerInvSlotPosition.x, playerInvSlotPosition.y));
            }
        }

        startY += 58;

        for (int column = 0; column < 9; column++) {
            Point playerInvSlotPosition = getPlayerInvSlotPosition(dimension, column, 3);
            this.addSlot(new Slot(player, column, playerInvSlotPosition.x, playerInvSlotPosition.y));
        }
    }

    @Override
    public boolean canUse(PlayerEntity playerIn) {
        return playerIn.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < 64;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity playerIn, int index) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            returnStack = slotStack.copy();
            if (index < this.rows * this.columns) {
                if (!this.insertItem(slotStack, this.rows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(slotStack, 0, this.rows * 9, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return returnStack;
    }


    @Override
    public void close(PlayerEntity pPlayer) {
        container.onClose(pPlayer);
        super.close(pPlayer);
    }

    @Override
    public void onSlotClick(int slotId, int dragType, SlotActionType clickTypeIn, PlayerEntity player) {
        if (clickTypeIn == SlotActionType.PICKUP && dragType == 1 && slotId >= 0) {
            Slot slot = this.getSlot(slotId);
            if (slot.canTakeItems(player)) {
                ItemStack stack = slot.getStack();
                if (stack.getItem() instanceof BackpackItem) {
                    if (!player.world.isClient) {
                        int bagSlot = slotId >= (this.rows + 3) * 9 ? slotId - (this.rows + 3) * 9 : slotId >= this.rows * 9 ? slotId - (this.rows - 1) * 9 : -1;
                    }
                    return;
                }
            }
        }
        super.onSlotClick(slotId, dragType, clickTypeIn, player);
    }
}
