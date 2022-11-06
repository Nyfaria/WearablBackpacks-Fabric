package com.nyfaria.wearablebackpacks;

import com.nyfaria.wearablebackpacks.client.SimpleArmorRenderer;
import com.nyfaria.wearablebackpacks.client.SimpleBlockEntityRenderer;
import com.nyfaria.wearablebackpacks.client.SimpleItemRenderer;
import com.nyfaria.wearablebackpacks.client.WearableBackpacksKeybinds;
import com.nyfaria.wearablebackpacks.ui.BackpackHandledScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

@Environment(EnvType.CLIENT)
public class WearableBackpacksClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        ScreenRegistry.register(WearableBackpacks.CONTAINER_TYPE, BackpackHandledScreen::new);
        registerBackpacks();
        WearableBackpacksKeybinds.initialize();
        GeoArmorRenderer.registerArmorRenderer(new SimpleArmorRenderer(), WearableBackpacks.BACKPACK_ITEM);
        GeoItemRenderer.registerItemRenderer(WearableBackpacks.BACKPACK_ITEM, new SimpleItemRenderer());
        BlockEntityRendererRegistry.register(WearableBackpacks.BACKPACK_BE, (BlockEntityRendererFactory.Context rendererDispatcherIn) -> new SimpleBlockEntityRenderer<>());
    }
    private void registerBackpacks() {


    }

    public static Identifier id(String name) {
        return new Identifier("wearablebackpacks", name);

    }

}
