package net.zephyrology.dragontrial.Item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.zephyrology.dragontrial.DragonTrial1;
import net.zephyrology.dragontrial.entity.ModEntities;

public class ModItems {

    public static final Item DRAGON_SPAWN_EGG = registerItem("dragon_spawn_egg", new SpawnEggItem(ModEntities.DRAGON, 0xA020F0, 0xD500F9, new Item.Settings()));

    private static Item registerItem (String name, Item item){
       return Registry.register(Registries.ITEM, Identifier.of(DragonTrial1.MOD_ID, name), item) ;
    }

    public static void registerModItems () {
        DragonTrial1.LOGGER.info("Registering Mod Items for " + DragonTrial1.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(DRAGON_SPAWN_EGG);
        });
    }


}
