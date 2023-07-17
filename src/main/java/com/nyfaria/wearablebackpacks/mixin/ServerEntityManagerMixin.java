package com.nyfaria.wearablebackpacks.mixin;

import com.google.common.collect.Lists;
import com.nyfaria.wearablebackpacks.WearableBackpacks;
import com.nyfaria.wearablebackpacks.util.InventoryUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Mixin(ServerEntityManager.class)
public class ServerEntityManagerMixin {


    @Inject(method = "addEntity(Lnet/minecraft/world/entity/EntityLike;Z)Z", at = @At("HEAD"))
    public void spawnWithBackPack(EntityLike entity, boolean existing, CallbackInfoReturnable<Boolean> cir) {
        if (!WearableBackpacks.getInstance().CONFIG.shouldEntitiesSpawnWithBackpack) return;
        if (entity instanceof MobEntity livingEntity) {
            if (livingEntity.getType().isIn(WearableBackpacks.BACKPACKABLE)) {
                if (livingEntity.getWorld().random.nextInt(100) < WearableBackpacks.getInstance().CONFIG.entityBackpackChance) {
                    ItemStack backpack = new ItemStack(WearableBackpacks.BACKPACK_ITEM);
                    Identifier lootID = new Identifier(WearableBackpacks.MODID, "backpack/" + Registry.ENTITY_TYPE.getKey(livingEntity.getType()).get().getValue().getNamespace() + "/" + Registry.ENTITY_TYPE.getKey(livingEntity.getType()).get().getValue().getPath());
                    unpackLootTable(lootID, livingEntity.world, livingEntity.getBlockPos(), livingEntity.getRandom().nextLong(), backpack);
                    livingEntity.equipStack(EquipmentSlot.CHEST, backpack);
                    livingEntity.setEquipmentDropChance(EquipmentSlot.CHEST, 1.0f);
                }
            }
        }
    }

    private static void unpackLootTable(Identifier lootTable, World level, BlockPos worldPosition, long lootTableSeed, ItemStack backpackStack) {
        if (lootTable != null && level.getServer() != null) {
            LootTable loottable = level.getServer().getLootManager().getTable(lootTable);
            LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld) level)).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(worldPosition)).random(lootTableSeed);
            SimpleInventory inventory = new SimpleInventory(WearableBackpacks.getInstance().CONFIG.columns * WearableBackpacks.getInstance().CONFIG.rows);
            fill(inventory, lootcontext$builder.build(LootContextTypes.CHEST), loottable);
            InventoryUtils.toTag(inventory);
            backpackStack.getOrCreateNbt().put("PlayerInventory", InventoryUtils.toTag(inventory));
        }

    }

    private static void fill(SimpleInventory inventory, LootContext pContext, LootTable pTable) {
        List<ItemStack> list = pTable.generateLoot(pContext);
        Random random = pContext.getRandom();
        List<Integer> list1 = getAvailableSlots(inventory, random);
        shuffleAndSplitItems(list, list1.size(), random);

        for (ItemStack itemstack : list) {
            if (list1.isEmpty()) {
                return;
            }

            if (itemstack.isEmpty()) {
                inventory.setStack(list1.remove(list1.size() - 1), ItemStack.EMPTY);
            } else {
                inventory.setStack(list1.remove(list1.size() - 1), itemstack);
            }
        }

    }

    private static List<Integer> getAvailableSlots(SimpleInventory pInventory, Random pRand) {
        ObjectArrayList<Integer> list = new ObjectArrayList();

        for (int i = 0; i < pInventory.size(); ++i) {
            if (pInventory.getStack(i).isEmpty()) {
                list.add(i);
            }
        }

        Collections.shuffle(list, new java.util.Random());
        return list;
    }

    private static void shuffleAndSplitItems(List<ItemStack> pStacks, int pEmptySlotsCount, Random pRand) {
        List<ItemStack> list = Lists.newArrayList();
        Iterator<ItemStack> iterator = pStacks.iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack = iterator.next();
            if (itemstack.isEmpty()) {
                iterator.remove();
            } else if (itemstack.getCount() > 1) {
                list.add(itemstack);
                iterator.remove();
            }
        }

        while (pEmptySlotsCount - pStacks.size() - list.size() > 0 && !list.isEmpty()) {
            ItemStack itemstack2 = list.remove(MathHelper.nextInt(pRand, 0, list.size() - 1));
            int i = MathHelper.nextInt(pRand, 1, itemstack2.getCount() / 2);
            ItemStack itemstack1 = itemstack2.split(i);
            if (itemstack2.getCount() > 1 && pRand.nextBoolean()) {
                list.add(itemstack2);
            } else {
                pStacks.add(itemstack2);
            }

            if (itemstack1.getCount() > 1 && pRand.nextBoolean()) {
                list.add(itemstack1);
            } else {
                pStacks.add(itemstack1);
            }
        }

        pStacks.addAll(list);
        Collections.shuffle(pStacks, new java.util.Random());
    }
}
