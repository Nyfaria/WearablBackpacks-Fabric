package com.nyfaria.wearablebackpacks.mixin;

import com.mojang.authlib.GameProfile;
import com.nyfaria.wearablebackpacks.WearableBackpacks;
import com.nyfaria.wearablebackpacks.block.entity.BackpackBlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {


    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, gameProfile, publicKey);
    }

    @Inject(method = "onDeath", at = @At(value = "HEAD"))
    private void callOnKillForPlayer(DamageSource source, CallbackInfo ci) {
        if(!this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)){
            if(this.getEquippedStack(EquipmentSlot.CHEST).isOf(WearableBackpacks.BACKPACK_ITEM)){
                world.setBlockState(getBlockPos(), WearableBackpacks.BACKPACK.getDefaultState());
                BackpackBlockEntity be = (BackpackBlockEntity)world.getBlockEntity(getBlockPos());
                be.setColor(WearableBackpacks.BACKPACK_ITEM.getColor(getEquippedStack(EquipmentSlot.CHEST)));
                be.setItems(getEquippedStack(EquipmentSlot.CHEST).copy());
                getEquippedStack(EquipmentSlot.CHEST).decrement(1);
            }
        }
    }
}
