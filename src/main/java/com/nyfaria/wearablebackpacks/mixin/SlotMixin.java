package com.nyfaria.wearablebackpacks.mixin;

import com.nyfaria.wearablebackpacks.WearableBackpacks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {

    @Shadow public abstract ItemStack getStack();

    @Shadow public abstract int getIndex();

    @Inject(method = "canTakeItems", at = @At("HEAD"), cancellable = true)
    public void  cantPickUpBackPack(PlayerEntity playerEntity, CallbackInfoReturnable<Boolean> cir){
        if(getIndex()  != 38) return;
        if(getStack().isOf(WearableBackpacks.BACKPACK_ITEM)){
            cir.setReturnValue(WearableBackpacks.getInstance().CONFIG.canEquipFromInventory);
        }
    }
}
