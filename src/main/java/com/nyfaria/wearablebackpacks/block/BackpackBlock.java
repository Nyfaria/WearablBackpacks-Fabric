package com.nyfaria.wearablebackpacks.block;

import com.nyfaria.wearablebackpacks.WearableBackpacks;
import com.nyfaria.wearablebackpacks.block.entity.BackpackBlockEntity;
import com.nyfaria.wearablebackpacks.item.BackpackItem;
import com.nyfaria.wearablebackpacks.util.InventoryUtils;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BackpackBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    private static final VoxelShape SHAPE_NORTH = makeShape(Direction.NORTH);
    private static final VoxelShape SHAPE_EAST = makeShape(Direction.EAST);
    private static final VoxelShape SHAPE_SOUTH = makeShape(Direction.SOUTH);
    private static final VoxelShape SHAPE_WEST = makeShape(Direction.WEST);

    public BackpackBlock(Settings p_49795_) {
        super(p_49795_);
    }

    @Override
    public BlockRenderType getRenderType(BlockState pState) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    public BlockState getPlacementState(ItemPlacementContext pContext) {
        return this.getDefaultState()/*.setValue(WATERLOGGED, Boolean.valueOf(levelaccessor.getFluidState(blockpos).getType() == Fluids.WATER))*/
                .with(FACING, pContext.getPlayerFacing());
    }

    @Override
    public ActionResult onUse(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockHitResult pHit) {
        if (!pLevel.isClient) {

            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof BackpackBlockEntity) {
                BackpackBlockEntity backpackBlockEntity = (BackpackBlockEntity) blockEntity;
                pPlayer.openHandledScreen(backpackBlockEntity);
                return ActionResult.CONSUME;
            }
        }
        return super.onUse(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    public static final Identifier CONTENTS = new Identifier("contents");


    @Override
    public ItemStack getPickStack(BlockView level, BlockPos pos, BlockState player) {
        BackpackBlockEntity blockentity = (BackpackBlockEntity) level.getBlockEntity(pos);
        return blockentity.getBackpackStack();
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        Direction direction = pState.get(FACING);
        return switch (direction) {
            case NORTH -> SHAPE_NORTH;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    public static VoxelShape makeShape(Direction direction) {
        VoxelShape shape = VoxelShapes.empty();
        if (direction == Direction.NORTH) {
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.1875, 0, 0.28125, 0.8125, 0.5625, 0.59375), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.25, 0.0625, 0.59375, 0.75, 0.4375, 0.71875), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.1875, 0.5625, 0.28125, 0.8125, 0.75, 0.59375), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.6875, 0.1875, 0.21875, 0.75, 0.6875, 0.28125), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.25, 0.1875, 0.21875, 0.3125, 0.6875, 0.28125), BooleanBiFunction.OR);
        } else if (direction == Direction.WEST) {
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.28125, 0, 0.1875, 0.59375, 0.5625, 0.8125), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.59375, 0.0625, 0.25, 0.71875, 0.4375, 0.75), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.28125, 0.5625, 0.1875, 0.59375, 0.75, 0.8125), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.21875, 0.1875, 0.25, 0.28125, 0.6875, 0.3125), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.21875, 0.1875, 0.625, 0.28125, 0.6875, 0.6875), BooleanBiFunction.OR);
        } else if (direction == Direction.EAST) {
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.40625, 0, 0.1875, 0.71875, 0.5625, 0.8125), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.28125, 0.0625, 0.25, 0.40625, 0.4375, 0.75), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.40625, 0.5625, 0.1875, 0.71875, 0.75, 0.8125), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.71875, 0.1875, 0.25, 0.78125, 0.6875, 0.3125), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.71875, 0.1875, 0.625, 0.78125, 0.6875, 0.6875), BooleanBiFunction.OR);
        } else if (direction == Direction.SOUTH) {
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.1875, 0, 0.40625, 0.8125, 0.5625, 0.71875), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.25, 0.0625, 0.28125, 0.75, 0.4375, 0.40625), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.1875, 0.5625, 0.40625, 0.8125, 0.75, 0.71875), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.6875, 0.1875, 0.71875, 0.75, 0.6875, 0.78125), BooleanBiFunction.OR);
            shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.25, 0.1875, 0.71875, 0.3125, 0.6875, 0.78125), BooleanBiFunction.OR);
        }
        return shape;
    }

    public boolean onSyncedBlockEvent(BlockState pState, World pLevel, BlockPos pPos, int pId, int pParam) {
        super.onSyncedBlockEvent(pState, pLevel, pPos, pId, pParam);
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        return blockentity == null ? false : blockentity.onSyncedBlockEvent(pId, pParam);
    }

    @Nullable
    public ExtendedScreenHandlerFactory getMenuProvider(BlockState pState, World pLevel, BlockPos pPos) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        return blockentity instanceof ExtendedScreenHandlerFactory ? (ExtendedScreenHandlerFactory) blockentity : null;
    }

    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_) {
        return p_152134_ == p_152133_ ? (BlockEntityTicker<A>) p_152135_ : null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new BackpackBlockEntity(pPos, pState);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        BackpackBlockEntity backpackBlockEntity = (BackpackBlockEntity) blockEntity;
        ItemStack stack1 = backpackBlockEntity.getBackpackStack();
        if(player.isSneaking()){
            player.equipStack(EquipmentSlot.CHEST,((BackpackBlockEntity)blockEntity).getBackpackStack());
        } else {
            SimpleInventory inventory = new SimpleInventory(backpackBlockEntity.size());
            InventoryUtils.fromTag(stack1.getOrCreateNbt().getCompound("PlayerInventory"), inventory);
            ItemScatterer.spawn(world, pos, inventory);
            ItemStack dropStack = new ItemStack(WearableBackpacks.BACKPACK_ITEM);
            ((DyeableItem)dropStack.getItem()).setColor(dropStack, ((DyeableItem)stack1.getItem()).getColor(stack1));
            world.spawnEntity(new ItemEntity(world, pos.getX()+0.5, pos.getY() +0.5, pos.getZ()+0.5, dropStack));
        }
    }

//    @Override
//    public boolean onDestroyedByPlayer(BlockState state, World pLevel, BlockPos pPos, Player pPlayer, boolean willHarvest, FluidState fluid) {
//            if (pLevel.getBlockEntity(pPos) instanceof BackpackBlockEntity blockEntity) {
//                ItemStack itemstack = getColoredItemStack(blockEntity.getColor());
//                itemstack.setTag(blockEntity.getBackpackTag());
//                if (pPlayer.isShiftKeyDown()) {
//                    NbtCompound tag = new NbtCompound();
//                    ContainerHelper.saveAllItems(tag, BackpackBlockEntity.getInventory(blockEntity));
//                    BackpackHolderAttacher.getBackpackHolderUnwrap(itemstack).deserializeNBT(tag, true);
//                    if (pPlayer.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
//                        if (!pLevel.isClientSide) {
//                            pPlayer.setItemSlot(EquipmentSlot.CHEST, itemstack);
//                        }
//                    } else {
//                        if (pPlayer.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.BACKPACK.get()) && !pLevel.isClientSide) {
//                            pPlayer.sendMessage(new TranslatableComponent("message.wearablebackpacks.limit"), UUID.randomUUID());
//                        } else if(!pLevel.isClientSide) {
//                            pPlayer.sendMessage(new TranslatableComponent("message.wearablebackpacks.chestplate"), UUID.randomUUID());
//                        }
//                        return false;
//                    }
//                } else {
//                    ItemEntity drop = new ItemEntity(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), itemstack);
//                    drop.setDefaultPickUpDelay();
//                    pLevel.addFreshEntity(drop);
//                    Containers.dropContents(pLevel,pPos,blockEntity);
//                }
//            }
//        return super.onDestroyedByPlayer(state, pLevel, pPos, pPlayer, willHarvest, fluid);
//    }

    public static ItemStack getColoredItemStack(@Nullable int pColor) {
        ItemStack stack = new ItemStack(WearableBackpacks.BACKPACK);
        ((DyeableItem) stack.getItem()).setColor(stack, pColor);
        return stack;
    }


}
