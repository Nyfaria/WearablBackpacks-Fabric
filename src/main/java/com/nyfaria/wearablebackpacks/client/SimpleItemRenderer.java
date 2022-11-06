package com.nyfaria.wearablebackpacks.client;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class SimpleItemRenderer<T extends Item & IAnimatable & DyeableItem> extends GeoItemRenderer<T> {

    T item;
    public SimpleItemRenderer() {
        super(new SimpleModel<>("block"));
    }

    @Override
    public void render(T animatable, MatrixStack stack, VertexConsumerProvider bufferIn, int packedLightIn, ItemStack itemStack) {
        item = animatable;
        super.render(animatable, stack, bufferIn, packedLightIn, itemStack);
    }

    @Override
    public void renderRecursively(GeoBone bone, MatrixStack stack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if(bone.getName().contains("color")) {
            int i = item.getColor(currentItemStack);
            float r = (float) (i >> 16 & 255) / 255.0F;
            float g = (float) (i >> 8 & 255) / 255.0F;
            float b = (float) (i & 255) / 255.0F;
            super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, r, g, b, alpha);
        }else{
            super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
    }
}
