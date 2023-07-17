package com.nyfaria.wearablebackpacks.block.entity;

import com.nyfaria.wearablebackpacks.WearableBackpacks;
import com.nyfaria.wearablebackpacks.ui.BackpackBEScreenHandler;
import com.nyfaria.wearablebackpacks.ui.BackpackScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class BackpackBlockEntity extends LockableContainerBlockEntity implements IAnimatable, ExtendedScreenHandlerFactory {
    private final AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);
    private boolean accessed = false;
    private int color = 0;

    private DefaultedList<ItemStack> items = DefaultedList.ofSize(36, ItemStack.EMPTY);
    private NbtCompound backpackTag;
    private ItemStack backpackStack;

    public BackpackBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(WearableBackpacks.BACKPACK_BE, pWorldPosition, pBlockState);
    }

    public void setBackpackTag(NbtCompound tag) {
        this.backpackTag = tag;
        Inventories.readNbt(tag, this.items);
        updateBlock();
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }

    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {
//        AnimationController controller = event.getController();
//        if(accessed) {
//            controller.setAnimation(new AnimationBuilder().addAnimation("open", true));
//            return PlayState.CONTINUE;
//        }
        return PlayState.STOP;
    }

    public static DefaultedList<ItemStack> getInventory(BackpackBlockEntity itemStack) {
        return itemStack.items;
    }

    public void readNbt(NbtCompound pTag) {
        super.readNbt(pTag);
        this.items = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readNbt(pTag, this.items);
        this.accessed = pTag.getBoolean("accessed");
        this.color = pTag.getInt("color");
        this.backpackTag = pTag.getCompound("backpackTag");
        if(pTag.contains("backpackStack"))
            this.backpackStack = ItemStack.fromNbt(pTag.getCompound("backpackStack"));
    }

    protected void writeNbt(NbtCompound pTag) {
        super.writeNbt(pTag);
        Inventories.writeNbt(pTag, this.items);
        pTag.putBoolean("accessed", accessed);
        pTag.putInt("color", color);
        pTag.put("backpackTag", backpackTag);
        if(backpackStack != null)
            pTag.put("backpackStack", backpackStack.writeNbt(new NbtCompound()));
    }


    public void setAccessed(boolean accessed) {
        this.accessed = accessed;
    }

    @Override
    public int size() {
        return WearableBackpacks.getInstance().CONFIG.columns * WearableBackpacks.getInstance().CONFIG.rows;
    }

    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }


    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = new NbtCompound();
        writeNbt(tag);
        return tag;
    }

    public ItemStack getBackpackStack() {
        return backpackStack;
    }

    @Override
    public ItemStack getStack(int pIndex) {
        return items.get(pIndex);
    }

    @Override
    public ItemStack removeStack(int pIndex, int pCount) {
        ItemStack itemstack = Inventories.splitStack(this.items, pIndex, pCount);
        if (!itemstack.isEmpty()) {
            this.markDirty();
        }
        return itemstack;
    }

    @Override
    public ItemStack removeStack(int pIndex) {
        return Inventories.removeStack(items, pIndex);
    }

    @Override
    public void setStack(int pIndex, ItemStack pStack) {
        items.set(pIndex, pStack);
        if (pStack.getCount() > this.getMaxCountPerStack()) {
            pStack.setCount(this.getMaxCountPerStack());
        }

        this.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity pPlayer) {
        if (this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return !(pPlayer.squaredDistanceTo((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) > 64.0D);
        }
    }

    public void setColor(int color) {
        this.color = color;
        updateBlock();
    }

    public int getColor() {
        return color;
    }

    public void setItems(ItemStack items) {
        this.backpackStack = items;
        updateBlock();
    }

    @Override
    protected Text getContainerName() {
        return Text.literal("Backpack");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new BackpackBEScreenHandler(syncId, playerInventory, this, getPos());
    }


    @Override
    public void clear() {

    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public NbtCompound getBackpackTag() {
        return backpackTag;
    }

    public void updateBlock() {
        BlockState blockState = getCachedState();
        this.world.updateListeners(this.getPos(), blockState, blockState, 3);
        this.markDirty();
    }

    @Override
    public AnimationFactory getFactory() {
        return animationFactory;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeItemStack(backpackStack);
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new BackpackScreenHandler(i, playerInventory, backpackStack);
    }
}
