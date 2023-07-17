package com.nyfaria.wearablebackpacks.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.nyfaria.wearablebackpacks.WearableBackpacks;
import com.nyfaria.wearablebackpacks.block.entity.BackpackBlockEntity;
import com.nyfaria.wearablebackpacks.ui.BackpackScreenHandler;
import com.nyfaria.wearablebackpacks.util.BackpackMaterial;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.UUID;


public class BackpackItem extends ArmorItem implements IAnimatable, DyeableItem {
    private final Block block;
    private final AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    public BackpackItem(Block pBlock, Item.Settings settings) {
        super(BackpackMaterial.LEATHER, EquipmentSlot.CHEST, settings);
        this.block = pBlock;

    }


    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand interactionHand) {
        player.setCurrentHand(interactionHand);
        if(WearableBackpacks.getInstance().CONFIG.canOpenWithHand) {
            openScreen(player, player.getStackInHand(interactionHand), interactionHand);
        }
        return TypedActionResult.success(player.getStackInHand(interactionHand));
    }

    public static void openScreen(PlayerEntity player, ItemStack backpackItemStack, Hand hand) {
        if(player.world != null && !player.world.isClient) {
            player.openHandledScreen(new ExtendedScreenHandlerFactory() {

                @Override
                public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                    buf.writeItemStack(backpackItemStack);
                }

                @Override
                public Text getDisplayName() {
                    return Text.translatable(backpackItemStack.getItem().getTranslationKey());
                }

                @Override
                public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new BackpackScreenHandler(syncId, inv, backpackItemStack);
                }
            });
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext pContext) {
        if(pContext.getSide() != Direction.UP || !pContext.getWorld().getBlockState(pContext.getBlockPos()).isFullCube(pContext.getWorld(),pContext.getBlockPos()) ){
            return super.useOnBlock(pContext);
        }
        ActionResult interactionresult = this.place(new ItemPlacementContext(pContext));
        if (!interactionresult.isAccepted() && this.isFood()) {
            ActionResult interactionresult1 = this.use(pContext.getWorld(), pContext.getPlayer(), pContext.getHand()).getResult();
            return interactionresult1 == ActionResult.CONSUME ? ActionResult.CONSUME_PARTIAL : interactionresult1;
        } else {
            return interactionresult;
        }
    }
    @javax.annotation.Nullable
    public ItemPlacementContext updatePlacementContext(ItemPlacementContext pContext) {
        return pContext;
    }
    @javax.annotation.Nullable
    protected BlockState getPlacementState(ItemPlacementContext pContext) {
        BlockState blockstate = this.getBlock().getPlacementState(pContext);
        return blockstate != null && this.canPlace(pContext, blockstate) ? blockstate : null;
    }
    public Block getBlock() {
        return this.getBlockRaw();
    }
    protected boolean canPlace(ItemPlacementContext pContext, BlockState pState) {
        PlayerEntity player = pContext.getPlayer();
        ShapeContext collisioncontext = player == null ? ShapeContext.absent() : ShapeContext.of(player);
        return (!this.mustSurvive() || pState.canPlaceAt(pContext.getWorld(), pContext.getBlockPos())) && pContext.getWorld().canPlace(pState, pContext.getBlockPos(), collisioncontext);
    }
    protected boolean mustSurvive() {
        return true;
    }
    private Block getBlockRaw() {
        return this.block;
    }
    protected boolean placeBlock(ItemPlacementContext pContext, BlockState pState) {
        return pContext.getWorld().setBlockState(pContext.getBlockPos(), pState, 11);
    }
    public ActionResult place(ItemPlacementContext pContext) {
        if (!pContext.canPlace()) {
            return ActionResult.FAIL;
        } else {
            ItemPlacementContext blockplacecontext = this.updatePlacementContext(pContext);
            if (blockplacecontext == null) {
                return ActionResult.FAIL;
            } else {
                BlockState blockstate = this.getPlacementState(blockplacecontext);
                if (blockstate == null) {
                    return ActionResult.FAIL;
                } else if (!this.placeBlock(blockplacecontext, blockstate)) {
                    return ActionResult.FAIL;
                } else {
                    BlockPos blockpos = blockplacecontext.getBlockPos();
                    World level = blockplacecontext.getWorld();
                    PlayerEntity player = blockplacecontext.getPlayer();
                    ItemStack itemstack = blockplacecontext.getStack();
                    BlockState blockstate1 = level.getBlockState(blockpos);
                    if (blockstate1.isOf(blockstate.getBlock())) {
                        blockstate1 = this.updateBlockStateFromTag(blockpos, level, itemstack, blockstate1);
                        this.updateCustomBlockEntityTag(blockpos, level, player, itemstack, blockstate1);
                        blockstate1.getBlock().onPlaced(level, blockpos, blockstate1, player, itemstack);
                        if (player instanceof ServerPlayerEntity) {
                            Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity) player, blockpos, itemstack);
                        }
                    }

                    level.emitGameEvent(player, GameEvent.BLOCK_PLACE, blockpos);
                    BlockSoundGroup soundtype = blockstate1.getSoundGroup();//level, blockpos, pContext.getPlayer());
                    level.playSound(player, blockpos, this.getPlaceSound(blockstate1, level, blockpos, pContext.getPlayer()), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    if (player == null || !player.getAbilities().creativeMode) {
                        itemstack.decrement(1);
                    }

                    return ActionResult.success(level.isClient);
                }
            }
        }
    }
    protected SoundEvent getPlaceSound(BlockState state, World world, BlockPos pos, PlayerEntity entity) {
        return state.getSoundGroup().getPlaceSound();
    }
    protected boolean updateCustomBlockEntityTag(BlockPos pPos, World pLevel, @javax.annotation.Nullable PlayerEntity pPlayer, ItemStack pStack, BlockState pState) {
        return updateCustomBlockEntityTag(pLevel, pPlayer, pPos, pStack);
    }
    @Override
    public EquipmentSlot getSlotType() {
        return WearableBackpacks.getInstance().CONFIG.canEquipFromInventory ? EquipmentSlot.CHEST : EquipmentSlot.MAINHAND;
    }
    public static boolean updateCustomBlockEntityTag(World pLevel, @javax.annotation.Nullable PlayerEntity pPlayer, BlockPos pPos, ItemStack pStack) {
        MinecraftServer minecraftserver = pLevel.getServer();
        if (minecraftserver == null) {
            return false;
        } else {
            NbtCompound compoundtag = getBlockEntityData(pStack);
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity != null) {
                ((BackpackBlockEntity) blockentity).setColor(((DyeableItem) pStack.getItem()).getColor(pStack));
//                ((BackpackBlockEntity) blockentity).setItems((BackpackHolderAttacher.getBackpackHolderUnwrap(pStack).getInventory().getStacks()));
                ((BackpackBlockEntity) blockentity).setBackpackTag(pStack.getNbt());
                ((BackpackBlockEntity) blockentity).setItems(pStack.copy());
                ((BackpackBlockEntity) blockentity).updateBlock();

                if (compoundtag != null) {
                    if (!pLevel.isClient && blockentity.copyItemDataRequiresOperator() && (pPlayer == null || !pPlayer.isCreativeLevelTwoOp())) {
                        return false;
                    }
                    NbtCompound compoundtag1 = blockentity.createNbt();
                    NbtCompound compoundtag2 = compoundtag1.copy();
                    compoundtag1.copyFrom(compoundtag);
                    if (!compoundtag1.equals(compoundtag2)) {
                        blockentity.readNbt(compoundtag1);
                        blockentity.markDirty();
                        return true;
                    }
                }
            }

            return false;
        }
    }
    @Nullable
    public static NbtCompound getBlockEntityData(ItemStack p_186337_) {
        return p_186337_.getSubNbt("BlockEntityTag");
    }
    private static <T extends Comparable<T>> BlockState updateState(BlockState pState, Property<T> pProperty, String pValueIdentifier) {
        return pProperty.parse(pValueIdentifier).map((p_40592_) -> {
            return pState.with(pProperty, p_40592_);
        }).orElse(pState);
    }
    private BlockState updateBlockStateFromTag(BlockPos pPos, World pLevel, ItemStack pStack, BlockState pState) {
        BlockState blockstate = pState;
        NbtCompound compoundtag = pStack.getNbt();
        if (compoundtag != null) {
            NbtCompound compoundtag1 = compoundtag.getCompound("BlockStateTag");
            StateManager<Block, BlockState> statedefinition = pState.getBlock().getStateManager();

            for (String s : compoundtag1.getKeys()) {
                Property<?> property = statedefinition.getProperty(s);
                if (property != null) {
                    String s1 = compoundtag1.get(s).asString();
                    blockstate = updateState(blockstate, property, s1);
                }
            }
        }

        if (blockstate != pState) {
            pLevel.setBlockState(pPos, blockstate, 2);
        }

        return blockstate;
    }
    @Override
    public void registerControllers(AnimationData animationData) {

    }

    @Override
    public int getProtection() {
        return WearableBackpacks.getInstance().CONFIG.backpackDefenseLevel;
    }

    @Override
    public float getToughness() {
        return super.getToughness();
    }

    private static final UUID[] ARMOR_MODIFIER_UUID_PER_SLOT = new UUID[]{UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot pEquipmentSlot) {
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        UUID uuid = ARMOR_MODIFIER_UUID_PER_SLOT[pEquipmentSlot.getEntitySlotId()];
        builder.put(EntityAttributes.GENERIC_ARMOR, new EntityAttributeModifier(uuid, "Armor modifier", WearableBackpacks.getInstance().CONFIG.backpackDefenseLevel, EntityAttributeModifier.Operation.ADDITION));
        return pEquipmentSlot == EquipmentSlot.CHEST ? builder.build() : super.getAttributeModifiers(pEquipmentSlot);
    }
    @Override
    public AnimationFactory getFactory() {
        return animationFactory;
    }
}
