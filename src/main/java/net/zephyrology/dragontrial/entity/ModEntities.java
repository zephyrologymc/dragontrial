package net.zephyrology.dragontrial.entity;


import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.zephyrology.dragontrial.DragonTrial1;
import net.zephyrology.dragontrial.entity.custom.DragonEntity;

public class ModEntities {

    public static final EntityType<DragonEntity> DRAGON = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(DragonTrial1.MOD_ID, "dragon"),
            EntityType.Builder.create(DragonEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6f, 0.8f).build());

    public static void registerModEntities () {
        DragonTrial1.LOGGER.info("Registering Mod Entities for "+ DragonTrial1.MOD_ID + "... ");
    }


}
