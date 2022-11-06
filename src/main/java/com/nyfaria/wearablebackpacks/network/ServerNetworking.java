package com.nyfaria.wearablebackpacks.network;

import com.nyfaria.wearablebackpacks.WearableBackpacks;
import com.nyfaria.wearablebackpacks.item.BackpackItem;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ServerNetworking {

    public static Identifier OPEN_BACKPACK = WearableBackpacks.id("open_backpack");
    public static Identifier INCREASE_SLOT = WearableBackpacks.id("increase_slot");
    public static Identifier DECREASE_SLOT = WearableBackpacks.id("decrease_slot");


    public static void init() {
        registerOpenBackpackPacketHandler();
    }

    private static void registerOpenBackpackPacketHandler() {
        ServerPlayNetworking.registerGlobalReceiver(OPEN_BACKPACK, ServerNetworking::receiveOpenBackpackPacket);
    }

    private static void receiveOpenBackpackPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        if(WearableBackpacks.getInstance().CONFIG.canOpenWhileEquipped) {
            BackpackItem.openScreen(player, player.getEquippedStack(EquipmentSlot.CHEST), player.getActiveHand());
        }
    }

}
