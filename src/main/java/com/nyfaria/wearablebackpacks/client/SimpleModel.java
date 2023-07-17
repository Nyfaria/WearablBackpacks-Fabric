package com.nyfaria.wearablebackpacks.client;

import com.nyfaria.wearablebackpacks.WearableBackpacks;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SimpleModel<T extends IAnimatable> extends AnimatedGeoModel<T> {
    private final Identifier texture;
    private final Identifier model;
    private final Identifier animations;

    public SimpleModel(String folder) {
        this.texture = new Identifier(WearableBackpacks.MODID, "textures/" + folder + "/backpack.png");
        this.model = new Identifier(WearableBackpacks.MODID, "geo/" + folder + "/backpack.geo.json");
        this.animations = new Identifier(WearableBackpacks.MODID, "animations/" + folder + "/backpack.animation.json");
    }
    @Override
    public Identifier getModelResource(T object) {
        return model;
    }

    @Override
    public Identifier getTextureResource(T object) {
        return texture;
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return animations;
    }
}
