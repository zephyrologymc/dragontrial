package net.zephyrology.dragontrial;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.util.Identifier;

import net.zephyrology.dragontrial.Item.ModItemGroups;
import net.zephyrology.dragontrial.Item.ModItems;
import net.zephyrology.dragontrial.datagen.ModModelProvider;
import net.zephyrology.dragontrial.entity.ModEntities;
import net.zephyrology.dragontrial.entity.custom.DragonEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragonTrial1 implements ModInitializer {
	public static final String MOD_ID = "dragontrial";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		ModEntities.registerModEntities();
		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();


		FabricDefaultAttributeRegistry.register(ModEntities.DRAGON, DragonEntity.createAttributes());

	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
