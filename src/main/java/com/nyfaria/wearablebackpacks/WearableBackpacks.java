package com.nyfaria.wearablebackpacks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nyfaria.wearablebackpacks.block.BackpackBlock;
import com.nyfaria.wearablebackpacks.block.entity.BackpackBlockEntity;
import com.nyfaria.wearablebackpacks.config.WearableBackpacksConfig;
import com.nyfaria.wearablebackpacks.item.BackpackItem;
import com.nyfaria.wearablebackpacks.network.ServerNetworking;
import com.nyfaria.wearablebackpacks.ui.BackpackBEScreenHandler;
import com.nyfaria.wearablebackpacks.ui.BackpackScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class WearableBackpacks implements ModInitializer {
	public static final String MODID = "wearablebackpacks";
	public WearableBackpacksConfig CONFIG;
	public static final Identifier CONTAINER_ID = id("backpack");
	public static final TagKey<EntityType<?>> BACKPACKABLE = entityTag("backpack");
	public static WearableBackpacks instance;
	public static final ScreenHandlerType<BackpackBEScreenHandler> BACKPACK_BE_CONTAINER =  ScreenHandlerRegistry.registerExtended(new Identifier(""), BackpackBEScreenHandler::new);
	public static final Block BACKPACK = new BackpackBlock(AbstractBlock.Settings.of(Material.SOIL, MapColor.BROWN).strength(2,3));

	public static final BlockEntityType<BackpackBlockEntity> BACKPACK_BE =  FabricBlockEntityTypeBuilder.create(BackpackBlockEntity::new, BACKPACK).build(null);

	public static final ScreenHandlerType<BackpackScreenHandler> CONTAINER_TYPE = ScreenHandlerRegistry.registerExtended(CONTAINER_ID, BackpackScreenHandler::new);
	public static final BackpackItem BACKPACK_ITEM = new BackpackItem(BACKPACK,new FabricItemSettings().group(ItemGroup.MISC));
	public static WearableBackpacks getInstance(){
		return instance;
	}
	@Override
	public void onInitialize() {
		loadConfig();
		ServerNetworking.init();
		instance = this;
		Registry.register(Registry.ITEM, id("backpack"), BACKPACK_ITEM);
		Registry.register(Registry.BLOCK, id("backpack"), BACKPACK);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, id("backpack"), BACKPACK_BE);
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
			if(state.isOf(BACKPACK)){
				if(player.isSneaking()){
					if(player.getEquippedStack(EquipmentSlot.CHEST).isOf(BACKPACK_ITEM)){
						player.sendMessage(Text.of("You can only Equip one Backpack at a time"), true);
						return false;
					}
				}
			}
			return true;
		});
		PlayerBlockBreakEvents.CANCELED.register((world, player, pos, state, entity) -> {
			if(state.isOf(BACKPACK)){
				if(player.isSneaking()){
					if(player.getEquippedStack(EquipmentSlot.CHEST).isOf(BACKPACK_ITEM)){
						((BackpackBlockEntity)world.getBlockEntity(pos)).updateBlock();
					}
				}
			}
		});
		UseBlockCallback.EVENT.register((player, world, hand, hitResult)->{
			if(!world.isClient) {
				if (hand == Hand.MAIN_HAND) {
					if (player.isSneaking()) {
						if (hitResult.getSide() == Direction.UP) {
							if (player.getEquippedStack(EquipmentSlot.CHEST).isOf(BACKPACK_ITEM)) {
								return WearableBackpacks.BACKPACK_ITEM.place(new ItemPlacementContext(player, hand, player.getEquippedStack(EquipmentSlot.CHEST), hitResult));
							}
						}
					}
				}
			}
			return ActionResult.PASS;
		});
		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(
				(world, entity, killedEntity) -> {
					if(killedEntity.getEquippedStack(EquipmentSlot.CHEST).isOf(BACKPACK_ITEM)){
						world.setBlockState(killedEntity.getBlockPos(), BACKPACK.getDefaultState());
						BackpackBlockEntity be = (BackpackBlockEntity)world.getBlockEntity(killedEntity.getBlockPos());
						be.setColor(BACKPACK_ITEM.getColor(killedEntity.getEquippedStack(EquipmentSlot.CHEST)));
						be.setItems(killedEntity.getEquippedStack(EquipmentSlot.CHEST).copy());
						killedEntity.getEquippedStack(EquipmentSlot.CHEST).decrement(1);
					}
				}
		);

	}
	public static Identifier id(String name) {
		return new Identifier("wearablebackpacks", name);

	}
	public void loadConfig() {
		File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "wearablebackpacks_config.json");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		if (configFile.exists()) {
			try {
				FileReader fileReader = new FileReader(configFile);
				CONFIG = gson.fromJson(fileReader, WearableBackpacksConfig.class);
				fileReader.close();
			} catch (IOException e) {

			}
		} else {
			CONFIG = new WearableBackpacksConfig();
			saveConfig();
		}
	}

	public void saveConfig() {
		File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "wearablebackpacks_config.json");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		if (!configFile.getParentFile().exists()) {
			configFile.getParentFile().mkdir();
		}
		try {
			FileWriter fileWriter = new FileWriter(configFile);
			fileWriter.write(gson.toJson(CONFIG));
			fileWriter.close();
		} catch (IOException e) {

		}
	}
	private static TagKey<EntityType<?>> entityTag(String pName) {
		return TagKey.of(Registry.ENTITY_TYPE_KEY, new Identifier(WearableBackpacks.MODID,pName));
	}
}
