package com.nyfaria.wearablebackpacks.client;

import com.nyfaria.wearablebackpacks.block.entity.BackpackBlockEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class SimpleBlockEntityRenderer<T extends BackpackBlockEntity & IAnimatable> extends GeoBlockRenderer<T> {

    T theEntity;

    public SimpleBlockEntityRenderer() {
        super(new SimpleModel<>("block"));
    }


    @Override
    public void render(T tile, float partialTicks, MatrixStack stack, VertexConsumerProvider bufferIn, int packedLightIn) {
        theEntity = tile;
        super.render(tile, partialTicks, stack, bufferIn, packedLightIn);
    }



    @Override
    public void renderRecursively(GeoBone bone, MatrixStack stack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if(bone.getName().contains("color")) {
            int i = theEntity.getColor();
            float r = (float) (i >> 16 & 255) / 255.0F;
            float g = (float) (i >> 8 & 255) / 255.0F;
            float b = (float) (i & 255) / 255.0F;
            super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, r,g,b, alpha);
        }else{
            super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
    }
}
