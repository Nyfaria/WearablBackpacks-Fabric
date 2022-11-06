package com.nyfaria.wearablebackpacks.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public abstract class SyncingBlockEntity extends BlockEntity {

    public SyncingBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    public void saveData(NbtCompound pTag) {}

    public void loadData(NbtCompound pTag) {}


    @Override
    public void readNbt(NbtCompound tag) {
        loadData(tag);
        super.readNbt(tag);
    }

    @Override
    protected void writeNbt(NbtCompound pTag) {
        super.writeNbt(pTag);
        saveData(pTag);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = new NbtCompound();
        saveData(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public void updateBlock() {
        BlockState blockState = world.getBlockState(this.getPos());
        this.world.updateListeners(this.getPos(), blockState, blockState, 3);
        this.markDirty();
    }
}
